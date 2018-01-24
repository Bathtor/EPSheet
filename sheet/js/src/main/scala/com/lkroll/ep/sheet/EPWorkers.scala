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
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }
import scala.scalajs.js

object EPWorkers extends SheetWorkerRoot {

  override def children: Seq[SheetWorker] = Seq(SkillWorkers, MorphWorkers, GearWorkers, PsiWorkers);

  import EPCharModel._

  register(activeSkills.reporder, ReporderSer);
  register(knowledgeSkills.reporder, ReporderSer);
  register(morphs.reporder, ReporderSer);
  register[ChatCommand](ChatSer);

  onOpen {
    log("EPSheet: Sheet workers loading...");
    versionLoadOp();
    ()
  };

  val versionLoadOp = op(versionField, characterSheet) { o: Option[(String, String)] =>
    o match {
      case Some((v, cs)) => {
        if (v == version) {
          log(s"Loaded sheet with version $v");
          Promise[Unit]().success(()).future
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
    }
  };

  private def aptTotalCalc(aptField: Field[Int]): Tuple4[Int, Int, Int, Int] => Seq[(FieldLike[Any], Any)] = {
    case (base, tmp, morph, morphMax) => Seq(aptField <<= aptTotal(base, tmp, morph, morphMax));
  }

  val initCalc = op(intTotal, refTotal) update {
    case (int, ref) => Seq(initiative <<= Math.ceil((int + ref).toFloat / 5.0f).toInt)
  }

  val dbCalc = op(somTotal) update {
    case (som) => Seq(damageBonus <<= som / 10) // this is round down, funnily
  }

  val woundCalc = bind(op(wounds, woundsIgnored)) update {
    case (curWounds, woundsIgn) => {
      val woundsApl = Math.max(curWounds - woundsIgn, 0);
      val woundsModifier = woundsApl * 10;
      Seq(woundsApplied <<= woundsApl, woundMod <<= woundsModifier)
    }
  }

  val traumaCalc = bind(op(trauma)) update {
    case (tr) => Seq(traumaMod <<= tr * 10)
  }

  val museTraumaCalc = bind(op(museTrauma)) update {
    case (tr) => Seq(museTraumaMod <<= tr * 10)
  }

  val willStatsCalc = op(wilTotal) update {
    case (wil) => {
      log(s"Updating will dependent stats with ${wil}");
      val luc = wil * 2;
      Seq(
        lucidity <<= luc,
        insanityRating <<= luc * 2,
        psiTempTime <<= Math.ceil(wil.toFloat / 5.0).toInt)
    }
  };

  val traumaThresholdCalc = op(wilTotal, async) update {
    case (wil, isAsync) => {
      val luc = wil * 2;
      val tt = if (isAsync) {
        Math.max(Math.ceil(luc.toFloat / 5.0f).toInt - 1, 0)
      } else {
        Math.ceil(luc.toFloat / 5.0f).toInt
      };
      Seq(
        traumaThreshold <<= tt)
    }
  };

  onChange(async, (ei: EventInfo) => {
    val f = for {
      _ <- traumaThresholdCalc()
    } yield ();
    ()
  })

  val museWillStatsCalc = bind(op(museWil)) update {
    case (wil) => {
      log(s"Updating will dependent stats with ${wil}");
      val luc = wil * 2;
      Seq(
        museLucidity <<= luc,
        museTraumaThreshold <<= Math.ceil(luc.toFloat / 5.0f).toInt,
        museInsanityRating <<= luc * 2)
    }
  };

  val traitTypeCalc = bind(op(characterTraits.traitType)) update {
    case (traitTypeName) => {
      import TraitType._

      val traitType = TraitType.withName(traitTypeName);
      val traitTypeLabel = TraitType.dynamicLabelShort(traitType);
      Seq(characterTraits.traitTypeShort <<= traitTypeLabel)
    }
  }

  val cogTotalCalc = bind(op(cogBase, cogTemp, cogMorph, cogMorphMax)) update (aptTotalCalc(cogTotal), SkillWorkers.skillTotalCalc);

  val cooTotalCalc = bind(op(cooBase, cooTemp, cooMorph, cooMorphMax)) update (aptTotalCalc(cooTotal), SkillWorkers.skillTotalCalc);

  val intTotalCalc = bind(op(intBase, intTemp, intMorph, intMorphMax)) update (aptTotalCalc(intTotal), initCalc.andThen(SkillWorkers.skillTotalCalc));

  val refTotalCalc = bind(op(refBase, refTemp, refMorph, refMorphMax)) update (aptTotalCalc(refTotal), initCalc.andThen(SkillWorkers.skillTotalCalc));

  val savTotalCalc = bind(op(savBase, savTemp, savMorph, savMorphMax)) update (aptTotalCalc(savTotal), SkillWorkers.skillTotalCalc);

  val somTotalCalc = bind(op(somBase, somTemp, somMorph, somMorphMax)) update (aptTotalCalc(somTotal), dbCalc.andThen(SkillWorkers.skillTotalCalc));

  val wilTotalCalc = bind(op(wilBase, wilTemp, wilMorph, wilMorphMax)) update (aptTotalCalc(wilTotal), willStatsCalc.andThen(traumaThresholdCalc).andThen(SkillWorkers.skillTotalCalc));

  val aptTotals = cogTotalCalc ++ List(cooTotalCalc, intTotalCalc, refTotalCalc, savTotalCalc, somTotalCalc, wilTotalCalc);

  val aptTotalsAll = aptTotals ++ List(initCalc, dbCalc, willStatsCalc, traumaThresholdCalc, SkillWorkers.skillTotalCalc);

  val durStatsCalc = op(durabilityBonus, morphDurability, morphType) update {
    case (bonus, morphDur, mt) => {
      val dur = morphDur + bonus;
      Seq(
        durability <<= dur,
        woundThreshold <<= Math.ceil(dur.toFloat / 5.0f).toInt,
        deathRating <<= drCalc(dur, MorphType.withName(mt)))
    }
  }

  onChange(durabilityBonus, (ei: EventInfo) => {
    val f = for {
      _ <- durStatsCalc()
    } yield ();
    ()
  })

  val chatOutputCalc = bind(op(chatOutputSelect)) update {
    case (targetS) => {
      import ChatOutput._;
      val target = ChatOutput.withName(targetS);
      val cc = target match {
        case Public       => Chat.Default
        case GM           => Chat.GM
        case PublicScript => Chat.API("eproll", "")
        case GMScript     => Chat.API("eproll", "-o GM")
      };
      Seq(chatOutput <<= cc)
    }
  }

  // TODO ongoing updates notification

  private[sheet] def searchSkillAndSetNameTotal(needle: String, section: RepeatingSection, nameField: TextField, totalField: FieldRefRepeating[Int]): Future[Unit] = {
    val rowId = Roll20.getActiveRepeatingField();
    val simpleRowId = extractSimpleRowId(rowId);
    val rowAttrsF = getRowAttrs(activeSkills, Seq(activeSkills.skillName));
    log(s"Searching for skill name for ${section.name} ($simpleRowId). Fetching rows...");
    val doF = for {
      rowAttrs <- rowAttrsF
    } yield {
      log(s"Got rows:\n${rowAttrs.mkString(",")}");

      val nameToId = rowAttrs.flatMap {
        case (id, attrs) => attrs(activeSkills.skillName).map((_, id))
      }.toMap;
      val needleL = needle.toLowerCase();
      val ratings = nameToId.keys.flatMap(n => {
        val nL = n.toLowerCase();
        val matchRes = (nL, needleL).zipped.takeWhile(Function.tupled(_ == _)).map(_._1).mkString;
        val matchLength = matchRes.length;
        log(s"Compared $nL with $needleL and matched $matchRes of length $matchLength");
        if (matchLength > 0) {
          Some((matchLength, n))
        } else {
          None
        }
      }).toList;
      if (ratings.isEmpty) {
        log(s"No match found for $needle");
        setAttrs(Map(
          section.at(simpleRowId, nameField) <<= nameField.resetValue,
          section.at(simpleRowId, totalField) <<= totalField.resetValue))
      } else {
        val sorted = ratings.sorted;
        val selection = sorted.last;
        log(s"Selected $selection as best fit for $needle");
        val selectionId = nameToId(selection._2);
        setAttrs(Map(
          section.at(simpleRowId, nameField) <<= selection._2,
          section.at(simpleRowId, totalField) <<= totalField.valueAt(selectionId)))
      }
    };
    doF.onFailure {
      case e: Throwable =>
        error(e); setAttrs(Map(
          section.at(simpleRowId, nameField) <<= nameField.resetValue,
          section.at(simpleRowId, totalField) <<= totalField.resetValue))
    }
    doF.flatMap(identity)
  }

  val setFrayHalved = nop { _: Option[Unit] =>
    searchFrayAndSetHalved()
  }

  private[sheet] def searchFrayAndSetHalved(): Future[Unit] = {
    val rowAttrsF = getRowAttrs(activeSkills, Seq(activeSkills.skillName));
    val doF = for {
      rowAttrs <- rowAttrsF
    } yield {
      val nameToId = rowAttrs.flatMap {
        case (id, attrs) => attrs(activeSkills.skillName).map((_, id))
      }.toMap;
      val attrs = nameToId.get("Fray") match {
        case Some(id) => {
          Map(frayField <<= frayField.valueFrom(activeSkills.total, id))
        }
        case None => {
          Map(frayField <<= frayField.valueFrom(refTotal))
        }
      };
      setAttrs(attrs)
    }
    doF.onFailure {
      case e: Throwable => error(e)
    }
    doF.flatMap(identity)
  }

  private def drCalc(dur: Int, mType: MorphType.MorphType): Int = {
    mType match {
      case MorphType.Biomorph | MorphType.Pod => Math.ceil(dur * 1.5f).toInt
      case MorphType.Synthmorph               => dur * 2
      case _                                  => 0
    }
  }

  private def aptTotal(base: Int, tmp: Int, morph: Int, max: Int): Int = {
    Math.min(base + morph, max) + tmp // or Math.min(base + morph + tmp, max) the rules are unclear on this
  }
}
