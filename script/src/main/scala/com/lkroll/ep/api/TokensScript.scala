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
import scalajs.js
import scalajs.js.JSON
//import fastparse.all._
import util.{ Try, Success, Failure }
import com.lkroll.ep.model.ActiveSkillSection

object TokensScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPTokensCommand);
}

case class AbilityTemplate(name: String, action: Character => Option[String], tokenAction: Boolean = true) {
  def invocation(char: Character): String = s"%{${char.name}|$name}";
}

object SupportedAbilities {
  private def actionFromSkill(skillName: String): Character => Option[String] = {
    val f: Character => Option[String] = char => {
      val res: List[FieldAttribute[String]] = char.repeating(ActiveSkillSection.skillName);
      res.find(a => a.current.equals(skillName)) match {
        case Some(a) => {
          val Some(rowId) = a.getRowId;
          Some(s"%{${char.name}|repeating_activeskills_${rowId}_activeskills_active_skill_roll}")
        }
        case None => APILogger.warn(s"Skill ${skillName} could not be found for character ${char.name}"); None
      }
    };
    f
  }

  def fromSkill(skillName: String): AbilityTemplate = AbilityTemplate(skillName, actionFromSkill(skillName));

  val ini = AbilityTemplate("Initiative", char => Some(s"%{${char.name}|initiative-roll}"));
  val fray = AbilityTemplate("Fray", actionFromSkill("Fray"));
  val frayHalved = AbilityTemplate("Fray/2", char => Some(s"%{${char.name}|fray-halved-roll}"));
}

class EPTokensConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val clear = opt[Boolean]("clear", descr = "Remove ALL character abilities.");
  val force = opt[Boolean]("force", descr = "Override existing abilities.");
  val ini = opt[Boolean]("ini", descr = "Add Initiative as a token action");
  val fray = opt[Boolean]("fray", descr = "Add Fray as a token action");
  val frayHalved = opt[Boolean]("fray-halved", descr = "Add Fray/2 as a token action");
  val skill = opt[List[String]](
    "skill",
    descr = "Add the provided Active Skill as a token action. (Can be specified multiple times)")(
      ScallopUtils.singleListArgConverter(identity));
  verify();
}

object EPTokensCommand extends APICommand[EPTokensConf] {
  import CoreImplicits._;
  override def command = "eptoken";
  override def options = (args) => new EPTokensConf(args);
  override def apply(config: EPTokensConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.reply("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      val updatedCharacters = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            var updates = List.empty[String];
            debug(s"Token represents $char");
            if (config.clear()) {
              val existing = char.abilities;
              debug(s"Found existing abilities to be removed for char=${char.name}:\n${existing.mkString("\n");}", true);
              existing.foreach(_.remove());
              updates ::= config.clear.name;
            }
            if (config.ini()) {
              if (createAbility(SupportedAbilities.ini, char, config.force())) {
                updates ::= config.ini.name;
              }
            }
            if (config.fray()) {
              if (createAbility(SupportedAbilities.fray, char, config.force())) {
                updates ::= config.fray.name;
              }
            }
            if (config.frayHalved()) {
              if (createAbility(SupportedAbilities.frayHalved, char, config.force())) {
                updates ::= config.frayHalved.name;
              }
            }
            if (config.skill.isSupplied) {
              //              if (createAbility(SupportedAbilities.fromSkill(config.skill()), char, config.force())) {
              //                updates ::= config.skill.name + s"(${config.skill()})";
              //              }
              config.skill().foreach { skillName =>
                if (createAbility(SupportedAbilities.fromSkill(skillName), char, config.force())) {
                  updates ::= config.skill.name + s"($skillName)";
                }
              }
            }
            Some(char.name -> updates)
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }

      };
      val updates = updatedCharacters.map(_ match {
        case (char, ups) => char + ups.mkString("<ul><li>", "</li><li>", "</li></ul>")
      }).mkString("<ul><li>", "</li><li>", "</li></ul>");
      debug(s"Updates: $updates")
      ctx.reply(s"Updated Characters $updates");
    }
  }

  private def createAbility(ability: AbilityTemplate, char: Character, force: Boolean): Boolean = {
    val existing = char.abilitiesForName(ability.name);
    debug(s"Found existing abilities for char=${char.name}:\n${existing.mkString("\n");}", true);
    if (!existing.isEmpty) {
      if (force) {
        info(s"Deleting ${ability.name} for ${char.name} as specified by flag --force.");
        existing.foreach(_.remove());
      } else {
        info(s"Skipping ${ability.name} for ${char.name} as it exists.");
        return false;
      }
    }
    ability.action(char) match {
      case Some(ac) => {
        val ab = Ability.create(char.id, ability.name);
        ab.action = ac;
        ab.isTokenAction = ability.tokenAction;
        debug(s"Created ability $ab");
        true
      }
      case None => false
    }

  }
}
