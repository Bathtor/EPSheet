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
import com.lkroll.ep.compendium.utils.OptionPickler._
import com.lkroll.ep.model.{ EPCharModel => epmodel, MorphSection }
import APIImplicits._;

case class MorphImport(morph: Morph) extends Importable {
  override def updateLabel: String = morph.label match {
    case Some(l) => s"morph $l (${morph.model})"
    case None    => s"morph ${morph.model}"
  }
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());
    char.createRepeating(MorphSection.id, rowId) <<= rowId.get;
    morph.label match {
      case Some(l) => char.createRepeating(MorphSection.morphLabel, rowId) <<= l;
      case None    => char.createRepeating(MorphSection.morphLabel, rowId) <<= s"Unnamed ${morph.model}";
    }
    char.createRepeating(MorphSection.morphType, rowId) <<= morph.morphType.label;
    char.createRepeating(MorphSection.morphName, rowId) <<= morph.model;
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
    char.createRepeating(MorphSection.skillBoni, rowId) <<= write(morph.skillBonus);
    morph.playerDecisions match {
      case Some(s) => Left(s"TODO: $s")
      case None    => Left("Ok")
    }
  }
  override def children: List[Importable] = morph.attacks.map(a => WeaponImport(a)).toList;
}
