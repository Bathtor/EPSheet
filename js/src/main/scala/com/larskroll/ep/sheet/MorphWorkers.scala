package com.larskroll.ep.sheet

import com.larskroll.roll20.facade.Roll20;
import com.larskroll.roll20.facade.Roll20.EventInfo;
import com.larskroll.roll20.sheet._
import SheetWorkerTypeShorthands._
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }
import scala.scalajs.js

object MorphWorkers extends SheetWorker {
  import EPCharModel._

  onChange(morphs.active, (e: EventInfo) => {
    import scalajs.js;
    log(s"Morph Active info: ${e.sourceAttribute}");
    val rowId = Roll20.getActiveRepeatingField();
    val simpleRowId = extractSimpleRowId(rowId);
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
          }).flatMap({
            case (row, _) => Seq(
              morphs.at(row, morphs.active) <<= false,
              morphs.at(row, morphs.morphLocation) <<= morphs.morphLocation.resetValue)
          });
          log(s"Deactivating other morphs: ${updates.mkString(",")}");
          val setF = setAttrs(updates.toMap);
          setF.onComplete {
            case Success(_) => log("All other morphs deactivated.");
            case Failure(e) => error(e);
          }
        }
        case (Some(currentId), Some(false)) if (currentId == rowId) => {
          log("All morphs deactivated.");
          resetMorphDefaults(Seq(morphs.at(simpleRowId, morphs.morphLocation) <<= morphs.morphLocation.resetValue));
        }
        case (Some(currentId), Some(false)) if (currentId != rowId) => log("Morph was already deactivated. What triggered the change?");
        case x => log(s"Got something unexpected: ${x}");
      }
    };
    doF.onFailure {
      case e: Throwable => error(e)
    }
  });

  private val morphAttrsCalc: Tuple15[Boolean, String, String, String, String, Int, String, Int, Int, String, String, String, String, String, String] => UpdateDecision = {
    case (active, name, tpe, gender, age, dur, mob, ae, ak, imp, traits, descr, aptB, aptMax, skillB) => if (active) {
      val rowId = Roll20.getActiveRepeatingField();
      log(s"Current row: ${rowId}");
      val updates = Seq(morphs.id <<= rowId, morphs.morphLocation <<= "ACTIVE",
        currentMorph <<= rowId, morphType <<= tpe,
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
    update(morphAttrsCalc, EPWorkers.aptTotalsAll ++ List(EPWorkers.durStatsCalc, GearWorkers.armourTotalCalc, SkillWorkers.morphSkillBoniCalc));

  private def resetMorphDefaults(extraUpdates: Seq[(FieldLike[Any], Any)] = Seq.empty) {
    val updates = Seq(currentMorph, morphType,
      morphName, morphDescription, morphTraits,
      morphImplants, morphMobilitySystem, morphDurability,
      morphArmourEnergy, morphArmourKinetic, morphSkillBoni).map({ case f: Field[Any] => (f -> f.resetValue) }) ++ morphAptBoni("") ++ morphAptMax("");
    val setF = setAttrs((extraUpdates ++ updates).toMap);
    setF.onComplete {
      case Success(_) => EPWorkers.aptTotalsAll.andThen(EPWorkers.durStatsCalc).andThen(SkillWorkers.morphSkillBoniCalc)()
      case Failure(e) => error(e)
    }
  }

  private[sheet] def morphAptBoni(s: String): Seq[(FieldLike[Any], Any)] = {
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

  private[sheet] def morphAptMax(s: String): Seq[(FieldLike[Any], Any)] = {
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
}
