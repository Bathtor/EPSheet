/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lars Kroll <bathtor@googlemail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import com.lkroll.ep.model.{ EPCharModel => epmodel, MorphSection, GearSection, ArmourItemSection, MeleeWeaponSection, RangedWeaponSection }
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import concurrent.Future
import scala.util.{ Try, Success, Failure }
import scala.collection.mutable

object CharCleanerScript extends EPScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(CharCleanerCommand);
}

class CharCleanerConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val egocast = opt[Boolean]("egocast", descr = "Remove everything not taken along on an Ego Cast from the sheet.");
  val backup = opt[Boolean]("backup", descr = "Remove everything not taken along during an Ego Backup from the sheet.");
  val prefix = opt[String]("prefix", descr = "&lt;param&gt; will be prefixed to the sheet name (default: BACKUP)", default = Some("BACKUP"))(ScallopUtils.singleArgSpacedConverter(identity));

  requireOne(egocast, backup);
  dependsOnAll(prefix, List(backup));
  verify();
}

object CharCleanerCommand extends EPCommand[CharCleanerConf] {
  import APIImplicits._;
  import TurnOrder.{ Entry, CustomEntry, TokenEntry };
  override def command = "epclean";
  override def options = (args) => new CharCleanerConf(args);
  override def apply(config: CharCleanerConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.reply("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      val updatedCharacters: List[(Character, List[String])] = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            if (config.egocast()) {
              cleanForUpload(char) match {
                case Left(l)  => Some(char -> l)
                case Right(l) => Some(char -> l)
              }
            } else if (config.backup()) {
              cleanForUpload(char) match {
                case Left(l) => {
                  val cname = char.name;
                  val cNameNoPre = if (cname.startsWith("(COPY) ")) {
                    cname.substring(7)
                  } else cname;
                  char.name = s"[${config.prefix()}] $cNameNoPre";
                  Some(char -> l)
                }
                case Right(l) => Some(char -> l)
              }
            } else {
              None
            }
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }
      };
      val updates = updatedCharacters.map(_ match {
        case (char, ups) => char.name + ups.mkString("<ul><li>", "</li><li>", "</li></ul>")
      }).mkString("<ul><li>", "</li><li>", "</li></ul>");
      debug(s"Updates: $updates");
      ctx.reply(s"Updated Characters $updates");
    }
  }

  private def cleanForUpload(char: Character): Either[List[String], List[String]] = {
    val curIdS = char.attribute(epmodel.currentMorph)();
    var messages = List.empty[String];
    if (curIdS != epmodel.currentMorph.defaultValue.get) {
      val curId = extractSimpleRowId(curIdS);
      char.repeatingAt(curId)(MorphSection.active) match {
        case Some(curActive) => {
          messages ::= s"Deactivated active morph."
          curActive.setWithWorker(false);
        }
        case None => messages ::= "Current morph was not marked active";
      }
    }
    // delete armour
    char.repeatingSection(ArmourItemSection.name).map(_.remove());
    // what the sheetworker would do
    char.attribute(epmodel.armourEnergyBonus) <<= 0;
    char.attribute(epmodel.armourKineticBonus) <<= 0;
    char.attribute(epmodel.layeringPenalty) <<= 0;
    char.attribute(epmodel.armourEnergyTotal) <<= 0;
    char.attribute(epmodel.armourKineticTotal) <<= 0;
    // delete other stuff
    char.repeatingSection(GearSection.name).map(_.remove());
    char.repeatingSection(MeleeWeaponSection.name).map(_.remove());
    char.repeatingSection(RangedWeaponSection.name).map(_.remove());
    // leave software!
    // remove damage and wounds
    char.attribute(epmodel.damage) <<= 0;
    char.attribute(epmodel.wounds).setWithWorker(0); // to update applied wounds and stuff

    messages ::= "Ok";
    Left(messages.reverse)
  }

}
