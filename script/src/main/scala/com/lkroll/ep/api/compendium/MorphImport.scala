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
package com.lkroll.ep.api.compendium

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.ep.compendium._
import com.lkroll.ep.compendium.Effect._
import com.lkroll.ep.compendium.utils.OptionPickler._
import com.lkroll.ep.model.{ EPCharModel => epmodel, MorphSection, ValueParsers, Aptitude, MorphType => ModelMorphType }
import APIImplicits._;
import scala.util.{ Try, Success, Failure }

case class MorphModelImport(morph: MorphModel) extends Importable {
  override def updateLabel: String = morph.name;
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    val rowId = Some(idPool.generateRowId());
    char.createRepeating(MorphSection.id, rowId) <<= rowId.get;
    char.createRepeating(MorphSection.morphLabel, rowId) <<= s"Imported ${morph.name}";
    char.createRepeating(MorphSection.morphType, rowId) <<= morph.morphType.label;
    char.createRepeating(MorphSection.morphName, rowId) <<= morph.name;
    char.createRepeating(MorphSection.description, rowId) <<= morph.descr;
    char.createRepeating(MorphSection.traits, rowId) <<= morph.traits.mkString(", ");
    char.createRepeating(MorphSection.implants, rowId) <<= morph.enhancements.mkString(", ");
    char.createRepeating(MorphSection.mobilitySystem, rowId) <<= morph.movement.mkString(", ");
    char.createRepeating(MorphSection.durability, rowId) <<= morph.durability;
    morph.armour match {
      case Some((energy, kinetic)) => {
        char.createRepeating(MorphSection.armourEnergy, rowId) <<= energy;
        char.createRepeating(MorphSection.armourKinetic, rowId) <<= kinetic;
      }
      case None => // leave defaults
    }
    char.createRepeating(MorphSection.aptitudeBoni, rowId) <<= write(morph.aptitudeBonus);
    char.createRepeating(MorphSection.aptitudeMax, rowId) <<= write(morph.aptitudeMax);

    var speed = 1;
    var moa = 0;
    var skillBoni = List.empty[String];
    var iniBonus = 0;
    var ignoredWounds = 0;
    var notes = List.empty[String];
    morph.otherEffects.foreach {
      case SpeedMod(mod)    => speed += mod
      case MOAMod(mod)      => moa += mod
      case IniMod(mod)      => iniBonus += mod
      case _: AptitudeMod   => notes ::= "Not importing AptitudeMod. Use Aptitude Boni field instead."
      case s: SkillMod      => skillBoni ::= s.text
      case _: DurMod        => notes ::= "Not importing DurMod. Use Durability field instead."
      case IgnoreWounds(w)  => ignoredWounds += w
      case _: IgnoreTraumas => notes ::= "Not importing IgnoreTraumas."
      case _: LucMod        => notes ::= "Not importing LucMod."
      case _: DamageEffect  => notes ::= "Not importing DamageEffect."
      case _: FreeForm      => notes ::= "Not importing FreeForm."
    }
    char.createRepeating(MorphSection.speed, rowId) <<= speed;
    char.createRepeating(MorphSection.moa, rowId) <<= moa;
    char.createRepeating(MorphSection.skillBoni, rowId) <<= skillBoni.mkString(", ");
    char.createRepeating(MorphSection.iniBonus, rowId) <<= iniBonus;
    char.createRepeating(MorphSection.ignoredWounds, rowId) <<= ignoredWounds;

    val notesS = if (notes.isEmpty) "" else notes.mkString(" [", ", ", "]");

    morph.playerDecisions match {
      case Some(s) => Ok(s"TODO: $s $notesS")
      case None    => Ok("Ok" ++ notesS)
    }
  }
  override def children: List[Importable] = morph.attacks.map(a => WeaponImport(a)).toList;
}

