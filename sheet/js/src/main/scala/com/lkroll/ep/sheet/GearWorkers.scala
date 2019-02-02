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
package com.lkroll.ep.sheet

import com.lkroll.roll20.facade.Roll20;
import com.lkroll.roll20.facade.Roll20.EventInfo;
import com.lkroll.roll20.sheet._
import com.lkroll.ep.model._
import SheetWorkerTypeShorthands._
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }
import scala.scalajs.js

object GearWorkers extends SheetWorker {
  import EPCharModel._

  val armourTotalCalc = op(armourEnergyBonus, armourKineticBonus, morphArmourEnergy, morphArmourKinetic, durability) update {
    case (energyBonus, kineticBonus, energyMorph, kineticMorph, dur) => Seq(
      armourEnergyTotal <<= (Math.min(dur, energyMorph + energyBonus)),
      armourKineticTotal <<= (Math.min(dur, kineticMorph + kineticBonus)))
  }

  val armourBonusFields = op(armourItems.active, armourItems.accessory,
    armourItems.energyBonus, armourItems.kineticBonus);

  val layeringPenaltyPerLayer = -20;

  private val armourBonusSum = armourBonusFields.fold(armourItems, (0, 0, 20))((acc: (Int, Int, Int), v: (String, (Boolean, Boolean, Int, Int))) => v match {
    case (_, ((active, accessory, energyBonus, kineticBonus))) => if (active) {
      (acc._1 + energyBonus, acc._2 + kineticBonus, if (accessory) { acc._3 } else { acc._3 + layeringPenaltyPerLayer })
    } else {
      acc
    }
  })(t => Seq(armourEnergyBonus <<= t._1, armourKineticBonus <<= t._2, layeringPenalty <<= (if (t._3 > 0) 0 else t._3)));

  private val armourBonusCalc: Tuple4[Boolean, Boolean, Int, Int] => UpdateDecision = {
    case (active, accessory, energyBonus, kineticBonus) => (emptyUpdates, ExecuteChain)
    case _ => (emptyUpdates, SkipChain)
  }

  val armourBonus = bind(armourBonusFields).update(armourBonusCalc, armourBonusSum.andThen(armourTotalCalc));

  onRemove(armourItems, (_: Roll20.EventInfo) => { armourBonusSum.andThen(armourTotalCalc)(); () });

  val skillSearchOpMelee = bind(op(meleeWeapons.skillSearch)) { (o: Option[String]) =>
    o match {
      case Some(needle) => {
        EPWorkers.searchSkillAndSetNameTotal(needle, meleeWeapons, meleeWeapons.skillName, meleeWeapons.skillTotal)
      }
      case None => Future.successful(()) // ignore
    }
  };

  val skillSearchOpRanged = bind(op(rangedWeapons.skillSearch)) { (o: Option[String]) =>
    o match {
      case Some(needle) => {
        EPWorkers.searchSkillAndSetNameTotal(needle, rangedWeapons, rangedWeapons.skillName, rangedWeapons.skillTotal)
      }
      case None => Future.successful(()) // ignore
    }
  };

  val weaponRangeLimitsFields = op(somTotal, rangedWeapons.thrown, rangedWeapons.shortRangeUpperInput, rangedWeapons.mediumRangeUpperInput, rangedWeapons.longRangeUpperInput, rangedWeapons.extremeRangeUpperInput);
  val weaponRangeLimits = weaponRangeLimitsFields update {
    case (som, somFactor, srui, mrui, lrui, xrui) => {
      val (sru, mru, lru, xru) = if (somFactor) {
        val somD = som.toDouble;
        val multSom: Double => Int = (in) => Math.ceil(in * somD).toInt
        (multSom(srui), multSom(mrui), multSom(lrui), multSom(xrui))
      } else {
        (srui.toInt, mrui.toInt, lrui.toInt, xrui.toInt)
      }
      val srl = if (sru <= 2) Math.max(sru - 1, 0) else 2;
      val mrl = sru + 1;
      val lrl = mru + 1;
      val xrl = lru + 1;
      val rus = if (somFactor) "×SOM" else "m";
      Seq(
        rangedWeapons.rangeUnitSymbol <<= rus,
        rangedWeapons.shortRangeUpper <<= sru,
        rangedWeapons.mediumRangeUpper <<= mru,
        rangedWeapons.longRangeUpper <<= lru,
        rangedWeapons.extremeRangeUpper <<= xru,
        rangedWeapons.shortRangeLower <<= srl,
        rangedWeapons.mediumRangeLower <<= mrl,
        rangedWeapons.longRangeLower <<= lrl,
        rangedWeapons.extremeRangeLower <<= xrl)
    }
  };
  val weaponRangeLimitsBinding = on(weaponRangeLimitsFields.getFields.map(f => s"change:${f.selector}").mkString(" "), (ei: EventInfo) => {
    val repIdS = Roll20.getActiveRepeatingField();
    debug(s"Got repid = $repIdS");
    val f = if (repIdS == "-1") {
      val op = weaponRangeLimits.all(RangedWeaponSection);
      op()
    } else {
      weaponRangeLimits()
    };
    f.onFailure {
      case e: Throwable => sheet.error(e)
    };
  });

  val damageAreaCalc = bind(op(rangedWeapons.damageArea)) update {
    case (daName) => {
      import DamageArea._

      val da = withName(daName);
      val daLabel = dynamicLabelShort(da);
      Seq(rangedWeapons.damageAreaShort <<= daLabel)
    }
  }

  val meleeDamageTypeCalc = bind(op(meleeWeapons.damageType)) update {
    case (dtName) => {
      import DamageType._

      val dt = withName(dtName);
      val dtLabel = dynamicLabelShort(dt);
      Seq(meleeWeapons.damageTypeShort <<= dtLabel)
    }
  }
  val meleeDamageDiv = bind(op(meleeWeapons.damageDivisor)) update {
    case (divisor) => {
      if (divisor == 1) {
        Seq(meleeWeapons.showDivisor <<= false)
      } else {
        Seq(meleeWeapons.showDivisor <<= true)
      }
    }
  }
  val rangedDamageTypeCalc = bind(op(rangedWeapons.damageType)) update {
    case (dtName) => {
      import DamageType._

      val dt = withName(dtName);
      val dtLabel = dynamicLabelShort(dt);
      Seq(rangedWeapons.damageTypeShort <<= dtLabel)
    }
  }
  val rangedDamageDiv = bind(op(rangedWeapons.damageDivisor)) update {
    case (divisor) => {
      if (divisor == 1) {
        Seq(rangedWeapons.showDivisor <<= false)
      } else {
        Seq(rangedWeapons.showDivisor <<= true)
      }
    }
  }
}
