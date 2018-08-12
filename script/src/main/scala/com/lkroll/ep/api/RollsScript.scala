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
import com.lkroll.ep.model.{ EPCharModel => epmodel, EPTranslation => ept, DamageType }
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import util.{ Try, Success, Failure }
import CoreImplicits._;

object RollsScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPRollsCommand, SpecialRollsCommand);

  def transformRoll(total: Int): TemplateVal.InlineRoll = {
    val roll: RollExpression[Int] = fakeRoll(Arith.RollArith(total.label("success roll total")), isCritical(total));
    val r = Rolls.InlineRoll(roll);
    TemplateVal.InlineRoll(r);
  }

  def transformTarget(expr: String): TemplateVal.InlineRoll = {
    import RollExprs.Math;
    import Arith.RollArith;
    val exprParen: RollExpression[Int] = Math(RollArith(RollExprs.Native[Int](expr)).paren);
    val roll: RollExpression[Int] = fakeRoll(RollArith(exprParen.label("roll target")));
    val r = Rolls.InlineRoll(roll);
    TemplateVal.InlineRoll(r);
  }

  def isCritical(total: Int): Boolean = {
    total == 0 || total % 11 == 0
  }

  def fakeRoll(expr: ArithmeticExpression[Int], critical: Boolean = false): RollExpression[Int] = {
    import RollExprs.Math;
    import Arith.RollArith;

    if (critical) {
      Math(expr + RollArith(Dice.unit.cs()(0).label("API hack")));
    } else {
      Math(expr + RollArith(Dice.unit.cs()(1).label("API hack")));
    }
  }
}

class SpecialRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  import org.rogach.scallop.singleArgConverter;

  val success = opt[Boolean]("success", descr = "roll a simple success roll");
  val target = opt[Int]("target", default = Some(99), descr = "target for a success roll");

  val damage = opt[Boolean]("damage", descr = "roll a damage roll");
  val damageDice = opt[Int]("damage-dice", default = Some(1), descr = "how many d10 to roll");
  val damageConst = opt[Int]("damage-const", default = Some(0), descr = "how much constant damage to add");
  val damageDiv = opt[Int]("damage-div", default = Some(1), descr = "what to divide the roll by");
  val ap = opt[Int]("ap", default = Some(0), descr = "armour penetration");
  val damageType = opt[String]("damage-type", descr = "damage type (Kinetic or Energy)");
  val label = opt[String]("label", descr = "custom roll label")(ScallopUtils.singleArgSpacedConverter(identity));
  val sublabel = opt[String]("sublabel", descr = "custom roll sublabel")(ScallopUtils.singleArgSpacedConverter(identity));
  requireOne(success, damage);
  dependsOnAll(target, List(success));
  dependsOnAll(damageDice, List(damage));
  dependsOnAll(damageConst, List(damage));
  dependsOnAll(damageDiv, List(damage));
  dependsOnAll(damageType, List(damage));
  dependsOnAll(ap, List(damage));
  verify();
}

object SpecialRollsCommand extends APICommand[SpecialRollsConf] {
  import CoreImplicits._;

  override def command = "epspecialroll";
  override def options = (args) => new SpecialRollsConf(args);
  override def apply(config: SpecialRollsConf, ctx: ChatContext): Unit = {
    if (config.success()) {
      val templF = for {
        rollResult <- rollViaChat(Rolls.SimpleRoll(epmodel.epRoll.formula))
      } yield {
        val targetValue = config.target();
        val targetR = RollsScript.transformTarget(targetValue.toString()).r;
        val roll = RollsScript.transformRoll(rollResult).r;
        val mofO = if (rollResult > targetValue) {
          val roll = numToExpr(rollResult);
          val target = numToExpr(targetValue);
          Some(Rolls.InlineRoll(RollsScript.fakeRoll(roll - target.paren).label("roll - target")))
        } else None;
        asDefaultTemplate(
          character = "API",
          attributeField = config.label.getOrElse("Success Roll"),
          attributeSubField = config.sublabel.toOption,
          testRoll = roll,
          testTarget = targetR,
          testMoF = mofO);
      };
      templF.onComplete {
        case Success(templ) => ctx.reply(templ)
        case Failure(e)     => error(e)
      }
    } else if (config.damage()) {
      val rollExpr = Dice.d10.copy(n = config.damageDice()) / config.damageDiv() + config.damageConst();
      val templ = asDamageTemplate(
        character = "API",
        attributeField = config.label.getOrElse("Damage Roll"),
        damageRoll = Rolls.InlineRoll(rollExpr),
        damageType = config.damageType.getOrElse("Unspecified"),
        armourPenetration = config.ap());
      ctx.reply(templ);
    }
  }

  lazy val minConf = new SpecialRollsConf(Seq("--success"));
}

class EPRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  import org.rogach.scallop.singleArgConverter;

  val output = opt[String]("output", descr = "who should receive the final output");
  val variables = trailArg[TemplateVars]("variables")(TemplateVars);
  verify();
}