case class MorphInstanceImport(morph: MorphInstance) extends Importable {
  override def updateLabel: String = s"${morph.label} (${morph.model})";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    importInto(char, idPool.generateRowId(), cache)
  }
  def importInto(char: Character, id: String, cache: ImportCache): Result[String] = {
    val rowId = Some(id);
    char.createRepeating(MorphSection.id, rowId) <<= rowId.get;
    char.createRepeating(MorphSection.morphLabel, rowId) <<= morph.label;
    char.createRepeating(MorphSection.morphType, rowId) <<= morph.morphType.label;
    char.createRepeating(MorphSection.morphName, rowId) <<= morph.model;
    morph.location match {
      case Some(l) => char.createRepeating(MorphSection.morphLocation, rowId) <<= l;
      case None    => // leave defaults
    }
    char.createRepeating(MorphSection.description, rowId) <<= morph.descr;
    morph.visibleAge match {
      case Some(a) => char.createRepeating(MorphSection.visibleAge, rowId) <<= a.toString();
      case None    => // leave defaults
    }
    morph.visibleGender match {
      case Some(g) => char.createRepeating(MorphSection.visibleGender, rowId) <<= g;
      case None    => // leave defaults
    }
    char.createRepeating(MorphSection.traits, rowId) <<= morph.traits.mkString(", ");
    char.createRepeating(MorphSection.implants, rowId) <<= morph.enhancements.mkString(", ");
    char.createRepeating(MorphSection.mobilitySystem, rowId) <<= morph.movement.mkString(", ");
    char.createRepeating(MorphSection.durability, rowId) <<= morph.durability;
    morph.armour match {
      case Some((energy, kinetic)) => {
        char.createRepeating(MorphSection.armourEnergy, rowId) <<= energy;
        char.createRepeating(MorphSection.armourKinetic, rowId) <<= kinetic;
      }
      case None => // leave defaults
    }
    char.createRepeating(MorphSection.aptitudeBoni, rowId) <<= write(morph.aptitudeBonus);
    char.createRepeating(MorphSection.aptitudeMax, rowId) <<= write(morph.aptitudeMax);
    var speed = 1;
    var moa = 0;
    var skillBoni = List.empty[String];
    var iniBonus = 0;
    var ignoredWounds = 0;
    var notes = List.empty[String];
    morph.otherEffects.foreach {
      case SpeedMod(mod)    => speed += mod
      case MOAMod(mod)      => moa += mod
      case IniMod(mod)      => iniBonus += mod
      case _: AptitudeMod   => notes ::= "Not importing AptitudeMod. Use Aptitude Boni field instead."
      case s: SkillMod      => skillBoni ::= s.text
      case _: DurMod        => notes ::= "Not importing DurMod. Use Durability field instead."
      case IgnoreWounds(w)  => ignoredWounds += w
      case _: IgnoreTraumas => notes ::= "Not importing IgnoreTraumas."
      case _: LucMod        => notes ::= "Not importing LucMod."
      case _: DamageEffect  => notes ::= "Not importing DamageEffect."
      case _: FreeForm      => notes ::= "Not importing FreeForm."
    }
    char.createRepeating(MorphSection.speed, rowId) <<= speed;
    char.createRepeating(MorphSection.moa, rowId) <<= moa;
    char.createRepeating(MorphSection.skillBoni, rowId) <<= skillBoni.mkString(", ");
    char.createRepeating(MorphSection.iniBonus, rowId) <<= iniBonus;
    char.createRepeating(MorphSection.ignoredWounds, rowId) <<= ignoredWounds;

    val notesS = if (notes.isEmpty) "" else notes.mkString(" [", ", ", "]");
    Ok("Ok" ++ notesS)
  }
  override def children: List[Importable] = morph.attacks.map(a => WeaponImport(a)).toList;
}

