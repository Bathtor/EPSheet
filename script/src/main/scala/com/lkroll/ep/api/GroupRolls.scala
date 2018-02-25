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
import com.lkroll.ep.model.{ EPCharModel => epmodel }
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import concurrent.Future
import util.{ Try, Success, Failure }

object GroupRollsScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPGroupRollsCommand);
}

class EPGroupRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val mod = opt[Int]("mod", descr = "The value used to replace roll queries for modifiers");
  val ini = opt[Boolean]("ini", descr = "Roll Initiative.");
  val skill = opt[List[String]](
    "skill",
    descr = "Roll the provided Active Skill.")(
      ScallopUtils.singleListArgConverter(identity));
  val frayHalved = opt[Boolean]("fray-halved", descr = "Roll Fray/2.");
  requireOne(ini, skill, frayHalved);
  dependsOnAll(skill, List(mod));
  dependsOnAll(frayHalved, List(mod));
  verify();
}

object EPGroupRollsCommand extends APICommand[EPGroupRollsConf] {
  import APIImplicits._;
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
      val targets = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            debug(s"Token represents $char");
            EPScripts.checkVersion(char) match {
              case Right(()) => Some((token, char))
              case Left(msg) => ctx.reply(msg + " Skipping token."); None
            }
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }
      };
      if (config.ini()) {
        rollInitiative(targets, ctx);
      } else if (config.skill.isSupplied) {
        // TODO roll skill
      } else if (config.frayHalved()) {
        // TODO roll Fray/2
      } else {
        ctx.reply(s"No roll option selected!");
      }
    }
  }

  def rollInitiative(targets: List[(Token, Character)], ctx: ChatContext): Unit = {
    val campaign = Campaign();
    val resFutures = targets.map {
      case (token, char) => {
        val strippedIni = epmodel.iniRoll.formula match {
          case RollExprs.WithOption(expr, _) => expr.forCharacter(char.name) // drop the to-tracker option and target for character
          case _                             => ??? // this shouldn't happen unless there's a bug
        };

        rollViaChat(Rolls.SimpleRoll(strippedIni)).map(r => (token, char, r))
      }
    };
    val resFuture = Future.sequence(resFutures);
    resFuture.onComplete {
      case Success(res) => {
        campaign.turnOrder ++= res.map(t => (t._1 -> t._3));
        campaign.turnOrder.dedup();
        campaign.turnOrder.sort();
        val msg = "<h3>Rolled Initiative</h3>" +
          res.map(t => s"""<b>${t._2.name}</b>: [[${t._3}]] """)
          .mkString("<ul><li>", "</li><li>", "</li></ul>");
        ctx.reply(msg);
      }
      case Failure(e) => error(e); ctx.reply(s"Some rolls failed to complete.");
    }
  }
}
