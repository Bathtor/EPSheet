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

import com.larskroll.roll20.facade.Roll20;
import com.larskroll.roll20.facade.Roll20.EventInfo;
import com.larskroll.roll20.sheet._
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }
import scala.scalajs.js

object EPWorkers extends SheetWorker {

  import EPCharModel._

  register(activeSkills.reporder, ReporderSer);
  register(knowledgeSkills.reporder, ReporderSer);
  register(morphs.reporder, ReporderSer);

  onOpen {
    log("TestSheet: Sheet workers loading...");
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

  val activeSkillTotalCalc = bind(op(activeSkills.noDefaulting, activeSkills.linkedAptitude, activeSkills.morphBonus, activeSkills.ranks, cogTotal, cooTotal, intTotal, refTotal, savTotal, somTotal, wilTotal)) update {
    case (nodef, apt, morph, ranks, cog, coo, int, ref, sav, som, wil) => {
      import Aptitude._

      val aptTotal = ValueParsers.aptFrom(apt) match {
        case COO => coo
        case COG => cog
        case INT => int
        case REF => ref
        case SAV => sav
        case SOM => som
        case WIL => wil
      };
      val total = if (nodef) {
        if (ranks == 0) 0 else aptTotal + ranks + morph
      } else {
        aptTotal + ranks + morph
      };
      Seq(activeSkills.total <<= total)
    }
  }

  val knowledgeSkillTotalCalc = bind(op(knowledgeSkills.noDefaulting, knowledgeSkills.linkedAptitude, knowledgeSkills.morphBonus, knowledgeSkills.ranks, cogTotal, cooTotal, intTotal, refTotal, savTotal, somTotal, wilTotal)) update {
    case (nodef, apt, morph, ranks, cog, coo, int, ref, sav, som, wil) => {
      import Aptitude._

      val aptTotal = ValueParsers.aptFrom(apt) match {
        case COO => coo
        case COG => cog
        case INT => int
        case REF => ref
        case SAV => sav
        case SOM => som
        case WIL => wil
      };
      val total = if (nodef) {
        if (ranks == 0) 0 else aptTotal + ranks + morph
      } else {
        aptTotal + ranks + morph
      };
      Seq(knowledgeSkills.total <<= total)
    }
  }

  val skillTotalCalc = activeSkillTotalCalc.all(activeSkills).andThen(knowledgeSkillTotalCalc.all(knowledgeSkills));

  val skillCategoryCalc = bind(op(activeSkills.skillCategory)) update {
    case (catName) => {
      import Skills._

      val cat = SkillCategory.withName(catName);
      val catLabel = SkillCategory.dynamicLabelShort(cat);
      Seq(activeSkills.skillCategoryShort <<= catLabel)
    }
  }

  val setSkillsGenerating = nop update { _ =>
    Seq(generateSkillsLabel <<= "generating-skills")
  };
  val unsetSkillsGenerating = nop update { _ =>
    Seq(generateSkillsLabel <<= "generate-skills",
      generateSkills <<= false)
  }
  val unsetSkillsSorting = nop update { _ =>
    Seq(sortSkills <<= false)
  }

  val sortSkillsOp = op(sortSkillsBy) { sortByO: Option[String] =>
    import Skills.{ SortBy, ActiveSkillTuple, KnowledgeSkillTuple };
    import js.JSConverters._;

    sortByO match {
      case Some(sortByS) => {
        val sortBy = Skills.SortBy.withName(sortByS);
        if (sortBy == SortBy.None) {
          log(s"Field ${sortSkillsBy.name} is set to None. Not sorting."); Future.successful(())
        } else {
          val activeTuplesF = getRowAttrs(activeSkills, Seq(activeSkills.rowId, activeSkills.skillName, activeSkills.skillCategory, activeSkills.linkedAptitude, activeSkills.skillField)).map(_.map {
            case (k, v) => ActiveSkillTuple(k, v(activeSkills.rowId), v(activeSkills.skillName), v(activeSkills.skillCategory), v(activeSkills.linkedAptitude), v(activeSkills.skillField))
          }.toList);
          val knowledgeTuplesF = getRowAttrs(knowledgeSkills, Seq(knowledgeSkills.rowId, knowledgeSkills.skillName, knowledgeSkills.linkedAptitude, knowledgeSkills.skillField)).map(_.map {
            case (k, v) => KnowledgeSkillTuple(k, v(knowledgeSkills.rowId), v(knowledgeSkills.skillName), v(knowledgeSkills.linkedAptitude), v(knowledgeSkills.skillField))
          }.toList);
          val r = for {
            activeTuples <- activeTuplesF
            knowledgeTuples <- knowledgeTuplesF
          } yield {
            //debug(s"Sorting with $sortBy (${SortBy.activeOrdering(sortBy)} , ${SortBy.knowledgeOrdering(sortBy)}");
            val activeSorted = activeTuples.sorted(SortBy.activeOrdering(sortBy));
            //            val differenceActive = activeSorted.zip(activeTuples).filterNot(t => t._1 == t._2);
            //            debug(s"Difference after sorting:\n${differenceActive.mkString(";")}");
            val knowledgeSorted = knowledgeTuples.sorted(SortBy.knowledgeOrdering(sortBy));
            //            val differenceKnowledge = knowledgeSorted.zip(knowledgeTuples).filterNot(t => t._1 == t._2);
            //            debug(s"Difference after sorting:\n${differenceKnowledge.mkString(";")}");
            val activeSortedIds = activeSorted.map { x => x.sortId }.toArray;
            val knowledgeSortedIds = knowledgeSorted.map { x => x.sortId }.toArray;
            val data = Seq(activeSkills.reporder <<= activeSortedIds,
              knowledgeSkills.reporder <<= knowledgeSortedIds).toMap;
            setAttrs(data)
          };
          r flatMap identity
        }
      }
      case None => error(s"Field ${sortSkillsBy.name} has no value. Not sorting."); Future.successful(())
    }
  }

  val generateDefaultSkills = nop { _: Option[Unit] =>

    val activeNamesF = getRowAttrs(activeSkills, Seq(activeSkills.skillName)).map(_.flatMap {
      case (k, v) => v.apply(activeSkills.skillName)
    } toSet);
    val knowledgeNamesF = getRowAttrs(knowledgeSkills, Seq(knowledgeSkills.skillName)).map(_.flatMap {
      case (k, v) => v.apply(knowledgeSkills.skillName)
    } toSet);

    val r = for {
      activeNames <- activeNamesF;
      knowledgeNames <- knowledgeNamesF
    } yield {
      // double check there are no duplicate row ids generated (Roll20 seems to be doing that sometimes...)
      val dataNoDuplicates = Skills.pregen.foldLeft((Set.empty[String], Map.empty[FieldLike[Any], Any]))((acc, skill) => {
        acc match {
          case (ids, valueAcc) => {
            val ignore = if (skill.cls == Skills.SkillClass.Active) ignoreSkill(skill, activeNames) else ignoreSkill(skill, knowledgeNames);
            if (ignore) {
              (ids, valueAcc)
            } else {
              var curId: String = null;
              do {
                curId = Roll20.generateRowID();
              } while (ids.contains(curId))
              val skillValues = generateSkillWithId(curId, skill);
              (ids + curId, valueAcc ++ skillValues)
            }
          }
        }
      });
      //.flatten.toMap;
      setAttrs(dataNoDuplicates._2)
    };
    r flatMap identity
  };

  private def ignoreSkill(skill: Skill, existingNames: Set[String]): Boolean = {
    skill.field match {
      case Some("???") => false
      case _           => existingNames.contains(skill.name)
    }
  }

  private def generateSkillWithId(id: String, skill: Skill): Seq[(FieldLike[Any], Any)] = {
    if (skill.cls == Skills.SkillClass.Active) {
      import activeSkills._;
      Seq(
        activeSkills.at(id, rowId) <<= id,
        activeSkills.at(id, skillName) <<= skill.name,
        activeSkills.at(id, skillField) <<= skill.field.getOrElse(skillField.resetValue),
        activeSkills.at(id, skillCategory) <<= skill.category.toString(),
        activeSkills.at(id, skillCategoryShort) <<= Skills.SkillCategory.dynamicLabelShort(skill.category),
        activeSkills.at(id, specialisations) <<= specialisations.resetValue,
        activeSkills.at(id, linkedAptitude) <<= skill.apt.toString(),
        activeSkills.at(id, noDefaulting) <<= skill.noDefaulting,
        activeSkills.at(id, ranks) <<= ranks.resetValue,
        activeSkills.at(id, morphBonus) <<= morphBonus.resetValue,
        activeSkills.at(id, total) <<= total.resetValue)
    } else {
      import knowledgeSkills._;
      Seq(
        knowledgeSkills.at(id, rowId) <<= id,
        knowledgeSkills.at(id, skillName) <<= skill.name,
        knowledgeSkills.at(id, skillField) <<= skill.field.getOrElse(skillField.resetValue),
        knowledgeSkills.at(id, specialisations) <<= specialisations.resetValue,
        knowledgeSkills.at(id, linkedAptitude) <<= skill.apt.toString(),
        knowledgeSkills.at(id, noDefaulting) <<= skill.noDefaulting,
        knowledgeSkills.at(id, ranks) <<= ranks.resetValue,
        knowledgeSkills.at(id, morphBonus) <<= morphBonus.resetValue,
        knowledgeSkills.at(id, total) <<= total.resetValue)
    }
  }

  onChange(generateSkills, (ei: EventInfo) => {
    for {
      _ <- setSkillsGenerating();
      _ <- generateDefaultSkills();
      _ <- skillTotalCalc();
      _ <- sortSkillsOp();
      _ <- unsetSkillsGenerating()
    } yield ();
    ()
  })

  onChange(sortSkills, (ei: EventInfo) => {
    for {
      _ <- sortSkillsOp();
      _ <- unsetSkillsSorting()
    } yield ();
    ()
  })

  onChange(activeSkills, (ei: EventInfo) => {
    val rowId = Roll20.getActiveRepeatingField();
    if (js.isUndefined(rowId)) {
      error(s"Ignoring active skills event as rowId is undefined:\n${js.JSON.stringify(ei)}");
      ()
    } else {
      debug(s"Updated rowId=${rowId} caused by:\n${js.JSON.stringify(ei)}");
      val data = Seq(activeSkills.at(rowId, activeSkills.rowId) <<= rowId).toMap;
      setAttrs(data);
      ()
    }
  })

  val cogTotalCalc = bind(op(cogBase, cogMorph, cogMorphMax)) update (aptTotalCalc(cogTotal), skillTotalCalc);

  val cooTotalCalc = bind(op(cooBase, cooMorph, cooMorphMax)) update (aptTotalCalc(cooTotal), skillTotalCalc);

  val intTotalCalc = bind(op(intBase, intMorph, intMorphMax)) update (aptTotalCalc(intTotal), initCalc.andThen(skillTotalCalc));

  val refTotalCalc = bind(op(refBase, refMorph, refMorphMax)) update (aptTotalCalc(refTotal), initCalc.andThen(skillTotalCalc));

  val savTotalCalc = bind(op(savBase, savMorph, savMorphMax)) update (aptTotalCalc(savTotal), skillTotalCalc);

  val somTotalCalc = bind(op(somBase, somMorph, somMorphMax)) update (aptTotalCalc(somTotal), dbCalc.andThen(skillTotalCalc));

  val wilTotalCalc = bind(op(wilBase, wilMorph, wilMorphMax)) update (aptTotalCalc(wilTotal), willStatsCalc.andThen(skillTotalCalc));

  val aptTotals = cogTotalCalc ++ List(cooTotalCalc, intTotalCalc, refTotalCalc, savTotalCalc, somTotalCalc, wilTotalCalc);

  val aptTotalsAll = aptTotals ++ List(initCalc, dbCalc, willStatsCalc, skillTotalCalc);

  val durStatsCalc = op(durabilityBonus, morphDurability, morphType) update {
    case (bonus, morphDur, mt) => {
      val dur = morphDur + bonus;
      Seq(durability <<= dur,
        woundThreshold <<= Math.ceil(dur.toFloat / 5.0f).toInt,
        deathRating <<= drCalc(dur, MorphType.withName(mt)))
    }
  }

  // TODO ongoing updates notification

  onChange(morphs.active, (e: EventInfo) => {
    import scalajs.js;
    log(s"Morph Active info: ${e.sourceAttribute}");
    val rowId = Roll20.getActiveRepeatingField();
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
      case Success(_) => aptTotalsAll.andThen(durStatsCalc)()
      case Failure(e) => error(e)
    }
  }

  private val morphAttrsCalc: Tuple13[Boolean, String, String, Int, String, Int, Int, String, String, String, String, String, String] => Seq[(FieldLike[Any], Any)] = {
    case (active, name, tpe, dur, mob, ae, ak, imp, traits, descr, aptB, aptMax, skillB) => if (active) {
      val rowId = Roll20.getActiveRepeatingField();
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
      morphs.morphDescription, morphs.aptitudeBoni, morphs.aptitudeMax, morphs.skillBoni)).update(morphAttrsCalc, aptTotalsAll.andThen(durStatsCalc));

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

  //  onChange(skills.mod, (e: EventInfo) => {
  //    log(s"Skill Mod info: ${e.sourceAttribute}");
  //  });
}