object MorphInstanceExport extends Exportable {
  override def updateLabel: String = "Morph Instance";
  override def exportFrom(char: AttributeCache): Result[Data] = {
    val rowIdS = char.attribute(epmodel.currentMorph)();
    if (rowIdS.equalsIgnoreCase("none")) {
      Err("No morph active.")
    } else {
      val rowId = APIUtils.extractSimpleRowId(rowIdS);
      APILogger.debug(s"Exporting morph at ${rowId}");
      val mLabel = char.repeatingAt(rowId)(MorphSection.morphLabel).map(_.apply()).getOrElse("Exported Morph");
      //APILogger.debug(s"Label was ${mLabel}");
      val mTypeO = char.repeatingAt(rowId)(MorphSection.morphType).map(_.apply()).
        map(ModelMorphType.withName(_)).
        flatMap(toCompendiumMorphType(_));
      APILogger.debug(s"Type was ${mTypeO}");
      val mModel = char.repeatingAt(rowId)(MorphSection.morphName).map(_.apply()).getOrElse("Unknown Model");
      val mLocation = Some("EXPORTED");
      val mDescr = char.repeatingAt(rowId)(MorphSection.description).map(_.apply()).getOrElse("");
      val mAge = char.repeatingAt(rowId)(MorphSection.visibleAge).map(_.apply()).
        flatMap(s => Try(s.toInt).toOption);
      val mGender = char.repeatingAt(rowId)(MorphSection.visibleGender).map(_.apply());
      val mTraits: Seq[String] = char.repeatingAt(rowId)(MorphSection.traits).map(
        _.apply().split(",").map(_.trim()).toSeq).getOrElse(Seq.empty);
      val mImplants: Seq[String] = char.repeatingAt(rowId)(MorphSection.implants).map(
        _.apply().split(",").map(_.trim()).toSeq).getOrElse(Seq.empty);
      val mMovement: Seq[String] = char.repeatingAt(rowId)(MorphSection.mobilitySystem).map(
        _.apply().split(",").map(_.trim()).toSeq).getOrElse(Seq.empty);
      val mDur = char.repeatingAt(rowId)(MorphSection.durability).map(_.apply()).getOrElse(0);
      val mAEnergyO = char.repeatingAt(rowId)(MorphSection.armourEnergy).map(_.apply());
      val mAKineticO = char.repeatingAt(rowId)(MorphSection.armourKinetic).map(_.apply());
      val mArmour = if (mAEnergyO.isEmpty && mAKineticO.isEmpty) { None } else {
        val energy = mAEnergyO.getOrElse(0);
        val kinetic = mAKineticO.getOrElse(0);
        Some((energy, kinetic))
      };
      val mAptBonusT = char.repeatingAt(rowId)(MorphSection.aptitudeBoni).map(_.apply()).
        toTryOr("").
        flatMap(ValueParsers.aptitudesFrom(_)).
        map(toCompendiumApts(_));
      val mAptMaxT = char.repeatingAt(rowId)(MorphSection.aptitudeMax).map(_.apply()).
        toTryOr("").
        flatMap(ValueParsers.aptitudesFrom(_)).
        map(toCompendiumApts(_));
      val mSkillBonusT = char.repeatingAt(rowId)(MorphSection.skillBoni).map(_.apply()).
        toTryOr("").
        flatMap(ValueParsers.skillsFrom(_)).
        map(toCompendiumSkills(_));
      val mSpeed = char.repeatingAt(rowId)(MorphSection.speed).map(_.apply()).getOrElse(1);
      val mMOA = char.repeatingAt(rowId)(MorphSection.moa).map(_.apply()).getOrElse(0);
      val mIni = char.repeatingAt(rowId)(MorphSection.iniBonus).map(_.apply()).getOrElse(0);
      val mIgnoredWounds = char.repeatingAt(rowId)(MorphSection.ignoredWounds).map(_.apply()).getOrElse(0);

      val r = for {
        mAptBonus <- mAptBonusT;
        mAptMax <- mAptMaxT;
        mSkillBonus <- mSkillBonusT;
        mType <- mTypeO.toTry("Invalid Morph Type")
      } yield {
        val speedBonus: List[Effect] = if (mSpeed > 1) List(SpeedMod(mSpeed - 1)) else List.empty;
        val moaBonus: List[Effect] = if (mMOA > 0) List(MOAMod(mMOA)) else List.empty;
        val iniBonus: List[Effect] = if (mIni > 0) List(IniMod(mIni)) else List.empty;
        val ignWounds: List[Effect] = if (mIgnoredWounds > 0) List(IgnoreWounds(mIgnoredWounds)) else List.empty;
        val effects: List[Effect] = mSkillBonus.toList ++
          speedBonus ++
          moaBonus ++
          iniBonus ++
          ignWounds;

        MorphInstance(mLabel, mModel, mType, mDescr, mGender, mAge, mLocation,
          mImplants, mTraits, mMovement, mAptMax, mAptBonus, effects,
          Seq.empty, mDur, mArmour)
      };

      Result.fromTry(r).mapErr(e => e.getMessage)
    }
  }
}
