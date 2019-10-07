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
import com.lkroll.ep.model.{EPCharModel => epmodel}
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.collection.mutable
import scalatags.Text.all._

object BattleManagerScript extends EPScript {
  import APIImplicits._;
  override def apiCommands: Seq[APICommand[_]] = Seq(EPBattlemanCommand);

  val minConf = new EPBattlemanConf(Seq("--start"));

  onChange(
    "campaign:turnorder", { (_, _) =>
      EPBattlemanCommand.state match {
        case EPBattlemanCommand.Active(_, _, _) => {
          val conf = minConf
          val resetButton = EPBattlemanCommand.invoke("reset", List(conf.reset <<= true)).render;
          sendChatWarning(
            "Battleman (API)",
            Chat.GM.htmlMessage(
              div(
                p(
                  "It is not recommended to manually change the turn order, while a battle is active in the Battle Manager.",
                  "You might end up in an inconsistent state!"
                ),
                p("Do you wish to ", raw(resetButton), " the state?")
              )
            )
          );
        }
        case _ => (), // that's ok
      }
    }
  );

}

class EPBattlemanConf(args: Seq[String]) extends ScallopAPIConf(args) {
  version(s"${EPBattlemanCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Manage EP battles efficiently.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");
  val start = opt[Boolean]("start", descr = "Start a battle with all the characters currently in the turn order.");
  val next = opt[Boolean]("next", descr = "Move to the next character, accounting for phases and turns.");
  val end = opt[Boolean]("end", descr = "Ends a battle by clearing the turn order and internal state.");
  val add = opt[Boolean](
    "add",
    descr =
      "Adds the currently selected tokens to the turn order and battleman. Use this while a battle is active, instead of manually adding tokens to the turn order."
  );
  val drop = opt[Boolean](
    "drop",
    descr =
      "Removes the currently selected tokens from the turn order and battleman. Use this while a battle is active, instead of manually removing tokens from the turn order."
  );
  val reset = opt[Boolean]("reset", descr = "Reset turn order to last valid state.");

  requireOne(start, next, end, add, drop, reset);
  verify();
}

object EPBattlemanCommand extends EPCommand[EPBattlemanConf] {
  import APIImplicits._;
  import TurnOrder.{CustomEntry, Entry, TokenEntry};
  override def command = "epbattleman";
  override def options = (args) => new EPBattlemanConf(args);
  override def apply(config: EPBattlemanConf, ctx: ChatContext): Unit = {
    if (config.start()) {
      onStart(ctx)
    } else if (config.next()) {
      onNext(ctx)
    } else if (config.end()) {
      onEnd(ctx)
    } else if (config.add()) {
      onAdd(ctx)
    } else if (config.drop()) {
      onDrop(ctx)
    } else if (config.reset()) {
      onReset(ctx)
    } else {
      error(s"Unsupported options supplied: ${config.args}");
    }
  }

  sealed trait State;
  object Inactive extends State;
  case class Active(round: Int, phase: Int, lastState: List[TurnOrder.Entry]) extends State;

  lazy val campaign = Campaign();
  lazy val turnOrder = campaign.turnOrder;

  private[api] var state: State = Inactive;
  private val participants = mutable.Map.empty[String, (Token, Character)]; // token id -> token & represents
  private val iniCache = mutable.Map.empty[String, Int]; // token id -> ini mod

  private def onStart(ctx: ChatContext) {
    state match {
      case Inactive => {
        var sorting = List.empty[(Int, Entry)];
        val order = turnOrder.get();
        state = Active(1, 0, order);
        order.foreach {
          case e @ CustomEntry(_, Left(ini)) => {
            sorting ::= (ini -> e);
          }
          case e @ TokenEntry(id, Left(ini)) => {
            Graphic.get(id) match {
              case Some(token: Token) => {
                token.represents match {
                  case Some(char) => {
                    participants += (id -> (token, char));
                    iniCache += (id -> getIniMod(char));
                    sorting ::= (ini -> e);
                  }
                  case None => {
                    ctx.replyWarn(
                      s"Token ${token.name}(${token.id}) does not represent any character. Unlinked tokens are currently not supported."
                    );
                  }
                }
              }
              case Some(_: Card) => {
                ctx.replyWarn(s"Entry with id=${id} was a card, but we require a token!");
              }
              case None => {
                ctx.replyWarn(s"Could not find token for id=${id}!");
              }
            }
          }
          case e => {
            ctx.replyWarn(s"Got unexpected token $e! Ignoring.");
          }
        }
        if (!sorting.isEmpty) {
          val sorted = sorting.sortBy(_._1)(Ordering[Int].reverse);
          val maxEntry = sorted.head._1 + 1;
          val newOrder = marker(maxEntry) :: sorted.map(_._2);
          turnOrder.set(newOrder);
          updateLastState(Some(newOrder));
          val part = ul(
            for ((t, c) <- participants.values.toSeq) yield li(b(c.name))
          );
          val reply = div(h4("Battle Participants"), p(part));
          ctx.reply("Battle Started!", reply);
        } else {
          ctx.replyWarn("There is no one to start a battle with :(");
        }
      }
      case _ => {
        ctx.replyWarn("Can't start new battle, as there is already an ongoing battle.");
      }
    }
  }

