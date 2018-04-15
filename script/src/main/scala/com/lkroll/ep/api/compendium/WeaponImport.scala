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
import com.lkroll.ep.model.{ EPCharModel => epmodel, MeleeWeaponSection, RangedWeaponSection, DamageType => ModelDamageType }
import APIImplicits._;

case class WeaponImport(weapon: Weapon) extends Importable {

  implicit def cdt2mdt(dt: DamageType): ModelDamageType.DamageType = dt match {
    case DamageType.Energy  => ModelDamageType.Energy
    case DamageType.Kinetic => ModelDamageType.Kinetic
  }

  override def updateLabel: String = s"weapon ${weapon.name}";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());
    weapon.`type` match {
      case _: WeaponType.Melee => {
        char.createRepeating(MeleeWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(MeleeWeaponSection.skillSearch, rowId) <<= weapon.`type`.skill;
        char.createRepeating(MeleeWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(MeleeWeaponSection.numDamageDice, rowId) <<= weapon.dmgD10;
        if (weapon.dmgDiv != 1) {
          char.createRepeating(MeleeWeaponSection.damageDivisor, rowId) <<= weapon.dmgDiv;
          char.createRepeating(MeleeWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(MeleeWeaponSection.damageBonus, rowId) <<= weapon.dmgConst;
        char.createRepeating(MeleeWeaponSection.damageType, rowId) <<= weapon.dmgType.label;
        char.createRepeating(MeleeWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(weapon.dmgType);
        char.createRepeating(MeleeWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(MeleeWeaponSection.skillName, rowId) <<= weapon.`type`.skill;
            char.createRepeating(MeleeWeaponSection.skillTotal, rowId) <<= MeleeWeaponSection.skillTotal.valueAt(skillId);
            Left("Ok");
          }
          case None => {
            Left(s"Could not find skill id for ${weapon.`type`.skill}.")
          }
        }
      }
      case _: WeaponType.Ranged => {
        char.createRepeating(RangedWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(RangedWeaponSection.skillSearch, rowId) <<= weapon.`type`.skill;
        char.createRepeating(RangedWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(RangedWeaponSection.numDamageDice, rowId) <<= weapon.dmgD10;
        if (weapon.dmgDiv != 1) {
          char.createRepeating(RangedWeaponSection.damageDivisor, rowId) <<= weapon.dmgDiv;
          char.createRepeating(RangedWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(RangedWeaponSection.damageBonus, rowId) <<= weapon.dmgConst;
        char.createRepeating(RangedWeaponSection.damageType, rowId) <<= weapon.dmgType.label;
        char.createRepeating(RangedWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(weapon.dmgType);

        weapon.range match {
          case Range.Melee     => return Right("Ranged weapons should have range Ranged")
          case _: Range.Thrown => return Right("Thrown weapons are currently not supported")
          case Range.Ranged(s, m, l, x) => {
            //char.createRepeating(RangedWeaponSection.shortRangeLower, rowId) <<= 2; // below this is point blank but don't write default value
            char.createRepeating(RangedWeaponSection.shortRangeUpper, rowId) <<= s;
            char.createRepeating(RangedWeaponSection.mediumRangeLower, rowId) <<= s + 1;
            char.createRepeating(RangedWeaponSection.mediumRangeUpper, rowId) <<= m;
            char.createRepeating(RangedWeaponSection.longRangeLower, rowId) <<= m + 1;
            char.createRepeating(RangedWeaponSection.longRangeUpper, rowId) <<= l;
            char.createRepeating(RangedWeaponSection.extremeRangeLower, rowId) <<= l + 1;
            char.createRepeating(RangedWeaponSection.extremeRangeUpper, rowId) <<= x;
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
          case None => APILogger.warn(s"Weapon ${weapon.name} does not seems to be gun, despite being of ranged type.")
        }

        char.createRepeating(RangedWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(RangedWeaponSection.skillName, rowId) <<= weapon.`type`.skill;
            char.createRepeating(RangedWeaponSection.skillTotal, rowId) <<= RangedWeaponSection.skillTotal.valueAt(skillId);
            Left("Ok");
          }
          case None => {
            Left(s"Could not find skill id for ${weapon.`type`.skill}.")
          }
        }
      }
      case _ => Right("Not implemented")
    }
  }
}

case class WeaponWithAmmoImport(weapon: WeaponWithAmmo) extends Importable {

  implicit def cdt2mdt(dt: DamageType): ModelDamageType.DamageType = dt match {
    case DamageType.Energy  => ModelDamageType.Energy
    case DamageType.Kinetic => ModelDamageType.Kinetic
  }

  override def updateLabel: String = s"weapon ${weapon.name}";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());
    weapon.weapon.`type` match {
      case _: WeaponType.Melee => {
        char.createRepeating(MeleeWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(MeleeWeaponSection.skillSearch, rowId) <<= weapon.weapon.`type`.skill;
        char.createRepeating(MeleeWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(MeleeWeaponSection.numDamageDice, rowId) <<= weapon.dmgD10;
        if (weapon.dmgDiv != 1) {
          char.createRepeating(MeleeWeaponSection.damageDivisor, rowId) <<= weapon.dmgDiv;
          char.createRepeating(MeleeWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(MeleeWeaponSection.damageBonus, rowId) <<= weapon.dmgConst;
        char.createRepeating(MeleeWeaponSection.damageType, rowId) <<= weapon.dmgType.label;
        char.createRepeating(MeleeWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(weapon.dmgType);
        char.createRepeating(MeleeWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(MeleeWeaponSection.skillName, rowId) <<= weapon.weapon.`type`.skill;
            char.createRepeating(MeleeWeaponSection.skillTotal, rowId) <<= MeleeWeaponSection.skillTotal.valueAt(skillId);
            Left("Ok");
          }
          case None => {
            Left(s"Could not find skill id for ${weapon.weapon.`type`.skill}.")
          }
        }
      }
      case _: WeaponType.Ranged => {
        char.createRepeating(RangedWeaponSection.weapon, rowId) <<= weapon.name;
        char.createRepeating(RangedWeaponSection.skillSearch, rowId) <<= weapon.weapon.`type`.skill;
        char.createRepeating(RangedWeaponSection.armourPenetration, rowId) <<= weapon.ap;
        char.createRepeating(RangedWeaponSection.numDamageDice, rowId) <<= weapon.dmgD10;
        if (weapon.dmgDiv != 1) {
          char.createRepeating(RangedWeaponSection.damageDivisor, rowId) <<= weapon.dmgDiv;
          char.createRepeating(RangedWeaponSection.showDivisor, rowId) <<= true;
        }
        char.createRepeating(RangedWeaponSection.damageBonus, rowId) <<= weapon.dmgConst;
        char.createRepeating(RangedWeaponSection.damageType, rowId) <<= weapon.dmgType.label;
        char.createRepeating(RangedWeaponSection.damageTypeShort, rowId) <<= ModelDamageType.dynamicLabelShort(weapon.dmgType);

        weapon.weapon.range match {
          case Range.Melee     => return Right("Ranged weapons should have range Ranged")
          case _: Range.Thrown => return Right("Thrown weapons are currently not supported")
          case Range.Ranged(s, m, l, x) => {
            //char.createRepeating(RangedWeaponSection.shortRangeLower, rowId) <<= 2; // below this is point blank but don't write default value
            char.createRepeating(RangedWeaponSection.shortRangeUpper, rowId) <<= s;
            char.createRepeating(RangedWeaponSection.mediumRangeLower, rowId) <<= s + 1;
            char.createRepeating(RangedWeaponSection.mediumRangeUpper, rowId) <<= m;
            char.createRepeating(RangedWeaponSection.longRangeLower, rowId) <<= m + 1;
            char.createRepeating(RangedWeaponSection.longRangeUpper, rowId) <<= l;
            char.createRepeating(RangedWeaponSection.extremeRangeLower, rowId) <<= l + 1;
            char.createRepeating(RangedWeaponSection.extremeRangeUpper, rowId) <<= x;
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
          case None => APILogger.warn(s"Weapon ${weapon.name} does not seems to be gun, despite being of ranged type.")
        }

        char.createRepeating(RangedWeaponSection.description, rowId) <<= weapon.descr;
        cache.activeSkillId(weapon.weapon.`type`.skill) match {
          case Some(skillId) => {
            char.createRepeating(RangedWeaponSection.skillName, rowId) <<= weapon.weapon.`type`.skill;
            char.createRepeating(RangedWeaponSection.skillTotal, rowId) <<= RangedWeaponSection.skillTotal.valueAt(skillId);
            Left("Ok");
          }
          case None => {
            Left(s"Could not find skill id for ${weapon.weapon.`type`.skill}.")
          }
        }
      }
      case _ => Right("Not implemented")
    }
  }
}
