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
import com.lkroll.roll20.sheet.model._
import com.lkroll.roll20.core._
import com.lkroll.ep.model._
import SheetWorkerTypeShorthands._
import util.{Failure, Success}
import concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import com.lkroll.ep.model.ValueParsers

object EffectsWorkers extends SheetWorker {
  import EPCharModel._

  val nop2: Option[Tuple2[Boolean, String]] => ChainingDecision = (f) => ExecuteChain;

  val effectsChangeCalc = bind(op(effects.active, effects.gameEffect)) apply (nop2, effectsCalc ++ List(
    EPWorkers.aptTotalsAll,
    EPWorkers.durStatsCalc,
    EPWorkers.initCalc,
    EPWorkers.woundCalc,
    EPWorkers.traumaCalc,
    EPWorkers.spdCalc,
    EPWorkers.moaCalc,
    SkillWorkers.skillTotalCalc
  ));

  lazy val effectsCalc = nop { _: Option[Unit] =>
    calculateEffects()
  };

  private def calculateEffects(): Future[Unit] = {
    val rowAttrsF = getRowAttrs(effects, Seq(effects.active, effects.gameEffect));
    log(s"Updating effects. Fetching rows...");
    val doF = for {
      rowAttrs <- rowAttrsF
    } yield {
      log(s"Got rows:\n${rowAttrs.mkString(",")}");

      var speedMod = 0;
      var moaMod = 0;
      var iniMod = 0;
      var aptitudeMods: Map[Aptitude.Aptitude, Int] = Map.empty;
      var skillMods: Map[String, SkillMod] = Map.empty;
      var durMod = 0;
      var ignoreWounds = 0;
      var ignoreTraumas = 0;
      var lucMod = 0;
      var freeform: List[FreeForm] = Nil;

      rowAttrs.foreach {
        case (_id, attrs) => {
          val active = attrs(effects.active).getOrElse(false);
          if (active) {
            val gameEffect = attrs(effects.gameEffect).getOrElse("");
            ValueParsers.effectsFrom(gameEffect) match {
              case Success(parsedEffects) =>
                parsedEffects.foreach {
                  case SpeedMod(mod) => speedMod += mod
                  case MOAMod(mod)   => moaMod += mod
                  case IniMod(mod)   => iniMod += mod
                  case AptitudeMod(apt, mod) => {
                    val current = aptitudeMods.getOrElse(apt, 0);
                    aptitudeMods = aptitudeMods + (apt -> (current + mod));
                  }
                  case smod @ SkillMod(skill, field, mod) => {
                    val key = s"$skill--${field.getOrElse("")}";
                    val update = skillMods.get(key) match {
                      case Some(other) => {
                        SkillMod(skill, field, other.mod + mod)
                      }
                      case None => smod
                    };
                    skillMods += (key -> update);
                  }
                  case DurMod(mod)      => durMod += mod
                  case IgnoreWounds(n)  => ignoreWounds += n
                  case IgnoreTraumas(n) => ignoreTraumas += n
                  case LucMod(mod)      => lucMod += mod
                  case ff: FreeForm     => freeform ::= ff
                }
              case Failure(e) => log(s"Could not parse effects '$gameEffect'. Error was: $e")
            };
          }
        }
      }
      var updates: Map[FieldLike[Any], Any] = Map.empty;
      var skillOpResult: Future[Unit] = Future.unit;
      val appliedSummary = {
        val sb = new StringBuilder;
        if (speedMod != 0) {
          updates += (speedExtra <<= speedMod);
          sb ++= SpeedMod(speedMod).text;
          sb ++= ", ";
        } else {
          updates += (speedExtra <<= speedExtra.resetValue);
        }
        if (moaMod != 0) {
          updates += (mentalOnlyActionsExtra <<= moaMod);
          sb ++= MOAMod(moaMod).text;
          sb ++= ", ";
        } else {
          updates += (mentalOnlyActionsExtra <<= mentalOnlyActionsExtra.resetValue);
        }
        if (iniMod != 0) {
          updates += (initiativeExtra <<= iniMod);
          sb ++= IniMod(iniMod).text;
          sb ++= ", ";
        } else {
          updates += (initiativeExtra <<= initiativeExtra.resetValue);
        }
        if (durMod != 0) {
          updates += (durabilityBonus <<= durMod);
          sb ++= DurMod(durMod).text;
          sb ++= ", ";
        } else {
          updates += (durabilityBonus <<= durabilityBonus.resetValue);
        }
        if (lucMod != 0) {
          updates += (lucidityExtra <<= lucMod);
          sb ++= LucMod(lucMod).text;
          sb ++= ", ";
        } else {
          updates += (lucidityExtra <<= lucidityExtra.resetValue);
        }
        if (ignoreWounds != 0) {
          updates += (woundsIgnoredEffects <<= ignoreWounds);
          sb ++= IgnoreWounds(ignoreWounds).text;
          sb ++= ", ";
        } else {
          updates += (woundsIgnoredEffects <<= woundsIgnoredEffects.resetValue);
        }
        if (ignoreTraumas != 0) {
          updates += (traumasIgnoredEffects <<= ignoreTraumas);
          sb ++= IgnoreTraumas(ignoreTraumas).text;
          sb ++= ", ";
        } else {
          updates += (traumasIgnoredEffects <<= traumasIgnoredEffects.resetValue);
        }
        // always reset aptitudes befor merging in new values
        updates ++= Map(
          cogTemp <<= cogTemp.resetValue,
          cooTemp <<= cooTemp.resetValue,
          intTemp <<= intTemp.resetValue,
          refTemp <<= refTemp.resetValue,
          savTemp <<= savTemp.resetValue,
          somTemp <<= somTemp.resetValue,
          wilTemp <<= wilTemp.resetValue
        );
        if (!aptitudeMods.isEmpty) {
          aptitudeMods.foreach {
            case (Aptitude.COG, mod) => updates += (cogTemp <<= mod)
            case (Aptitude.COO, mod) => updates += (cooTemp <<= mod)
            case (Aptitude.INT, mod) => updates += (intTemp <<= mod)
            case (Aptitude.REF, mod) => updates += (refTemp <<= mod)
            case (Aptitude.SAV, mod) => updates += (savTemp <<= mod)
            case (Aptitude.SOM, mod) => updates += (somTemp <<= mod)
            case (Aptitude.WIL, mod) => updates += (wilTemp <<= mod)
            case _                   => ??? // I think this is exhaustive -.-
          }
          sb ++= aptitudeMods
            .map {
              case (apt, mod) => AptitudeMod(apt, mod).text
            }
            .mkString(", ");
          sb ++= ", ";
        }
        if (!skillMods.isEmpty) {
          skillOpResult = SkillWorkers.effectsSkillBoniCalc(skillMods.values.toList);
          sb ++= skillMods.values
            .map(value => value.text)
            .mkString(", ");
        }
        if (sb.endsWith(", ")) {
          sb.take(sb.length - 2).result()
        } else {
          sb.result()
        }
      };
      // TODO add these two fields to the sheet (use code from active armour)
      updates += (appliedEffectsSummary <<= appliedSummary);
      val freeformSummary = freeform.map(_.text).mkString(", ");
      updates += (freeformEffectsSummary <<= freeformSummary);
      val updateRes = setAttrs(updates);
      skillOpResult.flatMap(_ => updateRes)
    };
    doF.flatten
  }
}
