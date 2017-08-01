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
import SheetWorkerTypeShorthands._
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

  val museSkillTotalCalcOp = bind(op(museSkills.linkedAptitude, museSkills.ranks, museCog, museCoo, museInt, museRef, museSav, museSom, museWil)) update {
    case (apt, ranks, cog, coo, int, ref, sav, som, wil) => {
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
      val total = aptTotal + ranks;
      Seq(museSkills.total <<= total)
    }
  }

  val museSkillTotalCalc = museSkillTotalCalcOp.all(museSkills);

  val skillCategoryCalc = bind(op(activeSkills.category)) update {
    case (catName) => {
      import Skills._

      val cat = SkillCategory.withName(catName);
      val catLabel = SkillCategory.dynamicLabelShort(cat);
      Seq(activeSkills.categoryShort <<= catLabel)
    }
  }
  val setSkillsGenerating = nop update { _ =>
    Seq(generateSkillsLabel <<= "generating-skills",
      generateMuseSkillsLabel <<= "generating-skills")
  };
  val unsetSkillsGenerating = nop update { _ =>
    Seq(generateSkillsLabel <<= "generate-skills",
      generateSkills <<= false,
      generateMuseSkillsLabel <<= "generate-skills",
      generateMuseSkills <<= false)
  }
  val unsetSkillsSorting = nop update { _ =>
    Seq(sortSkills <<= false)
  }

  private def getActiveSkills(): Future[List[Skills.ActiveSkillTuple]] = {
    import Skills.ActiveSkillTuple;
    getRowAttrs(activeSkills, Seq(activeSkills.rowId, activeSkills.skillName, activeSkills.category, activeSkills.linkedAptitude, activeSkills.field)).map(_.map {
      case (k, v) => ActiveSkillTuple(k, v(activeSkills.rowId), v(activeSkills.skillName), v(activeSkills.category), v(activeSkills.linkedAptitude), v(activeSkills.field))
    }.toList)
  }

  private def getKnowledgeSkills(): Future[List[Skills.KnowledgeSkillTuple]] = {
    import Skills.KnowledgeSkillTuple;
    getRowAttrs(knowledgeSkills, Seq(knowledgeSkills.rowId, knowledgeSkills.skillName, knowledgeSkills.linkedAptitude, knowledgeSkills.field)).map(_.map {
      case (k, v) => KnowledgeSkillTuple(k, v(knowledgeSkills.rowId), v(knowledgeSkills.skillName), v(knowledgeSkills.linkedAptitude), v(knowledgeSkills.field))
    }.toList)
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
          val activeTuplesF = getActiveSkills();
          val knowledgeTuplesF = getKnowledgeSkills();
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

  val generateMuseDefaultSkills = nop { _: Option[Unit] =>

    val namesF = getRowAttrs(museSkills, Seq(museSkills.skillName)).map(_.flatMap {
      case (k, v) => v.apply(museSkills.skillName)
    } toSet);

    val defaultSkills = Map(
      "Academics" -> 50,
      "Hardware" -> 20,
      "Infosec" -> 20,
      "Interfacing" -> 30,
      "Profession" -> 50,
      "Research" -> 20,
      "Programming" -> 10,
      "Perception" -> 10,
      "[Custom]" -> 30);
    val defaultFields = Map(
      "Academics" -> "Psychology",
      "Hardware" -> "Electronics",
      "Profession" -> "Accounting");
    val customSkill = Skill("[Custom]", None, null, null, Aptitude.COG);

    val r = for {
      names <- namesF
    } yield {
      // double check there are no duplicate row ids generated (Roll20 seems to be doing that sometimes...)
      val dataNoDuplicates = (Skills.pregen ++ Seq(customSkill, customSkill, customSkill)).foldLeft((Set.empty[String], Map.empty[FieldLike[Any], Any]))((acc, skill) => {
        acc match {
          case (ids, valueAcc) => {
            val ignore = filterSkill(skill, names, defaultSkills.keySet);
            if (ignore) {
              (ids, valueAcc)
            } else {
              var curId: String = null;
              do {
                curId = Roll20.generateRowID();
              } while (ids.contains(curId))
              val skillValues = generateMuseSkillWithId(curId, skill, defaultSkills, defaultFields);
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

  private def filterSkill(skill: Skill, exclude: Set[String], include: Set[String]): Boolean = {
    if (include.contains(skill.name)) {
      skill.field match {
        case Some("???") => false
        case _           => exclude.contains(skill.name) || !include.contains(skill.name)
      }
    } else {
      true
    }
  }

  private def generateMuseSkillWithId(id: String, skill: Skill, defaultValues: Map[String, Int], defaultFields: Map[String, String]): Seq[(FieldLike[Any], Any)] = {
    import museSkills._;
    Seq(
      museSkills.at(id, skillName) <<= skill.name,
      museSkills.at(id, field) <<= defaultFields.getOrElse(skill.name, field.resetValue),
      museSkills.at(id, linkedAptitude) <<= skill.apt.toString(),
      museSkills.at(id, ranks) <<= defaultValues.getOrElse(skill.name, ranks.resetValue),
      museSkills.at(id, total) <<= total.resetValue)
  }

  private def generateSkillWithId(id: String, skill: Skill): Seq[(FieldLike[Any], Any)] = {
    if (skill.cls == Skills.SkillClass.Active) {
      import activeSkills._;
      Seq(
        activeSkills.at(id, rowId) <<= id,
        activeSkills.at(id, skillName) <<= skill.name,
        activeSkills.at(id, field) <<= skill.field.getOrElse(field.resetValue),
        activeSkills.at(id, category) <<= skill.category.toString(),
        activeSkills.at(id, categoryShort) <<= Skills.SkillCategory.dynamicLabelShort(skill.category),
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
        knowledgeSkills.at(id, field) <<= skill.field.getOrElse(field.resetValue),
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

  onChange(generateMuseSkills, (ei: EventInfo) => {
    for {
      _ <- setSkillsGenerating();
      _ <- generateMuseDefaultSkills();
      _ <- museSkillTotalCalc();
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

  val cogTotalCalc = bind(op(cogBase, cogTemp, cogMorph, cogMorphMax)) update (aptTotalCalc(cogTotal), skillTotalCalc);

  val cooTotalCalc = bind(op(cooBase, cooTemp, cooMorph, cooMorphMax)) update (aptTotalCalc(cooTotal), skillTotalCalc);

  val intTotalCalc = bind(op(intBase, intTemp, intMorph, intMorphMax)) update (aptTotalCalc(intTotal), initCalc.andThen(skillTotalCalc));

  val refTotalCalc = bind(op(refBase, refTemp, refMorph, refMorphMax)) update (aptTotalCalc(refTotal), initCalc.andThen(skillTotalCalc));

  val savTotalCalc = bind(op(savBase, savTemp, savMorph, savMorphMax)) update (aptTotalCalc(savTotal), skillTotalCalc);

  val somTotalCalc = bind(op(somBase, somTemp, somMorph, somMorphMax)) update (aptTotalCalc(somTotal), dbCalc.andThen(skillTotalCalc));

  val wilTotalCalc = bind(op(wilBase, wilTemp, wilMorph, wilMorphMax)) update (aptTotalCalc(wilTotal), willStatsCalc.andThen(skillTotalCalc));

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

  val nop7: Option[Tuple7[Int, Int, Int, Int, Int, Int, Int]] => ChainingDecision = (f) => ExecuteChain;

  val museAptSkillCalc = bind(op(museCog, museCoo, museInt, museRef, museSav, museSom, museWil)) apply (nop7, museSkillTotalCalc);

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

  private val morphSkillBoniCalc = op(morphSkillBoni) { skillBoniO: Option[String] =>
    val activeTuplesF = getActiveSkills();
    val knowledgeTuplesF = getKnowledgeSkills();
    val r = for {
      activeTuples <- activeTuplesF
      knowledgeTuples <- knowledgeTuplesF
    } yield {
      val data = skillBoniO match {
        case Some(skillBoniS) if !skillBoniS.isEmpty() => {
          val skillBoni = ValueParsers.skillsFrom(skillBoniS);
          debug(s"Applying Skill Boni:\n${skillBoni.mkString(",")}");
          val activeUpdates = activeTuples.map { t =>
            val bonus = skillBoni.flatMap { sm =>
              t.name.flatMap { tname =>
                if (sm.skill.equalsIgnoreCase(tname)) {
                  val fieldMatch = for {
                    smField <- sm.field;
                    tField <- t.field
                  } yield smField.equalsIgnoreCase(tField);
                  fieldMatch match {
                    case Some(true) | None => Some(sm.mod)
                    case Some(false)       => None
                  }
                } else None
              }
            }.sum;
            activeSkills.at(t.id, activeSkills.morphBonus) <<= bonus
          };
          val knowledgeUpdates = knowledgeTuples.map { t =>
            val bonus = skillBoni.flatMap { sm =>
              t.name.flatMap { tname =>
                if (sm.skill.equalsIgnoreCase(tname)) {
                  val fieldMatch = for {
                    smField <- sm.field;
                    tFieldRaw <- t.field;
                    tField <- if (tFieldRaw == "???") None else Some(tFieldRaw)
                  } yield smField.equalsIgnoreCase(tField);
                  fieldMatch match {
                    case Some(true) | None => Some(sm.mod)
                    case Some(false)       => None
                  }
                } else None
              }
            }.sum;
            knowledgeSkills.at(t.id, knowledgeSkills.morphBonus) <<= bonus
          };
          (activeUpdates ++ knowledgeUpdates).toMap
        }
        case _ => {
          debug("No skill boni to apply. Resetting.");
          val activeUpdates = activeTuples.map { t =>
            activeSkills.at(t.id, activeSkills.morphBonus) <<= activeSkills.morphBonus.resetValue
          }
          val knowledgeUpdates = knowledgeTuples.map { t =>
            knowledgeSkills.at(t.id, knowledgeSkills.morphBonus) <<= knowledgeSkills.morphBonus.resetValue
          }
          (activeUpdates ++ knowledgeUpdates).toMap
        }
      };
      setAttrs(data)
    };
    r flatMap identity

  }

  private def resetMorphDefaults() {
    //    val defaults = (morphs.active.resetValue, morphs.morphName.resetValue, morphs.morphType.resetValue,
    //      morphs.durability.resetValue, morphs.mobilitySystem.resetValue, morphs.armourEnergy.resetValue,
    //      morphs.armourKinetic.resetValue, morphs.implants.resetValue, morphs.traits.resetValue,
    //      morphs.description.resetValue, morphs.aptitudeBoni.resetValue, morphs.aptitudeMax.resetValue,
    //      morphs.skillBoni.resetValue);
    val updates = Seq(currentMorph, morphType,
      morphName, morphDescription, morphTraits,
      morphImplants, morphMobilitySystem, morphDurability,
      morphArmourEnergy, morphArmourKinetic, morphSkillBoni).map({ case f: Field[Any] => (f -> f.resetValue) }) ++ morphAptBoni("") ++ morphAptMax("");
    val setF = setAttrs(updates.toMap);
    setF.onComplete {
      case Success(_) => aptTotalsAll.andThen(durStatsCalc).andThen(morphSkillBoniCalc)()
      case Failure(e) => error(e)
    }
  }

  private val armourTotalCalc = op(armourEnergyBonus, armourKineticBonus, morphArmourEnergy, morphArmourKinetic, durability) update {
    case (energyBonus, kineticBonus, energyMorph, kineticMorph, dur) => Seq(
      armourEnergyTotal <<= (Math.min(dur, energyMorph + energyBonus)),
      armourKineticTotal <<= (Math.min(dur, kineticMorph + kineticBonus)))
  }

  private val morphAttrsCalc: Tuple15[Boolean, String, String, String, String, Int, String, Int, Int, String, String, String, String, String, String] => UpdateDecision = {
    case (active, name, tpe, gender, age, dur, mob, ae, ak, imp, traits, descr, aptB, aptMax, skillB) => if (active) {
      val rowId = Roll20.getActiveRepeatingField();
      log(s"Current row: ${rowId}");
      val updates = Seq(morphs.id <<= rowId, currentMorph <<= rowId, morphType <<= tpe,
        morphVisibleGender <<= gender, morphVisibleAge <<= age,
        morphName <<= name, morphDescription <<= descr, morphTraits <<= traits,
        morphImplants <<= imp, morphMobilitySystem <<= mob, morphDurability <<= dur,
        morphArmourEnergy <<= ae, morphArmourKinetic <<= ak, morphSkillBoni <<= skillB) ++ morphAptBoni(aptB) ++ morphAptMax(aptMax);
      (updates, ExecuteChain)
    } else {
      log("********** No updates, skipping chain **********")
      (emptyUpdates, SkipChain)
    }
  }

  val morphAttrs = bind(
    op(morphs.active, morphs.morphName, morphs.morphType, morphs.visibleGender, morphs.visibleAge, morphs.durability, morphs.mobilitySystem,
      morphs.armourEnergy, morphs.armourKinetic, morphs.implants, morphs.traits,
      morphs.description, morphs.aptitudeBoni, morphs.aptitudeMax, morphs.skillBoni)).
    update(morphAttrsCalc, aptTotalsAll ++ List(durStatsCalc, armourTotalCalc, morphSkillBoniCalc));

  private val armourBonusFields = op(armourItems.active, armourItems.accessory,
    armourItems.energyBonus, armourItems.kineticBonus);

  private val layeringPenaltyPerLayer = -20;

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

  val skillSearchOpMelee = bind(op(meleeWeapons.skillSearch)) { (o: Option[String]) =>
    o match {
      case Some(needle) => {
        searchSkillAndSetNameTotal(needle, meleeWeapons, meleeWeapons.skillName, meleeWeapons.skillTotal)
      }
      case None => Future.successful(()) // ignore
    }
  };

  val skillSearchOpRanged = bind(op(rangedWeapons.skillSearch)) { (o: Option[String]) =>
    o match {
      case Some(needle) => {
        searchSkillAndSetNameTotal(needle, rangedWeapons, rangedWeapons.skillName, rangedWeapons.skillTotal)
      }
      case None => Future.successful(()) // ignore
    }
  };

  private def searchSkillAndSetNameTotal(needle: String, weaponSection: RepeatingSection, nameField: TextField, totalField: FieldRef[Int]): Future[Unit] = {
    val rowId = Roll20.getActiveRepeatingField();
    val simpleRowId = rowId.split('_').last;
    val rowAttrsF = getRowAttrs(activeSkills, Seq(activeSkills.skillName));
    log(s"Searching for skill name for ${weaponSection.name} ($simpleRowId). Fetching rows...");
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
          weaponSection.at(simpleRowId, nameField) <<= nameField.resetValue,
          weaponSection.at(simpleRowId, totalField) <<= totalField.resetValue))
      } else {
        val sorted = ratings.sorted;
        val selection = sorted.last;
        log(s"Selected $selection as best fit for $needle");
        val selectionId = nameToId(selection._2);
        setAttrs(Map(
          weaponSection.at(simpleRowId, nameField) <<= selection._2,
          weaponSection.at(simpleRowId, totalField) <<= totalField.valueAt(selectionId)))
      }
    };
    doF.onFailure {
      case e: Throwable =>
        error(e); setAttrs(Map(
          weaponSection.at(simpleRowId, nameField) <<= nameField.resetValue,
          weaponSection.at(simpleRowId, totalField) <<= totalField.resetValue))
    }
    doF.flatMap(identity)
  }

  val weaponRangeLimits = bind(op(rangedWeapons.shortRangeUpper, rangedWeapons.mediumRangeUpper, rangedWeapons.longRangeUpper)) update {
    case (sru, mru, lru) => {
      val mrl = sru + 1;
      val lrl = mru + 1;
      val xrl = lru + 1;
      Seq(rangedWeapons.mediumRangeLower <<= mrl,
        rangedWeapons.longRangeLower <<= lrl,
        rangedWeapons.extremeRangeLower <<= xrl)
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
  val rangedDamageTypeCalc = bind(op(rangedWeapons.damageType)) update {
    case (dtName) => {
      import DamageType._

      val dt = withName(dtName);
      val dtLabel = dynamicLabelShort(dt);
      Seq(rangedWeapons.damageTypeShort <<= dtLabel)
    }
  }
  val psiChiTypeCalc = bind(op(psiChi.psiType)) update {
    case (ptName) => {
      import PsiType._

      val pt = withName(ptName);
      val ptLabel = dynamicLabelShort(pt);
      Seq(psiChi.psiTypeShort <<= ptLabel)
    }
  }

  val psiGammaTypeCalc = bind(op(psiGamma.psiType)) update {
    case (ptName) => {
      import PsiType._

      val pt = withName(ptName);
      val ptLabel = dynamicLabelShort(pt);
      Seq(psiGamma.psiTypeShort <<= ptLabel)
    }
  }

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

  private def aptTotal(base: Int, tmp: Int, morph: Int, max: Int): Int = {
    Math.min(base + morph, max) + tmp // or Math.min(base + morph + tmp, max) the rules are unclear on this
  }

  //  onChange(skills.mod, (e: EventInfo) => {
  //    log(s"Skill Mod info: ${e.sourceAttribute}");
  //  });
}
