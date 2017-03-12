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

package com.larskroll.ep.sheet

import com.larskroll.roll20.facade.Roll20._
import com.larskroll.roll20.sheet._
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }

object EPWorkers extends SheetWorker {

  import EPCharModel._

  onOpen {
    log("TestSheet: Sheet workers loading...");
    versionLoadOp();
    ()
  };

  val versionLoadOp = op(versionField, characterSheet) {
    case Some((v, cs)) => {
      if (v == version) {
        log(s"Loaded sheet with version $v");
      } else {
        log(s"Loaded sheet with version $v < ${version}");
        // TODO update mechanism
        setAttrs(Map(versionField <<= version, characterSheet <<= s"$sheetName v$version"))
      }
    }
    case None => {
      log(s"Loaded unversioned sheet!");
      setAttrs(Map(versionField <<= version, characterSheet <<= s"$sheetName v$version"))
    }
  };

  private def aptTotalCalc(aptField: Field[Int]): Tuple3[Int, Int, Int] => Seq[(FieldLike[Any], Any)] = {
    case (base, morph, morphMax) => Seq(aptField <<= aptTotal(base, morph, morphMax));
  }

  val initCalc = op(intTotal, refTotal) update {
    case (int, ref) => Seq(initiative <<= Math.ceil((int + ref).toFloat / 5.0f).toInt)
  }

  val dbCalc = op(somTotal) update {
    case (som) => Seq(damageBonus <<= som / 10) // this is round down, funnily
  }

  val willStatsCalc = op(wilTotal) update {
    case (wil) => {
      log(s"Updating will dependent stats with ${wil}");
      val luc = wil * 2;
      Seq(
        lucidity <<= luc,
        traumaThreshold <<= Math.ceil(luc.toFloat / 5.0f).toInt,
        insanityRating <<= luc * 2)
    }
  };

  val cogTotalCalc = bind(op(cogBase, cogMorph, cogMorphMax)) update aptTotalCalc(cogTotal);

  val cooTotalCalc = bind(op(cooBase, cooMorph, cooMorphMax)) update aptTotalCalc(cooTotal);

  val intTotalCalc = bind(op(intBase, intMorph, intMorphMax)) update (aptTotalCalc(intTotal), initCalc);

  val refTotalCalc = bind(op(refBase, refMorph, refMorphMax)) update (aptTotalCalc(refTotal), initCalc);

  val savTotalCalc = bind(op(savBase, savMorph, savMorphMax)) update aptTotalCalc(savTotal);

  val somTotalCalc = bind(op(somBase, somMorph, somMorphMax)) update (aptTotalCalc(somTotal), dbCalc);

  val wilTotalCalc = bind(op(wilBase, wilMorph, wilMorphMax)) update (aptTotalCalc(wilTotal), willStatsCalc);

  val aptTotalsAll = cogTotalCalc ++ List(cooTotalCalc, intTotalCalc, refTotalCalc, savTotalCalc, somTotalCalc, wilTotalCalc);

  // TODO ongoing updates notification

  onChange(morphs.active, (e: EventInfo) => {
    import scalajs.js;
    log(s"Morph Active info: ${e.sourceAttribute}");
    val rowId = getActiveRepeatingField();
    val simpleRowId = rowId.split('_').last;
    val rowAttrsF = getRowAttrs(morphs, Seq(morphs.active));
    val currentF = getAttr(currentMorph);
    log("Fetching rows...");
    val doF = for {
      rowAttrs <- rowAttrsF;
      current <- currentF
    } yield {
      log(s"Got rows:\n${rowAttrs.mkString(",")}");
      val activeRow = rowAttrs(simpleRowId);
      val activeActive = activeRow(morphs.active);
      (current, activeActive) match {
        case (_, None) => log(s"No active value. Probably deleted a row?");
        case (Some(currentId), Some(true)) if (currentId == rowId) => log("Active morph is already current. What triggered the change?");
        case (Some(currentId), Some(true)) if (currentId != rowId) => {
          log(s"Active morph changed to ${rowId}");
          val updates = rowAttrs.filterKeys(_ != simpleRowId).mapValues(attrs => attrs(morphs.active) match {
            case Some(b) => b
            case None    => false
          }).filter({
            case (_, active) => active
          }).map({
            case (row, _) => (morphs.at(row, morphs.active) <<= false)
          });
          log(s"Deactivating other morphs: ${updates.mkString(",")}");
          val setF = setAttrs(updates.toMap);
          setF.onComplete {
            case Success(_) => log("All other morphs deactivated.");
            case Failure(e) => error(e);
          }
        }
        case (Some(currentId), Some(false)) if (currentId == rowId) => log("All morphs deactivated."); resetMorphDefaults();
        case (Some(currentId), Some(false)) if (currentId != rowId) => log("Morph was already deactivated. What triggered the change?");
        case x => log(s"Got something unexpected: ${x}");
      }
    };
    doF.onFailure {
      case e: Throwable => error(e)
    }
  });

