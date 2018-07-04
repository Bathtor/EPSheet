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
import com.lkroll.ep.model.{ EPCharModel => epmodel, GearSection, EffectsSection }
import APIImplicits._;

case class SubstanceImport(s: Substance) extends Importable {
  override def updateLabel: String = s"${s.name} (${s.category})";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val adcStr = s.addiction.map(a => s"${a.`type`.entryName} with modifier ${a.modStr}").getOrElse("–");
    val effects = s.effects.map(_.text).mkString(",");
    val extraDescr = s"""in category ${s.category} ${s.application.map(_.shortLabel).mkString("(", ",", ")")} of type ${s.classification.label}
Addiction: $adcStr
Onset Time: ${s.onset.renderShort}, Duration: ${s.duration.renderShort}
---
""" + s.descr;
    val gearRowId = Some(idPool.generateRowId());
    char.createRepeating(GearSection.itemName, gearRowId) <<= s.name;
    char.createRepeating(GearSection.description, gearRowId) <<= extraDescr;
    val effectsRowId = Some(idPool.generateRowId());
    char.createRepeating(EffectsSection.effectName, effectsRowId) <<= s.name;
    char.createRepeating(EffectsSection.duration, effectsRowId) <<= s.duration.renderShort;
    char.createRepeating(EffectsSection.gameEffect, effectsRowId) <<= effects;
    char.createRepeating(EffectsSection.description, effectsRowId) <<= extraDescr;

    Left("Ok")
  }
}
