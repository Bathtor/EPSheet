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
import fastparse.all._
import util.{ Try, Success, Failure }

object TokensScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPTokensCommand);
}

case class AbilityTemplate(name: String, action: Character => String, tokenAction: Boolean = true) {
  def invocation(char: Character): String = s"%{${char.name}|$name}";
}
object SupportedAbilities {
  val ini = AbilityTemplate("Initiative", char => s"%{${char.name}|initiative-roll}");
  // TODO val fray = AbilityTemplate("Fray", char => s"%{${char.name}|fray-halved-roll}");
  val frayHalved = AbilityTemplate("Fray/2", char => s"%{${char.name}|fray-halved-roll}");
}

class EPTokensConf(args: Seq[String]) extends ScallopAPIConf(args) {
  import org.rogach.scallop.singleArgConverter;

  val force = opt[Boolean]("force", descr = "Override existing abilities.");
  val ini = opt[Boolean]("ini");
  //val fray = opt[Boolean]("fray");
  val frayHalved = opt[Boolean]("fray-halved");
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
            debug(s"Token represents $char");
            if (config.ini()) {
              createAbility(SupportedAbilities.ini, char, config.force());
            }
            if (config.frayHalved()) {
              createAbility(SupportedAbilities.frayHalved, char, config.force());
            }
            Some(char.name)
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }

      };
      val updates = updatedCharacters.mkString("<ul><li>", "</li><li>", "</li><ul>");
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
    val ab = Ability.create(char.id, ability.name);
    ab.action = ability.action(char);
    ab.isTokenAction = ability.tokenAction;
    debug(s"Created ability $ab");
    return true;
  }
}
