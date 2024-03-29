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
import scala.scalajs.js.annotation._

@JSExportTopLevel("EPWorkers")
object EPWorkers extends SheetWorkerRoot {

  override def children: Seq[SheetWorker] =
    Seq(tabbedWorker, EPUpdates, SkillWorkers, MorphWorkers, GearWorkers, PsiWorkers, EffectsWorkers);

  lazy val tabbedWorker = TabbedWorker(EPCharModel, EPUpdates);

  import EPCharModel._

  register(activeSkills.reporder, ReporderSer);
  register(knowledgeSkills.reporder, ReporderSer);
  register(morphs.reporder, ReporderSer);
  register[ChatCommand](ChatSer);
  register[Boolean](ToggleSer);

  onOpen {
    log("EPSheet: Sheet workers loading...");
    //versionLoadOp();
    ()
  };

  private def aptTotalCalc(aptField: Field[Int]): Tuple4[Int, Int, Int, Int] => Seq[(FieldLike[Any], Any)] = {
    case (base, tmp, morph, morphMax) => Seq(aptField <<= aptTotal(base, tmp, morph, morphMax));
  }

  val initCalc = op(intTotal, refTotal, morphIniBonus, initiativeExtra) update {
    case (int, ref, morph, extra) => {
      val baseIni = Math.ceil((int + ref).toFloat / 5.0f).toInt;
      val ini = baseIni + morph + extra;
      Seq(initiative <<= ini)
    }
  };

  val dbCalc = op(somTotal, morphType) update {
    case (som, mTypeS) => {
      // this is rounded down, funnily
      val db = MorphType.withName(mTypeS) match {
        case MorphType.Synthmorph => (som / 10) + 2; // see EP core p. 143
        case _                    => (som / 10);
      };
      Seq(damageBonus <<= db)
    }
  };

  val spdCalc = bind(op(morphSpeed, speedExtra)) update {
    case (morph, extra) => Seq(speed <<= (morph + extra))
  };

  val moaCalc = bind(op(morphMOA, mentalOnlyActionsExtra)) update {
    case (morph, extra) => Seq(mentalOnlyActions <<= (morph + extra))
  };

  val woundCalc = bind(op(wounds, woundsIgnored, morphIgnoredWounds, woundsIgnoredEffects)) update {
    case (curWounds, woundsIgn, morph, effects) => {
      val woundsApl = Math.max(curWounds - woundsIgn - morph - effects, 0);
      val woundsModifier = woundsApl * 10;
      Seq(woundsApplied <<= woundsApl, woundMod <<= woundsModifier)
    }
  };

  val traumaCalc = bind(op(trauma, traumasIgnored, traumasIgnoredEffects)) update {
    case (curTraumas, traumasIgn, effects) => {
      val traumasApl = Math.max(curTraumas - traumasIgn - effects, 0);
      val traumasMod = traumasApl * 10;
      Seq(traumasApplied <<= traumasApl, traumaMod <<= traumasMod)
    }
  };

  val museTraumaCalc = bind(op(museTrauma)) update {
    case (tr) => Seq(museTraumaMod <<= tr * 10)
  };

  val willStatsCalc = bind(op(wilTotal, lucidityExtra, async)) update {
    case (wil, lucExtra, isAsync) => {
      debug(s"Updating will dependent stats with ${wil}");
      val baseLuc = wil * 2;
      val luc = baseLuc + lucExtra;
      val tt = if (isAsync) {
        Math.max(Math.ceil(luc.toFloat / 5.0f).toInt - 1, 0)
      } else {
        Math.ceil(luc.toFloat / 5.0f).toInt
      };
      Seq(
        lucidity <<= luc,
        stressMax <<= luc,
        insanityRating <<= baseLuc * 2, // the book isn't clear about this, but it's my interpretation
        psiTempTime <<= Math.ceil(wil.toFloat / 5.0).toInt,
        traumaThreshold <<= tt
      )
    }
  };

  // val traumaThresholdCalc = op(wilTotal, async) update {
  //   case (wil, isAsync) => {
  //     val luc = wil * 2;
  //     val tt = if (isAsync) {
  //       Math.max(Math.ceil(luc.toFloat / 5.0f).toInt - 1, 0)
  //     } else {
  //       Math.ceil(luc.toFloat / 5.0f).toInt
  //     };
  //     Seq(traumaThreshold <<= tt)
  //   }
  // };

  // onChange(async, (ei: EventInfo) => {
  //   val f = for {
  //     _ <- traumaThresholdCalc()
  //   } yield ();
  //   ()
  // })

  val museWillStatsCalc = bind(op(museWil)) update {
    case (wil) => {
      log(s"Updating will dependent stats with ${wil}");
      val luc = wil * 2;
      Seq(museLucidity <<= luc,
          museTraumaThreshold <<= Math.ceil(luc.toFloat / 5.0f).toInt,
          museInsanityRating <<= luc * 2)
    }
  };