  private def resetMorphDefaults() {
    val defaults = (morphs.active.resetValue, morphs.morphName.resetValue, morphs.morphType.resetValue,
      morphs.morphDurability.resetValue, morphs.morphMobilitySystem.resetValue, morphs.morphArmourEnergy.resetValue,
      morphs.morphArmourKinetic.resetValue, morphs.morphImplants.resetValue, morphs.morphTraits.resetValue,
      morphs.morphDescription.resetValue, morphs.aptitudeBoni.resetValue, morphs.aptitudeMax.resetValue,
      morphs.skillBoni.resetValue);
    val updates = Seq(currentMorph, morphType,
      morphName, morphDescription, morphTraits,
      morphImplants, morphMobilitySystem, morphDurability,
      morphArmourEnergy, morphArmourKinetic).map({ case f: Field[Any] => (f -> f.resetValue) }) ++ morphAptBoni("") ++ morphAptMax("");
    val setF = setAttrs(updates.toMap);
    setF.onComplete {
      case Success(_) => aptTotalsAll()
      case Failure(e) => error(e)
    }
  }

  private val morphAttrsCalc: Tuple13[Boolean, String, String, Int, String, Int, Int, String, String, String, String, String, String] => Seq[(FieldLike[Any], Any)] = {
    case (active, name, tpe, dur, mob, ae, ak, imp, traits, descr, aptB, aptMax, skillB) => if (active) {
      val rowId = getActiveRepeatingField();
      log(s"Current row: ${rowId}");
      Seq(morphs.id <<= rowId, currentMorph <<= rowId, morphType <<= tpe,
        morphName <<= name, morphDescription <<= descr, morphTraits <<= traits,
        morphImplants <<= imp, morphMobilitySystem <<= mob, morphDurability <<= dur,
        morphArmourEnergy <<= ae, morphArmourKinetic <<= ak) ++ morphAptBoni(aptB) ++ morphAptMax(aptMax)
    } else {
      Seq.empty
    }
  }

  // TODO skill modifiers
  val morphAttrs = bind(
    op(morphs.active, morphs.morphName, morphs.morphType, morphs.morphDurability, morphs.morphMobilitySystem,
      morphs.morphArmourEnergy, morphs.morphArmourKinetic, morphs.morphImplants, morphs.morphTraits,
      morphs.morphDescription, morphs.aptitudeBoni, morphs.aptitudeMax, morphs.skillBoni)).update(morphAttrsCalc, aptTotalsAll);

  private def morphAptBoni(s: String): Seq[(FieldLike[Any], Any)] = {
    val ab = ValueParsers.aptitudesFrom(s);
    val default = cogMorph.defaultValue.orElse(Some(0));
    log(s"Got Aptitude Bonus: $ab");
    val cog = ab.cog.orElse(default).map(v => cogMorph <<= v);
    val coo = ab.coo.orElse(default).map(v => cooMorph <<= v);
    val int = ab.int.orElse(default).map(v => intMorph <<= v);
    val ref = ab.ref.orElse(default).map(v => refMorph <<= v);
    val sav = ab.sav.orElse(default).map(v => savMorph <<= v);
    val som = ab.som.orElse(default).map(v => somMorph <<= v);
    val wil = ab.wil.orElse(default).map(v => wilMorph <<= v);
    Seq(cog, coo, int, ref, sav, som, wil).flatten
  }

  private def morphAptMax(s: String): Seq[(FieldLike[Any], Any)] = {
    val ab = ValueParsers.aptitudesFrom(s);
    val default = cogMorphMax.defaultValue.orElse(Some(0));
    log(s"Got Aptitude Max: $ab");
    val cog = ab.cog.orElse(default).map(v => cogMorphMax <<= v);
    val coo = ab.coo.orElse(default).map(v => cooMorphMax <<= v);
    val int = ab.int.orElse(default).map(v => intMorphMax <<= v);
    val ref = ab.ref.orElse(default).map(v => refMorphMax <<= v);
    val sav = ab.sav.orElse(default).map(v => savMorphMax <<= v);
    val som = ab.som.orElse(default).map(v => somMorphMax <<= v);
    val wil = ab.wil.orElse(default).map(v => wilMorphMax <<= v);
    Seq(cog, coo, int, ref, sav, som, wil).flatten
  }

  val durStatsCalc = op(durabilityBonus, morphDurability, morphType) update {
    case (bonus, morphDur, mt) => {
      val dur = morphDur + bonus;
      Seq(durability <<= dur,
        woundThreshold <<= Math.ceil(dur.toFloat / 5.0f).toInt,
        deathRating <<= drCalc(dur, MorphType.withName(mt)))
    }
  }

  private def drCalc(dur: Int, mType: MorphType.MorphType): Int = {
    mType match {
      case MorphType.Biomorph | MorphType.Pod => Math.ceil(dur * 1.5f).toInt
      case MorphType.Synthmorph               => dur * 2
      case _                                  => 0
    }
  }

  private def aptTotal(base: Int, morph: Int, max: Int): Int = {
    Math.min(base + morph, max)
  }

  onChange(skills.mod, (e: EventInfo) => {
    log(s"Skill Mod info: ${e.sourceAttribute}");
  });
}
