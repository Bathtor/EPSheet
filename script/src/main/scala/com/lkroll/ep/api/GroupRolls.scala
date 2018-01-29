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

// TODO On ice for now...Must support RollQueries to be useful

object GroupRollsScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPGroupRollsCommand);
}

class EPGroupRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {

  //val ini = opt[Boolean]("ini");
  //val fray = opt[Boolean]("fray");
  val frayHalved = opt[Boolean]("fray-halved");
  requireOne(frayHalved);
  verify();
}

object EPGroupRollsCommand extends APICommand[EPGroupRollsConf] {
  import CoreImplicits._;
  override def command = "epgroup-roll";
  override def options = (args) => new EPGroupRollsConf(args);
  override def apply(config: EPGroupRollsConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.reply("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      tokens.foreach { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            debug(s"Token represents $char");
            //            if (config.ini()) {
            //              rollAbility(ctx, SupportedAbilities.ini, char);
            //            }
            if (config.frayHalved()) {
              rollAbility(ctx, SupportedAbilities.frayHalved, char);
            }
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!")
        }
      };
    }
  }

  private def rollAbility(ctx: ChatContext, ability: AbilityTemplate, char: Character): Unit = {
    val existing = char.abilitiesForName(ability.name);
    val msg = if (existing.isEmpty) {
      ability.action(char)
    } else {
      ability.action(char)
      //ability.invocation(char)
    }; //.replace("/", "");
    debug(s"About to send message '$msg'");
    sendChat(ctx.player, msg);
  }
}
