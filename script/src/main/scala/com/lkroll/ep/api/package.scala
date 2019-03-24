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
package com.lkroll.ep

import com.lkroll.ep.compendium.ChatRenderable
import com.lkroll.roll20.core.{ Rolls, APIButton, RollExpression }
import com.lkroll.ep.model.{ EPCharModel => epmodel }
import com.lkroll.roll20.api._
import com.lkroll.roll20.api.templates._

package object api {

  /*
   * Re-exports
   */
  type Result[T] = com.lkroll.common.result.Result[T, String];
  val Result = com.lkroll.common.result.Result;
  val Ok = com.lkroll.common.result.Ok;
  val Err = com.lkroll.common.result.Err;

  /*
   * Implicits
   */

  implicit class EnhancedContext(ctx: ChatContext) {
    import com.lkroll.roll20.core._
    def withChars(f: List[Character] => Unit): Unit = {
      val graphicTokens = ctx.selected;
      if (graphicTokens.isEmpty) {
        ctx.reply("No tokens selected. Nothing to do...");
      } else {
        val tokens = graphicTokens.flatMap {
          case t: Token => Some(t)
          case c        => APILogger.debug(s"Ignoring non-Token $c"); None
        };
        val chars = tokens.flatMap { token =>
          APILogger.debug(s"Working on token: ${token.name} (${token.id})");
          val tO = token.represents;
          if (tO.isEmpty) {
            ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!");
          }
          tO
        }
        f(chars)
      }
    }

    def forChar[T](f: Character => T): List[T] = {
      val graphicTokens = ctx.selected;
      if (graphicTokens.isEmpty) {
        ctx.reply("No tokens selected. Nothing to do...");
        List.empty
      } else {
        val tokens = graphicTokens.flatMap {
          case t: Token => Some(t)
          case c        => APILogger.debug(s"Ignoring non-Token $c"); None
        };
        tokens.flatMap { token =>
          APILogger.debug(s"Working on token: ${token.name} (${token.id})");
          token.represents match {
            case Some(char) => Some(f(char))
            case None       => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
          }
        }
      }
    }
  }

  import TemplateImplicits._;

  def asInfoTemplate(
    title:        String,
    subtitle:     String,
    importButton: Option[APIButton],
    keys:         List[(String, String)],
    description:  String): TemplateApplication = {
    val t = templateV("title" -> title);
    val st = templateV("subtitle" -> subtitle);
    val ib = templateV("import" -> importButton);
    val d = templateV("description" -> description);
    val other = keys.map(templateV(_));
    val vars = t :: st :: ib :: d :: other;

    EPTemplates.info.fillWith(vars)
  }

  def asInfoTemplate(r: ChatRenderable): TemplateApplication = {
    asInfoTemplate(r.templateTitle, r.templateSubTitle, None, r.templateKV.toList.sortBy(_._1), r.templateDescr)
  }

  def asInfoTemplate(r: ChatRenderable, importButton: APIButton, buttons: (String, APIButton)*): TemplateApplication = {
    asInfoTemplate(r, importButton, buttons);
  }

  def asInfoTemplate(r: ChatRenderable, importButton: APIButton, buttons: Iterable[(String, APIButton)]): TemplateApplication = {
    val btns = buttons.map({
      case (k, v) => (k -> v.render)
    }).toMap;
    val rkv = r.templateKV;
    val kv = (rkv ++ btns).toList.sortBy(_._1); // override original fields with same name buttons
    asInfoTemplate(r.templateTitle, r.templateSubTitle, Some(importButton), kv, r.templateDescr)
  }

  def asDefaultTemplate(character: String, attributeField: String, attributeSubField: Option[String] = None,
                        testRoll: Rolls.InlineRoll[Int], testTarget: Rolls.InlineRoll[Int],
                        testMoF: Option[Rolls.InlineRoll[Int]] = None): TemplateApplication = {
    val char = templateV("character" -> character);
    val field = templateV("attribute-field" -> attributeField);
    val subField = templateV("attribute-subfield" -> attributeSubField);
    val roll = templateV("test-roll" -> testRoll);
    val target = templateV("test-target" -> testTarget);
    val mof = templateV("test-mof" -> testMoF);
    EPTemplates.default.fillWith(char, field, subField, roll, target, mof)
  }

  def asDamageTemplate(character: String, attributeField: String,
                       damageRoll: Rolls.InlineRoll[Int], damageType: String,
                       armourPenetration: Int): TemplateApplication = {
    val char = templateV("character" -> character);
    val field = templateV("attribute-field" -> attributeField);
    val roll = templateV("damage-roll" -> damageRoll);
    val dType = templateV("damage-type" -> damageType);
    val ap = templateV("armour-penetration" -> armourPenetration);
    EPTemplates.damage.fillWith(char, field, roll, dType, ap)
  }

  //  private def insertRolls(data: List[(String, String)]): List[(String, String)] = data.map {
  //    case ("Damage", rollS)    => {
  //      val c = SpecialRollsCommand.minConf;
  //      SpecialRollsCommand.invoke(rollS, args)
  //    }
  //    case ("Skill", skillName) => {
  //      ("Skill" -> API)
  //    }
  //    case t                    => t
  //  }
}
