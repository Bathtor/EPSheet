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
import com.lkroll.ep.model.{
  EPCharModel => epmodel,
  MeleeWeaponSection,
  RangedWeaponSection,
  DamageType => ModelDamageType,
  DamageArea => ModelDamageArea
}
import APIImplicits._;
//import scala.util.{ Try, Success, Failure }

object WeaponDamageTypeConverter {
  def convert(dt: DamageType): Result[ModelDamageType.DamageType] = {
    dt match {
      case DamageType.Energy  => Ok(ModelDamageType.Energy)
      case DamageType.Kinetic => Ok(ModelDamageType.Kinetic)
      case DamageType.Untyped => Ok(ModelDamageType.Untyped)
      case DamageType.Psychic => Err("Weapons can not have Psychic damage type!")
    }
  }
}

object WeaponDamageAreaConverter {
  def convert(da: DamageArea): Result[ModelDamageArea.DamageArea] = {
    da match {
      case DamageArea.Point           => Ok(ModelDamageArea.Point)
      case DamageArea.Blast           => Ok(ModelDamageArea.Blast)
      case DamageArea.Cone            => Ok(ModelDamageArea.Cone)
      case DamageArea.UniformBlast(_) => Ok(ModelDamageArea.UniformBlast)
    }
  }
}