  val traitTypeCalc = bind(op(characterTraits.traitType)) update {
    case (traitTypeName) => {
      import TraitType._

      val traitType = TraitType.withName(traitTypeName);
      val traitTypeShortKey = TraitType.dynamicLabelShort(traitType);
      val traitTypeShortLabel = getTranslationByKey(traitTypeShortKey).getOrElse(traitType.toString)
      Seq(characterTraits.traitTypeShort <<= traitTypeShortLabel)
    }
  }

  val cogTotalCalc = bind(op(cogBase, cogTemp, cogMorph, cogMorphMax)) update (aptTotalCalc(cogTotal), SkillWorkers.skillTotalCalc);

  val cooTotalCalc = bind(op(cooBase, cooTemp, cooMorph, cooMorphMax)) update (aptTotalCalc(cooTotal), SkillWorkers.skillTotalCalc);

  val intTotalCalc = bind(op(intBase, intTemp, intMorph, intMorphMax)) update (aptTotalCalc(intTotal), initCalc.andThen(
    SkillWorkers.skillTotalCalc
  ));

  val refTotalCalc = bind(op(refBase, refTemp, refMorph, refMorphMax)) update (aptTotalCalc(refTotal), initCalc.andThen(
    SkillWorkers.skillTotalCalc
  ));

  val savTotalCalc = bind(op(savBase, savTemp, savMorph, savMorphMax)) update (aptTotalCalc(savTotal), SkillWorkers.skillTotalCalc);

  val somTotalCalc = bind(op(somBase, somTemp, somMorph, somMorphMax)) update (aptTotalCalc(somTotal), dbCalc
    .andThen(GearWorkers.weaponRangeLimits.all(RangedWeaponSection))
    .andThen(SkillWorkers.skillTotalCalc));

  val wilTotalCalc = bind(op(wilBase, wilTemp, wilMorph, wilMorphMax)) update (aptTotalCalc(wilTotal), willStatsCalc
    .andThen(SkillWorkers.skillTotalCalc));

  val aptTotals = cogTotalCalc ++ List(cooTotalCalc,
                                       intTotalCalc,
                                       refTotalCalc,
                                       savTotalCalc,
                                       somTotalCalc,
                                       wilTotalCalc);

  val aptTotalsAll = aptTotals ++ List(initCalc,
                                       dbCalc,
                                       willStatsCalc,
                                       GearWorkers.weaponRangeLimits.all(RangedWeaponSection),
                                       SkillWorkers.skillTotalCalc);

  val durStatsCalc = op(durabilityBonus, morphDurability, morphType) update {
    case (bonus, morphDur, mt) => {
      val dur = morphDur + bonus;
      Seq(durability <<= dur,
          damageMax <<= dur,
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
      val (ccep, cco, api) = target match {
        case Public       => (Chat.Default, Chat.Default, false)
        case GM           => (Chat.GM, Chat.GM, false)
        case PublicScript => (Chat.API("eproll", ""), Chat.Default, true)
        case GMScript     => (Chat.API("eproll", "-o GM"), Chat.GM, true)
      };
      Seq(chatOutputEPRolls <<= ccep, chatOutputOther <<= cco, usingAPIScript <<= api)
    }
  }

  private[sheet] def searchSkillAndSetNameTotal(needle: String,
                                                section: RepeatingSection,
                                                nameField: TextField,
                                                totalField: FieldRefRepeating[Int]): Future[Unit] = {
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
      val ratings = nameToId.keys
        .flatMap(n => {
          val nL = n.toLowerCase();
          val matchRes = nL.lazyZip(needleL).takeWhile(Function.tupled(_ == _)).map(_._1).mkString;
          val matchLength = matchRes.length;
          log(s"Compared $nL with $needleL and matched $matchRes of length $matchLength");
          if (matchLength > 0) {
            Some((matchLength, n))
          } else {
            None
          }
        })
        .toList;
      if (ratings.isEmpty) {
        log(s"No match found for $needle");
        setAttrs(
          Map(section.at(simpleRowId, nameField) <<= nameField.resetValue,
              section.at(simpleRowId, totalField) <<= totalField.resetValue)
        )
      } else {
        val sorted = ratings.sorted;
        val selection = sorted.last;
        log(s"Selected $selection as best fit for $needle");
        val selectionId = nameToId(selection._2);
        setAttrs(
          Map(section.at(simpleRowId, nameField) <<= selection._2,
              section.at(simpleRowId, totalField) <<= totalField.valueAt(selectionId))
        )
      }
    };
    doF.onComplete {
      case Success(_) => ()
      case Failure(e) =>
        error(e);
        setAttrs(
          Map(section.at(simpleRowId, nameField) <<= nameField.resetValue,
              section.at(simpleRowId, totalField) <<= totalField.resetValue)
        )
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
      val frayTermO = getTranslationByKey(EPTranslation.defaultSkills("Fray").key);
      val attrs = frayTermO.flatMap(frayTerm => nameToId.get(frayTerm)) match {
        case Some(id) => {
          Map(frayField <<= frayField.valueFrom(activeSkills.total, id))
        }
        case None => {
          Map(frayField <<= frayField.valueFrom(refTotal))
        }
      };
      setAttrs(attrs)
    }
    doF.onComplete {
      case Success(_) => ()
      case Failure(e) => error(e)
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
