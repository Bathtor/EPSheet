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
import com.lkroll.roll20.core._
import scalatags.Text.all._
import SheetImplicits._

object EPDamageTemplate extends RollTemplate {
  import FieldImplicits._
  override def name: String = "ep-damage";

  val t = EPTranslation;
  val sty = EPStyle;

  def explained(cf: Field[String], af: Field[String], rf: RollField[Int], de: LabelI18N): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageExplanation <<= de);

  def explained(cf: Field[String], al: LabelI18N, rf: RollField[Int], de: LabelI18N): TemplateApplication =
    apply(character <<= cf, attributeLabel <<= al, damageRoll <<= rf, damageExplanation <<= de);

  def apply(cf: Field[String], al: LabelI18N, rf: RollField[Int]): TemplateApplication =
    apply(character <<= cf, attributeLabel <<= al, damageRoll <<= rf);

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], dtf: Field[String], apf: Field[Int]): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageType <<= dtf, armourPenetration <<= apf);

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], dtf: Field[String], apf: Field[Int], apiApply: CommandButton): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageType <<= dtf, armourPenetration <<= apf, applyDamage <<= apiApply);

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], dtf: Field[String], apf: Field[Int], xdbf: CommandButton, xdfa: CommandButton): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageType <<= dtf, armourPenetration <<= apf, concentrateBF <<= xdbf, concentrateFA <<= xdfa);

  // **** Fields ****
  val character = attribute[String]("character");
  val attributeLabel = labeli18n("attribute-label");
  val attributeField = attribute[String]("attribute-field");
  val damageRoll = rollable[Int]("damage-roll");
  val damageType = attribute[String]("damage-type");
  val damageExplanation = labeli18n("damage-explanation");
  val armourPenetration = attribute[Int]("armour-penetration");
  val concentrateBF = button("concentrate-damage-bf");
  val concentrateFA = button("concentrate-damage-fa");
  val applyDamage = button("apply-damage");
  val applyCritDamage = button("apply-crit-damage");

  def damageResult(r: TemplateFields.RollableField[Int]) =
    p(span(t.damageInflicts), span(raw(" ")), span(fontWeight.bold, t.damageValue), span(": "), r);

  // **** Layout ****
  override def content: Tag = div(
    sty.`template-wrapper`,
    h3(character),
    exists(attributeLabel) {
      h4(attributeLabel)
    },
    exists(attributeField) {
      h4(attributeField)
    },
    p(span(t.damageInflicts), span(": "), damageRoll, span(raw(" ")), span(fontWeight.bold, t.damageValue),
      exists(damageType) {
        Seq(span(raw(" ")), span(damageType))
      }, exists(damageExplanation) {
        Seq(span(raw(" ")), span(damageExplanation))
      }),
    exists(armourPenetration) {
      p(span(sty.fieldvalue, armourPenetration), span(" "), span(fontWeight.bold, t.ap), span(" "), span(fontStyle.italic, t.orTotalAP))
    },
    exists(concentrateBF) {
      exists(concentrateFA) {
        Seq(
          h4(span(sty.`sub-header`, t.concentrateFire)),
          p(span(fontWeight.bold, t.burstFire), span(": "), concentrateBF),
          p(span(fontWeight.bold, t.fullAutomatic), span(": "), concentrateFA))
      }
    },
    exists(applyDamage) {
      Seq(
        h4(span(sty.`sub-header`, t.apiHead)),
        p(applyDamage),
        p(applyCritDamage))
    });
}