case class WeaponImport(weapon: Weapon) extends Importable {

  override def updateLabel: String = s"weapon ${weapon.name}";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    val rowId = Some(idPool.generateRowId());
    val damageType = WeaponDamageTypeConverter.convert(weapon.damage.dmgType) match {
      case Ok(dt)   => dt
      case Err(msg) => return Err(msg)
    };
    val damageArea = WeaponDamageAreaConverter.convert(weapon.area) match {
      case Ok(da)   => da
      case Err(msg) => return Err(msg)
    }
    weapon.`type` match {
      case _: WeaponType.Melee => {
        char.createRepeating(MeleeWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(MeleeWeaponSection.skillSearch, rowId) <<= weapon.`type`.skill;
        char.createRepeating(MeleeWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(MeleeWeaponSection.numDamageDice, rowId) <<= weapon.damage.dmgD10;
        if (weapon.damage.dmgDiv != 1) {
          char.createRepeating(MeleeWeaponSection.damageDivisor, rowId) <<= weapon.damage.dmgDiv;
          char.createRepeating(MeleeWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(MeleeWeaponSection.damageBonus, rowId) <<= weapon.damage.dmgConst;
        char.createRepeating(MeleeWeaponSection.damageType, rowId).setWithWorker(damageType.toString);
        // char.createRepeating(MeleeWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(
        //   damageType
        // );
        char.createRepeating(MeleeWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(MeleeWeaponSection.skillName, rowId) <<= weapon.`type`.skill;
            char.createRepeating(MeleeWeaponSection.skillTotal, rowId) <<= MeleeWeaponSection.skillTotal.valueAt(
              skillId
            );
            Ok("Ok");
          }
          case None => {
            Ok(s"Could not find skill id for ${weapon.`type`.skill}.")
          }
        }
      }
      case _: WeaponType.Ranged | _: WeaponType.Thrown => {
        char.createRepeating(RangedWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(RangedWeaponSection.skillSearch, rowId) <<= weapon.`type`.skill;
        char.createRepeating(RangedWeaponSection.miscMod, rowId) <<= weapon.attackBonus;
        char.createRepeating(RangedWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(RangedWeaponSection.numDamageDice, rowId) <<= weapon.damage.dmgD10;
        if (weapon.damage.dmgDiv != 1) {
          char.createRepeating(RangedWeaponSection.damageDivisor, rowId) <<= weapon.damage.dmgDiv;
          char.createRepeating(RangedWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(RangedWeaponSection.damageBonus, rowId) <<= weapon.damage.dmgConst;
        char.createRepeating(RangedWeaponSection.damageType, rowId).setWithWorker(damageType.toString);
        // char.createRepeating(RangedWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(
        //   damageType
        // );
        char.createRepeating(RangedWeaponSection.damageArea, rowId).setWithWorker(damageArea.toString);
        // char.createRepeating(RangedWeaponSection.damageAreaShort, rowId) <<= ModelDamageArea.dynamicLabelShort(
        //   damageArea
        // );
        weapon.area match {
          case DamageArea.UniformBlast(r) => {
            char.createRepeating(RangedWeaponSection.uniformBlastArea, rowId) <<= r;
          }
          case _ => () // leave default
        }

        val thrownField = char.createRepeating(RangedWeaponSection.thrown, rowId);
        weapon.range match {
          case Range.Melee => return Err("Ranged weapons should have range Ranged")
          case r: Range.Thrown => {
            char.createRepeating(RangedWeaponSection.shortRangeUpperInput, rowId) <<= r.shortFactor;
            char.createRepeating(RangedWeaponSection.mediumRangeUpperInput, rowId) <<= r.mediumFactor;
            char.createRepeating(RangedWeaponSection.longRangeUpperInput, rowId) <<= r.longFactor;
            char.createRepeating(RangedWeaponSection.extremeRangeUpperInput, rowId) <<= r.extremeFactor;

            thrownField.setWithWorker(true); // this will cause dependent ranged to be calculated
          }
          case Range.Ranged(s, m, l, x) => {
            char.createRepeating(RangedWeaponSection.shortRangeUpperInput, rowId) <<= s;
            char.createRepeating(RangedWeaponSection.mediumRangeUpperInput, rowId) <<= m;
            char.createRepeating(RangedWeaponSection.longRangeUpperInput, rowId) <<= l;
            char.createRepeating(RangedWeaponSection.extremeRangeUpperInput, rowId) <<= x;

            thrownField.setWithWorker(false); // this will cause dependent ranged to be calculated
          }
        }

        weapon.gun match {
          case Some(GunExtras(modes, magazineSize)) => {
            char.createRepeating(RangedWeaponSection.singleShot, rowId) <<= modes.singleShot;
            char.createRepeating(RangedWeaponSection.semiAutomatic, rowId) <<= modes.semiAutomatic;
            char.createRepeating(RangedWeaponSection.burstFire, rowId) <<= modes.burstFire;
            char.createRepeating(RangedWeaponSection.fullAutomatic, rowId) <<= modes.fullAutomatic;

            val magazine = char.createRepeating(RangedWeaponSection.magazineCurrent, rowId);
            magazine <<= magazineSize;
            magazine.max = magazineSize.toString(); // TODO make max handling nicer
          }
          case None => {
            char.createRepeating(RangedWeaponSection.singleShot, rowId) <<= true;
            char.createRepeating(RangedWeaponSection.semiAutomatic, rowId) <<= false;
            char.createRepeating(RangedWeaponSection.burstFire, rowId) <<= false;
            char.createRepeating(RangedWeaponSection.fullAutomatic, rowId) <<= false;

            val magazine = char.createRepeating(RangedWeaponSection.magazineCurrent, rowId);
            magazine <<= 1;
            magazine.max = "1"; // TODO make max handling nicer
            char.createRepeating(RangedWeaponSection.magazineType, rowId) <<= "NA";
          }
        }

        char.createRepeating(RangedWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(RangedWeaponSection.skillName, rowId) <<= weapon.`type`.skill;
            char.createRepeating(RangedWeaponSection.skillTotal, rowId) <<= RangedWeaponSection.skillTotal.valueAt(
              skillId
            );
            Ok("Ok");
          }
          case None => {
            Ok(s"Could not find skill id for ${weapon.`type`.skill}.")
          }
        }
      }
      case _ => Err("Not implemented")
    }
  }
}

case class WeaponWithAmmoImport(weapon: WeaponWithAmmo) extends Importable {

  override def updateLabel: String = s"weapon ${weapon.name}";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    val rowId = Some(idPool.generateRowId());
    val damageType = WeaponDamageTypeConverter.convert(weapon.damage.dmgType) match {
      case Ok(dt)   => dt
      case Err(msg) => return Err(msg)
    };
    val damageArea = WeaponDamageAreaConverter.convert(weapon.area) match {
      case Ok(da)   => da
      case Err(msg) => return Err(msg)
    }
    weapon.weapon.`type` match {
      case _: WeaponType.Melee => {
        char.createRepeating(MeleeWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(MeleeWeaponSection.skillSearch, rowId) <<= weapon.weapon.`type`.skill;
        char.createRepeating(MeleeWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(MeleeWeaponSection.numDamageDice, rowId) <<= weapon.damage.dmgD10;
        if (weapon.damage.dmgDiv != 1) {
          char.createRepeating(MeleeWeaponSection.damageDivisor, rowId) <<= weapon.damage.dmgDiv;
          char.createRepeating(MeleeWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(MeleeWeaponSection.damageBonus, rowId) <<= weapon.damage.dmgConst;
        char.createRepeating(RangedWeaponSection.damageType, rowId).setWithWorker(damageType.toString);
        // char.createRepeating(RangedWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(
        //   damageType
        // );
        char.createRepeating(MeleeWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(MeleeWeaponSection.skillName, rowId) <<= weapon.weapon.`type`.skill;
            char.createRepeating(MeleeWeaponSection.skillTotal, rowId) <<= MeleeWeaponSection.skillTotal.valueAt(
              skillId
            );
            Ok("Ok");
          }
          case None => {
            Ok(s"Could not find skill id for ${weapon.weapon.`type`.skill}.")
          }
        }
      }
      case _: WeaponType.Ranged => {
        char.createRepeating(RangedWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(RangedWeaponSection.skillSearch, rowId) <<= weapon.weapon.`type`.skill;
        char.createRepeating(RangedWeaponSection.miscMod, rowId) <<= weapon.weapon.attackBonus;
        char.createRepeating(RangedWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(RangedWeaponSection.numDamageDice, rowId) <<= weapon.damage.dmgD10;
        if (weapon.damage.dmgDiv != 1) {
          char.createRepeating(RangedWeaponSection.damageDivisor, rowId) <<= weapon.damage.dmgDiv;
          char.createRepeating(RangedWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(RangedWeaponSection.damageBonus, rowId) <<= weapon.damage.dmgConst;
        char.createRepeating(RangedWeaponSection.damageType, rowId).setWithWorker(damageType.toString);
        // char.createRepeating(RangedWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(
        //   damageType
        // );
        char.createRepeating(RangedWeaponSection.damageArea, rowId).setWithWorker(damageArea.toString);
        // char.createRepeating(RangedWeaponSection.damageAreaShort, rowId) <<= ModelDamageArea.dynamicLabelShort(
        //   damageArea
        // );
        weapon.area match {
          case DamageArea.UniformBlast(r) => {
            char.createRepeating(RangedWeaponSection.uniformBlastArea, rowId) <<= r;
          }
          case _ => () // leave default
        }

        val thrownField = char.createRepeating(RangedWeaponSection.thrown, rowId);
        weapon.weapon.range match {
          case Range.Melee => return Err("Ranged weapons should have range Ranged")
          case r: Range.Thrown => {
            char.createRepeating(RangedWeaponSection.shortRangeUpperInput, rowId) <<= r.shortFactor;
            char.createRepeating(RangedWeaponSection.mediumRangeUpperInput, rowId) <<= r.mediumFactor;
            char.createRepeating(RangedWeaponSection.longRangeUpperInput, rowId) <<= r.longFactor;
            char.createRepeating(RangedWeaponSection.extremeRangeUpperInput, rowId) <<= r.extremeFactor;

            thrownField.setWithWorker(true); // this will cause dependent ranged to be calculated
          }
          case Range.Ranged(s, m, l, x) => {
            char.createRepeating(RangedWeaponSection.shortRangeUpperInput, rowId) <<= s;
            char.createRepeating(RangedWeaponSection.mediumRangeUpperInput, rowId) <<= m;
            char.createRepeating(RangedWeaponSection.longRangeUpperInput, rowId) <<= l;
            char.createRepeating(RangedWeaponSection.extremeRangeUpperInput, rowId) <<= x;

            thrownField.setWithWorker(false); // this will cause dependent ranged to be calculated
          }
        }

        weapon.weapon.gun match {
          case Some(GunExtras(modes, magazineSize)) => {
            char.createRepeating(RangedWeaponSection.singleShot, rowId) <<= modes.singleShot;
            char.createRepeating(RangedWeaponSection.semiAutomatic, rowId) <<= modes.semiAutomatic;
            char.createRepeating(RangedWeaponSection.burstFire, rowId) <<= modes.burstFire;
            char.createRepeating(RangedWeaponSection.fullAutomatic, rowId) <<= modes.fullAutomatic;

            val magazine = char.createRepeating(RangedWeaponSection.magazineCurrent, rowId);
            magazine <<= magazineSize;
            magazine.max = magazineSize.toString(); // TODO make max handling nicer
            char.createRepeating(RangedWeaponSection.magazineType, rowId) <<= weapon.ammo.name;
          }
          case None => {
            char.createRepeating(RangedWeaponSection.singleShot, rowId) <<= true;
            char.createRepeating(RangedWeaponSection.semiAutomatic, rowId) <<= false;
            char.createRepeating(RangedWeaponSection.burstFire, rowId) <<= false;
            char.createRepeating(RangedWeaponSection.fullAutomatic, rowId) <<= false;

            val magazine = char.createRepeating(RangedWeaponSection.magazineCurrent, rowId);
            magazine <<= 1;
            magazine.max = "1"; // TODO make max handling nicer
            char.createRepeating(RangedWeaponSection.magazineType, rowId) <<= weapon.ammo.name;
          }
        }

        char.createRepeating(RangedWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(RangedWeaponSection.skillName, rowId) <<= weapon.weapon.`type`.skill;
            char.createRepeating(RangedWeaponSection.skillTotal, rowId) <<= RangedWeaponSection.skillTotal.valueAt(
              skillId
            );
            Ok("Ok");
          }
          case None => {
            Ok(s"Could not find skill id for ${weapon.weapon.`type`.skill}.")
          }
        }
      }
      case _ => Err("Not implemented")
    }
  }
}