  private def getIniMod(c: Character): Int = {
    val ini = c.attribute(epmodel.initiative).getOrDefault;
    val iniRaw = c.attribute(epmodel.initiative).current;
    val wounds = c.attribute(epmodel.woundsApplied).getOrDefault;
    val woundsRaw = c.attribute(epmodel.woundsApplied).current;
    debug(s"Got raw ini=${iniRaw} and wounds_applied=${woundsRaw} for ${c.name}");
    val traumas = c.attribute(epmodel.trauma).getOrDefault;
    val misc = c.attribute(epmodel.miscInitiativeMod).getOrDefault;
    val iniMod = ini - wounds - traumas + misc;
    debug(s"Got iniMod=${iniMod}=${ini}-${wounds}-${traumas}+$misc for ${c.name}");
    iniMod
  }

  private def onNext(ctx: ChatContext) {
    state match {
      case Active(round, phase, _) => {
        val order = turnOrder.get();
        order match {
          case head :: rest => {
            head match {
              case e @ CustomEntry(name, Left(ini)) => {
                if (name.startsWith("|")) { // its our turn marker (probably^^)
                  if (rest.isEmpty) { // next turn
                    startNewTurn();
                  } else { // next phase
                    val newOrder = rest ++ List(marker(ini));
                    turnOrder.set(newOrder);
                    state = Active(round, phase + 1, newOrder);
                  }
                } else { // something else, just drop it
                  debug(s"Dropping custom entry $e");
                  turnOrder.set(rest);
                  updateLastState(Some(rest));
                }
              }
              case e @ TokenEntry(id, Left(ini)) => {
                participants.get(id) match {
                  case Some((_, c)) => {
                    val newOrder = if (shouldReschedule(c, phase)) {
                      validateAllInis(rest ++ List(e))
                    } else {
                      validateAllInis(rest)
                    };
                    turnOrder.set(newOrder);
                    updateLastState(Some(newOrder));
                  }
                  case None => {
                    debug(s"Token is not a participant: $id. Dropping.");
                    turnOrder.set(rest);
                    updateLastState(Some(rest));
                  }
                }
              }
              case e => {
                debug(s"Dropping unsupported entry $e");
                turnOrder.set(rest);
                updateLastState(Some(rest));
              }
            }
          }
          case Nil => {
            startNewTurn();
          }
        }
      }
      case _ => {
        debug("Ignoring Battleman.next as no battle is active.");
      }
    }
  }

  private def startNewTurn(): Unit = {
    debug("Starting new turn...");
    EPGroupRollsCommand.rollInitiative(participants.values.toList) {
      case Success(res) => {
        state = state match {
          case Active(round, phase, s) => Active(round + 1, 0, s)
          case s                       => error("Battleman is in an invalid state!"); s
        };
        var sorting = List.empty[(Int, Entry)];
        res.foreach {
          case (token, character, ini) => {
            val e = TokenEntry(token.id, Left(ini));
            sorting ::= (ini -> e);
          }
        }
        if (!sorting.isEmpty) {
          val sorted = sorting.sortBy(_._1)(Ordering[Int].reverse);
          val maxEntry = sorted.head._1 + 1;
          val newOrder = marker(maxEntry) :: sorted.map(_._2);
          turnOrder.set(newOrder);
          updateLastState(Some(newOrder));
          info("New turn is beginning.");
        } else {
          error("There is no one to continue the battle with :(");
        }
      }
      case Failure(f) => error(f)
    }
  }

  private def validateAllInis(order: List[Entry]): List[Entry] = {
    val changes = mutable.Map.empty[String, Int]; // token id -> ini diff
    participants.foreach {
      case (id, (token, char)) => {
        val cur = iniCache(id);
        val should = getIniMod(char);
        if (cur != should) {
          iniCache(id) = should;
          val diff = should - cur;
          debug(s"${char.name}'s ini differs by $diff");
          changes += (id -> diff);
        }
      }
    }
    if (changes.isEmpty) {
      debug("No changes after ini validation.");
      order
    } else {
      debug(s"Processing ${changes.size} changes after ini validation.");
      var sortingPre = List.empty[(Int, Entry)];
      var sortingPost = List.empty[(Int, Entry)];

      var preMarker = true;
      order.foreach {
        case e @ CustomEntry(name, _) => {
          if (name.startsWith("|")) { // it's probably our marker
            preMarker = false;
          } else {
            debug(s"Dropping custom entry $e during sorting.");
          }
        }
        case e @ TokenEntry(id, Left(ini)) => {
          changes.get(id) match {
            case Some(diff) => {
              val newIni = ini + diff;
              val sEntry = (newIni -> TokenEntry(id, Left(newIni)));
              if (preMarker) {
                sortingPre ::= sEntry;
              } else {
                sortingPost ::= sEntry;
              }
            }
            case None =>
              if (preMarker) {
                sortingPre ::= (ini -> e);
              } else {
                sortingPost ::= (ini -> e)
              };
          }
        }
        case e => debug(s"Dropping entry $e during sorting.");
      }
      assembleWithMarker(sortingPre, sortingPost)
    }
  }

