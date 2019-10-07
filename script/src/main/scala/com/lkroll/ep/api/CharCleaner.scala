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
import com.lkroll.ep.model.{
  EPCharModel => epmodel,
  MorphSection,
  GearSection,
  ArmourItemSection,
  MeleeWeaponSection,
  RangedWeaponSection
}
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.collection.mutable

object CharCleanerScript extends EPScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(CharCleanerCommand);
}

class CharCleanerConf(args: Seq[String]) extends ScallopAPIConf(args) {
  version(s"${CharCleanerCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Remove invalid EP sheet fields after Egocast/Backup.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");
  val egocast = opt[Boolean]("egocast", descr = "Remove everything not taken along on an Ego Cast from the sheet.");
  val backup = opt[Boolean]("backup", descr = "Remove everything not taken along during an Ego Backup from the sheet.");
  val prefix = opt[String]("prefix",
                           descr = "&lt;param&gt; will be prefixed to the sheet name (default: BACKUP)",
                           default = Some("BACKUP"))(ScallopUtils.singleArgSpacedConverter(identity));

  requireOne(egocast, backup);
  dependsOnAll(prefix, List(backup));
  verify();
}

object CharCleanerCommand extends EPCommand[CharCleanerConf] {
  import APIImplicits._;
  import scalatags.Text.all._;

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
      val updatedCharacters: List[(Character, Future[List[String]])] = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            if (config.egocast()) {
              Some(char -> cleanForUpload(char))
            } else if (config.backup()) {
              val f = cleanForUpload(char);
              val renamedF = f.map { msgs =>
                val cname = char.name;
                val cNameNoPre = if (cname.startsWith("(COPY) ")) {
                  cname.substring(7)
                } else cname;
                char.name = s"[${config.prefix()}] $cNameNoPre";
                s"Renamed sheet to ${char.name}" :: msgs
              };
              Some(char -> renamedF)
            } else {
              None
            }
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }
      };
      val updates = ul(for ((char, _) <- updatedCharacters) yield li(b(char.name)));
      debug(s"Updates: ${updates.render}");
      val msg = div(h4("Updating Characters"), p(updates));
      val cleanType = if (config.egocast()) {
        "Egocast"
      } else if (config.backup()) {
        "Backup"
      } else {
        "???"
      };
      ctx.replyHeader(s"Character Cleaner - ${cleanType}", msg);
      val partials: List[Future[Unit]] = for ((char, upsF) <- updatedCharacters)
        yield
          upsF.map { msgs =>
            val resp = div(h4(s"Finished ${char.name}"), p(ul(for (up <- msgs.reverse) yield li(up))));
            ctx.replyBody(resp);
          };
      val partialF = Future.sequence(partials);
      partialF.onComplete {
        case Success(_) => ctx.replyFooter(h4("All done!"))
        case Failure(e) =>
          error(e); ctx.replyError("An error occurred during execution of a task. Please consult the log for details.")
      }
    }
  }

  private def cleanForUpload(char: Character): Future[List[String]] = {
    val curIdS = char.attribute(epmodel.currentMorph)();
    val morphF: Future[List[String]] = if (curIdS != epmodel.currentMorph.defaultValue.get) {
      val curId = extractSimpleRowId(curIdS);
      char.repeatingAt(curId)(MorphSection.active) match {
        case Some(curActive) => {
          curActive.setWithWorker(false).map(_ => List("Deactivated active morph."))
        }
        case None => Future.successful(List("Current morph was not marked active."))
      }
    } else {
      Future.successful(List("No morph was active."))
    };
    morphF.flatMap { morphMessages =>
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
      val f = char.attribute(epmodel.wounds).setWithWorker(0); // to update applied wounds and stuff

      val messages = "Ok" :: morphMessages;
      f.map(_ => messages)
    }
  }

}
