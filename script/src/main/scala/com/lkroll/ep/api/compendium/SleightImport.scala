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
import com.lkroll.ep.model.{ EPCharModel => epmodel, PsiChiSection, PsiGammaSection, PsiType => ModelPsiType }
import APIImplicits._;

object SleightConversions {
  def compendiumPsi2modelPsi(t: PsiType): ModelPsiType.PsiType = {
    t match {
      case _: PsiType.Active  => ModelPsiType.Active
      case _: PsiType.Passive => ModelPsiType.Passive
    }
  }
}

case class SleightImport(s: PsiSleight) extends Importable {
  override def updateLabel: String = s.name;
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());

    s.sleightType match {
      case SleightType.Chi => {
        char.createRepeating(PsiChiSection.sleight, rowId) <<= s.name;
        val psiType = SleightConversions.compendiumPsi2modelPsi(s.psiType);
        char.createRepeating(PsiChiSection.psiType, rowId) <<= psiType.toString;
        char.createRepeating(PsiChiSection.psiTypeShort, rowId) <<= ModelPsiType.dynamicLabelShort(psiType);
        char.createRepeating(PsiChiSection.range, rowId) <<= s.psiType.range.toString();
        char.createRepeating(PsiChiSection.action, rowId) <<= s.psiType.action;
        char.createRepeating(PsiChiSection.duration, rowId) <<= s.psiType.duration.label;
        char.createRepeating(PsiChiSection.strainMod, rowId) <<= s.psiType.strainMod.getOrElse(0);
        char.createRepeating(PsiChiSection.description, rowId) <<= s.descr;
        Left("Ok")
      }
      case SleightType.Epsilon | SleightType.Gamma => {
        char.createRepeating(PsiGammaSection.sleight, rowId) <<= s.name;
        val psiType = SleightConversions.compendiumPsi2modelPsi(s.psiType);
        char.createRepeating(PsiGammaSection.psiType, rowId) <<= psiType.toString;
        char.createRepeating(PsiGammaSection.psiTypeShort, rowId) <<= ModelPsiType.dynamicLabelShort(psiType);
        char.createRepeating(PsiGammaSection.range, rowId) <<= s.psiType.range.toString();
        char.createRepeating(PsiGammaSection.action, rowId) <<= s.psiType.action;
        char.createRepeating(PsiGammaSection.duration, rowId) <<= s.psiType.duration.label;
        char.createRepeating(PsiGammaSection.strainMod, rowId) <<= s.psiType.strainMod.getOrElse(0);
        char.createRepeating(PsiGammaSection.description, rowId) <<= s.descr;
        s.psiType.skill match {
          case Some(skill) => {
            cache.activeSkillId(skill) match {
              case Some(skillId) => {
                char.createRepeating(PsiGammaSection.skillSearch, rowId) <<= skill;
                char.createRepeating(PsiGammaSection.skillName, rowId) <<= skill;
                char.createRepeating(PsiGammaSection.skillTotal, rowId) <<= PsiGammaSection.skillTotal.valueAt(skillId);
                Left("Ok");
              }
              case None => {
                Left(s"Could not find skill id for ${skill}.")
              }
            }
          }
          case None => Left("Ok") // leave skill fields at default
        }
      }
    }
  }
}
