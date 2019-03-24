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
import com.lkroll.ep.model.{ EPCharModel => epmodel, ActiveSkillSection }
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import concurrent.Future
import util.{ Try, Success, Failure }

object GroupRollsScript extends EPScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPGroupRollsCommand);
}

class EPGroupRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {

  version(s"${EPGroupRollsCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Roll for multiple tokens at once.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");

  val mod = opt[Int]("mod", descr = "The value used to replace roll queries for modifiers (default: 0).", default = Some(0));
  val ini = opt[Boolean]("ini", descr = "Roll Initiative.");
  val skill = opt[List[String]](
    "skill",
    descr = "Roll the provided Active Skill.")(
      ScallopUtils.singleListArgConverter(identity));
  val frayHalved = opt[Boolean]("fray-halved", descr = "Roll Fray/2.");
  requireOne(ini, skill, frayHalved);
  dependsOnAny(mod, List(skill, frayHalved));
  verify();
}

object EPGroupRollsCommand extends EPCommand[EPGroupRollsConf] {
  import APIImplicits._;
  import scalatags.Text.all._;

  lazy val frayName = "Fray"; // TODO pull from translation or from sheet field

  override def command = "epgroup-roll";
  override def options = (args) => new EPGroupRollsConf(args);
  override def apply(config: EPGroupRollsConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.replyWarn("No tokens selected. Nothing to do...");
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
              case Ok(_)    => Some((token, char))
              case Err(msg) => ctx.replyWarn(msg + " Skipping token."); None
            }
          }
          case None => ctx.replyWarn(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }
      };
      if (config.ini()) {
        rollInitiativeAndAddToTracker(targets, ctx);
      } else if (config.skill.isSupplied) {
        config.skill().foreach { skill =>
          rollSkill(targets, skill, config.mod(), ctx)
        }
      } else if (config.frayHalved()) {
        rollFrayHalved(targets, config.mod(), ctx)
      } else {
        ctx.replyWarn(s"No roll option selected!");
      }
    }
  }

  type RollConsumer = Try[List[(Token, Character, Int)]] => Unit;

  def rollInitiative(targets: List[(Token, Character)])(f: RollConsumer): Unit = {
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
    resFuture.onComplete(f);
  }

  def rollInitiativeAndAddToTracker(targets: List[(Token, Character)], ctx: ChatContext): Unit = {
    val f: RollConsumer = {
      case Success(res) => {
        val campaign = Campaign();
        campaign.turnOrder ++= res.map(t => (t._1 -> t._3));
        campaign.turnOrder.dedup();
        campaign.turnOrder.sortDesc();
        val msg = div(
          h4("Participants"),
          ul(for ((_, char, roll) <- res) yield li(b(char.name), ": ", s"[[$roll]]")));
        ctx.reply("Rolled Initiative", msg);
      }
      case Failure(e) => {
        error(e);
        ctx.replyError(s"Some rolls failed to complete. See log for details.");
      }
    };
    rollInitiative(targets)(f)
  }

  def rollSkill(targets: List[(Token, Character)], skillName: String, mod: Int, ctx: ChatContext): Unit = {
    val rollTarget: Character => Int = (char: Character) => {
      val cache = compendium.ImportCache(char);
      val skillValue: Int = cache.activeSkillId(skillName) match {
        case Some(rowId) => (char.repeatingAt(rowId)(ActiveSkillSection.total)).map(_.getOrDefault).getOrElse(0)
        case None => {
          // TODO try to lookup skill's associated aptitude in compendium, maybe?
          ctx.replyWarn(p("Could not find skill ", em(skillName), " for ", b(char.name), ". Rolling against 0."));
          0
        }
      };
      // TODO check skill type to determine appropriate mods
      val globalPhysicalValue = globalPhysicalMods(char);
      skillValue + globalPhysicalValue + mod
    };
    rollSuccess(targets, s"Group Roll $skillName", rollTarget, ctx)
  }

  def rollFrayHalved(targets: List[(Token, Character)], mod: Int, ctx: ChatContext): Unit = {
    val rollTarget: Character => Int = (char: Character) => {
      val cache = compendium.ImportCache(char);
      val frayValue: Int = (cache.activeSkillId(frayName) match {
        case Some(rowId) => (char.repeatingAt(rowId)(ActiveSkillSection.total)).map(_.getOrDefault)
        case None        => None
      }).getOrElse(char.attribute(epmodel.refTotal).getOrDefault);
      val globalPhysicalValue = globalPhysicalMods(char);
      frayValue / 2 + globalPhysicalValue + mod
    };
    rollSuccess(targets, "Group Roll Fray/2", rollTarget, ctx)
  }

  def woundTraumaMods(char: Character): Int = {
    val traumaMod: Int = char.attribute(epmodel.traumaMod).getOrDefault;
    val woundMod: Int = char.attribute(epmodel.woundMod).getOrDefault;
    traumaMod + woundMod
  }
  def globalMods(char: Character): Int = {
    val miscActionMod: Int = char.attribute(epmodel.miscActionMod).getOrDefault;
    val psiSustainedMod: Int = char.attribute(epmodel.psiSustainedMod).getOrDefault;
    miscActionMod - woundTraumaMods(char) + psiSustainedMod
  }
  def globalPhysicalMods(char: Character): Int = {
    val miscPhysicalMod: Int = char.attribute(epmodel.miscPhysicalMod).getOrDefault;
    val layeringPenalty: Int = char.attribute(epmodel.layeringPenalty).getOrDefault;
    globalMods(char) + miscPhysicalMod + layeringPenalty
  }

  def rollEPRoll(targets: List[(Token, Character)])(f: RollConsumer): Unit = {
    val resFutures = targets.map {
      case (token, char) => {
        val roll = Rolls.SimpleRoll(epmodel.epRoll.formula);
        rollViaChat(roll).map(r => (token, char, r))
      }
    };
    val resFuture = Future.sequence(resFutures);
    resFuture.onComplete(f);
  }

  def rollSuccess(targets: List[(Token, Character)], title: String, rollTarget: Character => Int, ctx: ChatContext): Unit = {
    rollEPRoll(targets) {
      case Success(res) => {
        val resOut = res.map {
          case (token, char, dieRoll) => {
            debug(s"Got die result of $dieRoll for ${char.name}");
            val target = rollTarget(char);
            val isCrit = RollsScript.isCritical(dieRoll);
            val isAutoSuccess = dieRoll == 0;
            val isAutoFailure = dieRoll == 99;
            val isSuccess = !isAutoFailure && ((dieRoll <= target) || isAutoSuccess);
            val isFailure = !isSuccess;
            val mof = dieRoll - target;
            val mos = dieRoll;

            val rollRes = p(b(char.name), s": [[${dieRoll}]] vs [[${target}]]");
            val rollJudgement = if (isAutoSuccess) {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-success sheet-result-indent",
                  "Roll is an automatic success! » MoS [[0]]"))
            } else if (isAutoFailure) {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-failure sheet-result-indent",
                  s"Roll is an automatic failure... » MoF [[${mof}]]"))
            } else if (isSuccess && isCrit) {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-success sheet-result-indent",
                  s"Roll is a critical success! » MoS [[${mos}]]"))
            } else if (isFailure && isCrit) {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-failure sheet-result-indent",
                  s"Roll is a critical failure... » MoF [[${mof}]]"))
            } else if (isSuccess) {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-success sheet-result-indent",
                  s"Roll is a success! » MoS [[${mos}]]"))
            } else {
              div(
                rollRes,
                p(
                  cls := "sheet-roll-failure sheet-result-indent",
                  s"Roll is a failure... » MoF [[${mof}]]"))
            };
            (token, char, rollJudgement)
          }
        };
        val msg = div(
          h4("Participants"),
          div(for ((_, _, judge) <- resOut) yield judge));
        ctx.reply(title, msg)
      }
      case Failure(e) => {
        error(e);
        ctx.replyError(s"Some rolls failed to complete. See log for details.");
      }
    }
  }
}
