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
import com.lkroll.ep.model.{ EPCharModel => epmodel, CharacterTraitSection, TraitType => ModelTraitType }
import APIImplicits._;

case class EgoTraitImport(t: EPTrait) extends Importable {

  implicit def ctt2mtt(tt: TraitType): ModelTraitType.TraitType = tt match {
    case TraitType.Positive => ModelTraitType.Positive
    case TraitType.Negative => ModelTraitType.Negative
    case TraitType.Neutral  => ModelTraitType.Neutral
  };

  override def updateLabel: String = t.name;
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    t.applicability match {
      case TraitApplicability.Ego | TraitApplicability.Both => {
        val rowId = Some(idPool.generateRowId());
        char.createRepeating(CharacterTraitSection.traitName, rowId) <<= t.name;
        char.createRepeating(CharacterTraitSection.traitType, rowId) <<= ctt2mtt(t.traitType).toString();
        char.createRepeating(CharacterTraitSection.traitTypeShort, rowId) <<= ModelTraitType.dynamicLabelShort(t.traitType);
        char.createRepeating(CharacterTraitSection.description, rowId) <<= t.descr;
        Ok("Ok")
      }
      case TraitApplicability.Morph => Err("Can't import morph traits!")
    }
  }
}
