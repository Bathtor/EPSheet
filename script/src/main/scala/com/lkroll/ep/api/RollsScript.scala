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

object RollsScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPRollsCommand);
}

class EPRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  import org.rogach.scallop.singleArgConverter;

  val output = opt[String]("output", descr = "who should receive the final output");
  val variables = trailArg[TemplateVars]("variables")(TemplateVars);
  verify();
}

object EPRollsCommand extends APICommand[EPRollsConf] {
  import CoreImplicits._;

  override def command = "eproll";
  override def options = (args) => new EPRollsConf(args);
  override def apply(config: EPRollsConf, ctx: ChatContext): Unit = {
    val target = if (config.output.isSupplied) {
      Chat.Whisper(config.output())
    } else {
      Chat.Default
    };
    debug(s"Got roll ${ctx.raw.content}");
    //log(JSON.stringify(ctx.raw), true);
    if (config.variables.isSupplied) {
      val vars = config.variables();
      ctx.rollTemplate match {
        case Some("ep-default") => {
          var testTarget, testRoll: Option[Int] = None;
          val replacedVars = vars.replaceInlineRollRefs(ctx.inlineRolls, (ir, tvar) => {
            Try(ir.results.total.toInt) match {
              case Success(total) => {
                tvar.key match {
                  case "test-roll"   => { testRoll = Some(total); transformRoll(total) }
                  case "test-target" => { testTarget = Some(total); transformTarget(ir.expression) }
                  case _             => TemplateVal.InlineRoll(total)
                }
              }
              case Failure(e) => error(e); TemplateVal.Empty
            }
          });
          val mofO = for {
            targetValue <- testTarget;
            rollValue <- testRoll
          } yield {
            val roll = numToExpr(rollValue);
            val target = numToExpr(targetValue);
            TemplateVar("test-mof", TemplateVal.InlineRoll(fakeRoll(roll - target).label("roll - target")))
          };
          val augmentedVars = mofO match {
            case Some(mof) => mof :: replacedVars;
            case None      => replacedVars
          };
          val msg = s"&{template:ep-default} ${augmentedVars.render}";
          debug(s"About to send: $msg");
          sendChat(ctx.player, target.message(msg));
        }
        case Some(t) => { invalidRoll(ctx); warn(s"template '${t}' is not supported") }
        case None    => { invalidRoll(ctx); warn("roll must use a roll template") }
      }
    } else {
      warn("No variables supplied!");
    }
  }
  //
  //  private def passthroughRoll(template: String, target: ChatCommand, vars: TemplateVars, ctx: ChatContext): Unit = {
  //    val replacedVars = vars.replaceInlineRollRefs(ctx.inlineRolls, ir => {
  //      log(s"Replacing inline roll with ${ir.expression}.");
  //      TemplateValue.InlineRoll(Rolls.InlineRoll(RollExprs.Native[Int](ir.expression)))
  //    });
  //    val msg = s"&{template:$template} ${replacedVars.render}";
  //    log(s"About to send: $msg");
  //    sendChat(ctx.player, target.message(msg));
  //  }

  private def invalidRoll(ctx: ChatContext): Unit = {
    ctx.reply("Invalid roll!");
  }

  private def transformRoll(total: Int): TemplateVal.InlineRoll = {

    val roll: RollExpression[Int] = fakeRoll(total, isCritical(total));
    val r = Rolls.InlineRoll(roll.label("success roll total"));
    TemplateVal.InlineRoll(r);
  }

  private def transformTarget(expr: String): TemplateVal.InlineRoll = {
    import RollExprs.Math;
    import Arith.RollArith;
    val exprParen: RollExpression[Int] = Math(RollArith(RollExprs.Native[Int](expr)).paren);
    val roll: RollExpression[Int] = fakeRoll(RollArith(exprParen.label("roll target")));
    val r = Rolls.InlineRoll(roll);
    TemplateVal.InlineRoll(r);
  }

  private def isCritical(total: Int): Boolean = {
    total == 0 || total % 11 == 0
  }

  private def fakeRoll(expr: ArithmeticExpression[Int], critical: Boolean = false): RollExpression[Int] = {
    import RollExprs.Math;
    import Arith.RollArith;

    if (critical) {
      Math(expr + RollArith(Dice.unit.cs()(0).label("API hack")));
    } else {
      Math(expr + RollArith(Dice.unit.cs()(1).label("API hack")));
    }
  }

  //  private def extractIfIntLiteral(tvar: TemplateVar): Option[Int] = tvar.value match {
  //    case TemplateVal.InlineRoll(Rolls.InlineRoll(RollExprs.Math(Arith.Literal(i: Int)))) => Some(i)
  //    case _ => None
  //  }
}