  private def shouldReschedule(c: Character, phase: Int): Boolean = {
    val speed = c.attribute(epmodel.speed).getOrDefault;
    speed > phase
  }

  private def onEnd(ctx: ChatContext) {
    state match {
      case Active(round, phase, _) => {
        state = Inactive;
        turnOrder.clear();
        participants.clear();
        iniCache.clear();
        val time = round * 3;
        ctx.reply("Battle Ended!", s"The battle concluded in ${round} rounds (${time}s) and ${phase} phases.");
      }
      case _ => {
        ctx.replyWarn("There is no currently active battle to end.");
      }
    }
  }

  private def marker(ini: Int): TurnOrder.CustomEntry = {
    val s = state match {
      case Active(round, phase, _) => s"|Round ${round}|Phase ${phase + 1}|"
      case Inactive                => "|Inactive|"
    };
    TurnOrder.CustomEntry(s, Left(ini))
  }

  private def onAdd(ctx: ChatContext) {
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
      debug(s"Adding ${targets.size} tokens to battleman.");
      EPGroupRollsCommand.rollInitiative(targets) {
        case Success(res) => {
          res.foreach {
            case (token, character, _) => {
              participants += (token.id -> (token, character));
              iniCache += (token.id -> getIniMod(character));
            }
          }
          // put all new entries before the marker, since they should act this phase
          var sortingPre: List[(Int, Entry)] = res.map {
            case (token, character, ini) => {
              (ini -> TokenEntry(token.id, Left(ini)))
            }
          };
          var sortingPost = List.empty[(Int, Entry)];

          var preMarker = true;
          turnOrder.get().foreach {
            case e @ CustomEntry(name, _) => {
              if (name.startsWith("|")) { // it's probably our marker
                preMarker = false;
              } else {
                debug(s"Dropping custom entry $e during sorting.");
              }
            }
            case e @ TokenEntry(id, Left(ini)) => {
              if (preMarker) {
                sortingPre ::= (ini -> e);
              } else {
                sortingPost ::= (ini -> e)
              }
            }
            case e => debug(s"Dropping entry $e during sorting.");
          }
          val newOrder = assembleWithMarker(sortingPre, sortingPost);
          turnOrder.set(newOrder);
          updateLastState(Some(newOrder));
          val part = ul(
            for ((t, c, i) <- res) yield li(b(c.name))
          );
          val reply = div(h4("New Battle Participants"), p(part));
          ctx.reply("Battle Updated!", reply);
        }
        case Failure(f) =>
          error(f); ctx.replyError("Tokens could not be added to battleman. See log for error messages.")
      }
    }
  }

  private def assembleWithMarker(sortingPre: List[(Int, Entry)], sortingPost: List[(Int, Entry)]): List[Entry] = {
    (sortingPre.isEmpty, sortingPost.isEmpty) match {
      case (true, true) => List.empty
      case (true, false) => {
        val sorted = sortingPost.sortBy(_._1)(Ordering[Int].reverse);
        val maxEntry = sorted.head._1 + 1;
        marker(maxEntry) :: sorted.map(_._2);
      }
      case (false, true) => {
        val sorted = sortingPre.sortBy(_._1)(Ordering[Int].reverse);
        val maxEntry = sorted.head._1 + 1;
        sorted.map(_._2) ++ List(marker(maxEntry))
      }
      case (false, false) => {
        val sortedPost = sortingPost.sortBy(_._1)(Ordering[Int].reverse);
        val sortedPre = sortingPre.sortBy(_._1)(Ordering[Int].reverse);
        val maxEntry = Math.max(sortedPost.head._1, sortedPre.head._1) + 1;
        val pre = sortedPre.map(_._2);
        val post = sortedPost.map(_._2);
        pre ++ (marker(maxEntry) :: post)
      }
    }
  }

  private def onDrop(ctx: ChatContext) {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.replyWarn("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      val ids = tokens.map { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        participants.remove(token.id);
        iniCache.remove(token.id);
        token.id
      }.toSet;
      turnOrder.modify(_.filterNot {
        case TokenEntry(id, _) => ids.contains(id)
        case _                 => false
      });
      updateLastState();
      ctx.reply("Battle Updated!", s"Removed ${ids.size} tokens from battleman.");
    }
  }

  private def onReset(ctx: ChatContext) {
    state match {
      case Active(_, _, lastState) => {
        turnOrder.set(lastState);
        ctx.reply("Battle Reset!", "Reset battle to last valid state.");
      }
      case Inactive => {
        ctx.replyWarn("No active battle to reset!");
      }
    }
  }

  private def updateLastState(newOrder: Option[List[TurnOrder.Entry]] = None): Unit = {
    state match {
      case a: Active => {
        state = a.copy(lastState = newOrder.getOrElse(turnOrder.get()))
      }
      case Inactive => ()
    }
  }
}