object EPRollsCommand extends APICommand[EPRollsConf] {
  import APIImplicits._;
  import TemplateImplicits._;

  implicit val labelFields: LabelFields = ExplicitlyLabelFields;

  override def command = "eproll";
  override def options = (args) => new EPRollsConf(args);
  override def apply(config: EPRollsConf, ctx: ChatContext): Unit = {
    val target = if (config.output.isSupplied) {
      Chat.Whisper(config.output())
    } else {
      Chat.Default
    };
    debug(s"Got roll ${ctx.raw.content}");
    log(JSON.stringify(ctx.raw), true);
    if (config.variables.isSupplied) {
      val vars = config.variables();
      ctx.rollTemplate match {
        case Some("ep-default") => transformDefault(ctx, vars, target);
        case Some("ep-damage")  => transformDamage(ctx, vars, target);
        case Some(t)            => { invalidRoll(ctx); warn(s"template '${t}' is not supported") }
        case None               => { invalidRoll(ctx); warn("roll must use a roll template") }
      }
    } else {
      warn("No variables supplied!");
    }
  }

  private def transformDefault(ctx: ChatContext, vars: TemplateVars, target: ChatCommand): Unit = {
    var testTarget, testRoll: Option[Int] = None;
    val replacedVars = vars.replaceInlineRollRefs(ctx.inlineRolls, (ir, tvar) => {
      Try(ir.results.total.toInt) match {
        case Success(total) => {
          tvar.key match {
            case "test-roll"   => { testRoll = Some(total); RollsScript.transformRoll(total) }
            case "test-target" => { testTarget = Some(total); RollsScript.transformTarget(ir.expression) }
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
      TemplateVar("test-mof", TemplateVal.InlineRoll(RollsScript.fakeRoll(roll - target.paren).label("roll - target")))
    };
    val augmentedVars = mofO match {
      case Some(mof) => mof :: replacedVars;
      case None      => replacedVars
    };
    val msg = templateApplication("ep-default", augmentedVars);
    debug(s"About to send: $msg");
    sendChat(ctx.player, target.message(msg));
  }

  private def transformDamage(ctx: ChatContext, vars: TemplateVars, target: ChatCommand): Unit = {
    var damageTotal: Option[Int] = None;
    val replacedVars = vars.replaceInlineRollRefs(ctx.inlineRolls, (ir, tvar) => {
      Try(ir.results.total.toInt) match {
        case Success(total) => tvar.key match {
          case "damage-roll" => { damageTotal = Some(total); TemplateVal.InlineRoll(total) }
          case _             => TemplateVal.InlineRoll(total)
        }
        case Failure(e) => { error(e); TemplateVal.Empty }
      }
    });
    val msg = damageTotal match {
      case Some(total) => {
        val c = CharToolsCommand.minConf;
        val armour = vars.lookup("damage-type").map {
          case TemplateVar(_, TemplateVal.Raw(s)) => Some(DamageType.withName(s))
          case tv                                 => { error(s"Invalid value for damage-type: $tv"); None }
        }.flatten;
        val ap: Int = vars.lookup("armour-penetration").map {
          case TemplateVar(_, TemplateVal.Number(i: Int)) => Some(i)
          case TemplateVar(_, TemplateVal.Raw(s))         => Try(s.toInt).toOption
          case tv                                         => { error(s"Invalid value for armour-penetration: $tv"); None }
        }.flatten.getOrElse(0);
        val applyDamage = CharToolsCommand.invoke(ept.applyDamage.dynamic.render, List(
          c.characterName <<= epmodel.characterName.expr.forTarget.render.replaceAll("@", "&#64;"),
          c.damage <<= total,
          c.armour <<? armour,
          c.armourPenetration <<= ap));
        val applyCritDamage = CharToolsCommand.invoke(ept.applyCritDamage.dynamic.render, List(
          c.characterName <<= epmodel.characterName.expr.forTarget.render.replaceAll("@", "&#64;"),
          c.damage <<= total,
          c.armour <<? Option.empty[DamageType.DamageType],
          c.armourPenetration <<= ap));
        val augmentedVars = templateV("apply-damage" -> applyDamage) :: templateV("apply-crit-damage" -> applyCritDamage) :: replacedVars;
        templateApplication("ep-damage", augmentedVars)
      }
      case None => {
        error("Could not find damage total in damage roll!");
        templateApplication("ep-damage", replacedVars)
      }
    };
    debug(s"About to send: $msg");
    sendChat(ctx.player, target.message(msg));
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

  //  private def extractIfIntLiteral(tvar: TemplateVar): Option[Int] = tvar.value match {
  //    case TemplateVal.InlineRoll(Rolls.InlineRoll(RollExprs.Math(Arith.Literal(i: Int)))) => Some(i)
  //    case _ => None
  //  }
}
