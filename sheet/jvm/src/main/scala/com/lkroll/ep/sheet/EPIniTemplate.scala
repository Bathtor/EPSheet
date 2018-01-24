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

object EPIniTemplate extends RollTemplate {
  override def name: String = "ep-ini";

  def apply(cf: Field[String], rf: RollField[Int]): TemplateApplication = apply(character <<= cf, iniRoll <<= rf);

  val t = EPTranslation;
  val sty = EPStyle;

  // **** Fields ****
  val character = attribute[String]("character");
  val iniRoll = rollable[Int]("ini-roll");

  // **** Layout ****
  override def content: Tag = div(
    sty.`template-wrapper`,
    h3(character),
    p(span(t.rollsfor), raw("&nbsp;"), span(fontWeight.bold, t.init), raw(":&nbsp;"), iniRoll));
}

