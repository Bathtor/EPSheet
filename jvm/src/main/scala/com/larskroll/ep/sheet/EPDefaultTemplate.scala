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
package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._
import SheetImplicits._

object EPDefaultTemplate extends RollTemplate {
  override def name: String = "ep-default";

  val t = EPTranslation;
  val sty = EPStyle;

  def apply(cf: Field[String], l: LabelI18N, rf: RollField[Int], tf: RollField[Int]): TemplateApplication =
    apply(character <<= cf, attributeLabel <<= l, testRoll <<= rf, testTarget <<= tf);
  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], tf: RollField[Int]): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, testRoll <<= rf, testTarget <<= tf);
  def apply(cf: Field[String], af: Field[String], asf: Field[String], rf: RollField[Int], tf: RollField[Int]): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, attributeSubField <<= asf, testRoll <<= rf, testTarget <<= tf);
  def apply(cf: Field[String], af: Field[String], asf: Field[String], rf: RollField[Int], tf: RollField[Int], sm: CommandButton): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, attributeSubField <<= asf, testRoll <<= rf, testTarget <<= tf, successMacro <<= sm);
  def apply(cf: Field[String], af: Field[String], asf: Field[String], rf: RollField[Int], tf: RollField[Int], sm: CommandButton, smes30: CommandButton, smes60: CommandButton): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, attributeSubField <<= asf, testRoll <<= rf, testTarget <<= tf, successMacro <<= sm, successMacroES30 <<= smes30, successMacroES60 <<= smes60);

  // **** Fields ****
  val character = attribute[String]("character");
  val attributeLabel = labeli18n("attribute-label");
  val attributeField = attribute[String]("attribute-field");
  val attributeSubField = attribute[String]("attribute-subfield");
  val testRoll = rollable[Int]("test-roll");
  val testTarget = rollable[Int]("test-target"); // attribute plus rollquery
  val successMacro = button("success-macro");
  val successMacroES30 = button("success-macro-es30");
  val successMacroES60 = button("success-macro-es60");

  def rollSuccess(l: LabelI18N) = div(
    p(sty.`roll-success`,
      span(l), span(raw("! &raquo; ")), span(t.mos), span(": "), testRoll),
    rollLess(testRoll, 30) {
      exists(successMacro) {
        p(sty.`roll-success`, successMacro)
      }
    },
    rollGreater(testRoll, 29) {
      rollLess(testRoll, 60) {
        switchExists(successMacroES30, {
          p(sty.`roll-success`, successMacroES30)
        }, {
          exists(successMacro) {
            p(sty.`roll-success`, successMacro)
          }
        })
      }
    },
    rollGreater(testRoll, 59) {
      switchExists(successMacroES60, {
        p(sty.`roll-success`, successMacroES60)
      }, {
        exists(successMacro) {
          p(sty.`roll-success`, successMacro)
        }
      })
    });
  def rollFailure(l: LabelI18N) = p(sty.`roll-failure`,
    span(l), span(raw("... &raquo; ")), span(t.mof), span(": "), testRoll, raw(" - "), testTarget);
  // TODO write API script that replaces the inline math with the result

  // **** Layout ****
  override def content: Tag = div(sty.`template-wrapper`,
    h3(character),
    exists(attributeLabel) {
      h4(attributeLabel)
    },
    exists(attributeField) {
      switchExists(attributeSubField, {
        h4(attributeField, br, span(sty.`sub-header`, attributeSubField))
      }, {
        h4(attributeField)
      })
    },
    p(testRoll, raw("&nbsp;vs&nbsp;"), testTarget),
    rollTotal(testRoll, 0) {
      rollSuccess(t.rollAutoSuccess)
    },
    rollTotal(testRoll, 99) {
      rollFailure(t.rollAutoFailure)
    },
    not(rollTotal(testRoll, 0)) {
      not(rollTotal(testRoll, 99)) {
        Seq(
          not(rollWasCrit(testRoll)) {
            Seq(
              rollTotal(testRoll, testTarget) {
                rollSuccess(t.rollSuccess)
              },
              rollLess(testRoll, testTarget) {
                rollSuccess(t.rollSuccess)
              },
              rollGreater(testRoll, testTarget) {
                rollFailure(t.rollFailure)
              })
          },
          rollWasCrit(testRoll) {
            Seq(
              rollTotal(testRoll, testTarget) {
                rollSuccess(t.rollCritSuccess)
              },
              rollLess(testRoll, testTarget) {
                rollSuccess(t.rollCritSuccess)
              },
              rollGreater(testRoll, testTarget) {
                rollFailure(t.rollCritFailure)
              })
          })
      }
    });

}
