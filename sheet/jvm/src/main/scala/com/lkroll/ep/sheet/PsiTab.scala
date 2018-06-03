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
package com.lkroll.ep.sheet

import com.lkroll.roll20.sheet._
import com.lkroll.roll20.sheet.model._
import com.lkroll.ep.model._
import scalatags.Text.all._

object PsiTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val ptRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
    sup(span(EPStyle.`cat-tag-field`, name := f.name, SheetI18NAttrs.datai18nDynamic))
  }

  val chiStrainDamageRoll = roll(char.psiChi, "strain_damage_roll", char.chatOutputEPRolls, EPDamageTemplate.strain(char.characterName, char.psiChi.sleight, char.psiChi.strainDamage, t.strain),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.strain),
      span(raw("1d10/2+")),
      char.psiChi.strainMod));

  val gammaStrainDamageRoll = roll(char.psiGamma, "strain_damage_roll", char.chatOutputOther, EPDamageTemplate.strain(char.characterName, char.psiGamma.sleight, char.psiGamma.strainDamage, t.strain),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.strain),
      span(raw("1d10/2+")),
      char.psiGamma.strainMod));

  val gammaSleightRoll = roll(char.psiGamma, "sleight_roll", char.chatOutputEPRolls,
    EPDefaultTemplate(char.characterName, char.psiGamma.skillName, char.psiGamma.sleight, char.epRoll, char.psiGamma.attackTarget),
    char.psiGamma.sleight.like(GearTab.rowItemName));

  val psiChi = block(
    t.psiChi,
    char.psiChi {
      TightRepRow(
        presOnly(flowpar(
          char.psiChi.sleight.like(GearTab.rowItemName),
          char.psiChi.psiTypeShort.like(ptRenderer),
          span(raw(" [")),
          chiStrainDamageRoll,
          span(raw("] ")),
          char.psiChi.range.like(CoreTabRenderer.italic),
          span(raw(" ~ ")),
          char.psiChi.action,
          span(raw(" / ")),
          char.psiChi.duration,
          char.psiChi.description.like(CoreTabRenderer.inlineDescription),
          flexFill)),
        editOnly(tightfrow(
          sty.halfRemRowSeparator,
          char.psiChi.sleight.like(CoreTabRenderer.textWithPlaceholder(t.sleightName.placeholder)),
          char.psiChi.psiType,
          char.psiChi.psiTypeShort.hidden,
          (t.psiRange -> char.psiChi.range),
          (t.psiAction -> char.psiChi.action),
          (t.psiDuration -> char.psiChi.duration),
          (t.strainMod -> char.psiChi.strainMod),
          span(EPStyle.inlineLabel, t.sleightDescription),
          MarkupElement(char.psiChi.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });
  val psiGamma = block(
    t.psiGamma,
    char.psiGamma {
      TightRepRow(
        presOnly(flowpar(
          gammaSleightRoll,
          char.psiGamma.psiTypeShort.like(ptRenderer),
          span(raw(" (")),
          char.psiGamma.skillName,
          span(raw(") ")),
          span(raw("[")),
          gammaStrainDamageRoll,
          span(raw("] ")),
          char.psiGamma.range.like(CoreTabRenderer.italic),
          span(raw(" ~ ")),
          char.psiGamma.action,
          span(raw(" / ")),
          char.psiGamma.duration,
          char.psiGamma.description.like(CoreTabRenderer.inlineDescription),
          flexFill)),
        editOnly(tightfrow(
          sty.halfRemRowSeparator,
          char.psiGamma.sleight.like(CoreTabRenderer.textWithPlaceholder(t.sleightName.placeholder)),
          char.psiGamma.psiType,
          char.psiGamma.psiTypeShort.hidden,
          span(EPStyle.inlineLabel, t.psiSkill),
          char.psiGamma.skillSearch.like(GearTab.skillSearchBox),
          char.psiGamma.skillName,
          char.psiGamma.skillTotal.hidden,
          (t.psiRange -> char.psiGamma.range),
          (t.psiAction -> char.psiGamma.action),
          (t.psiDuration -> char.psiGamma.duration),
          (t.strainMod -> char.psiGamma.strainMod),
          span(EPStyle.inlineLabel, t.sleightDescription),
          MarkupElement(char.psiGamma.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val members: Seq[SheetElement] = Seq(
    eprow(frow(
      sty.`flex-centre`,
      flexFillNarrow,
      sblock(t.async, sty.max20rem,
        char.async, span(raw("&nbsp;")), span(t.asyncTrait)),
      flexFillNarrow,
      sblock(t.psiDuration, sty.max20rem,
        (t.psiTempTime -> char.psiTempTime), span(t.psiTempUnits)),
      flexFillNarrow,
      sblock(t.psiSustained, sty.max20rem,
        (t.psiCurrent -> char.psiCurrentSustained),
        (t.psiSustainedMod -> char.psiSustainedMod)),
      flexFillNarrow)),
    frow(
      sty.`flex-start`,
      fcol(
        Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem),
        psiChi),
      fcol(
        Seq(EPStyle.`flex-grow`, sty.exactly20rem),
        psiGamma)));

  override def renderer = CoreTabRenderer;
}
