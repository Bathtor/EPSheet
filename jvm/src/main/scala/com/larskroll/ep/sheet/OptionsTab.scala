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

object OptionsTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val members: Seq[SheetElement] = Seq(
    frow(
      sty.`flex-start`,
      fcol(
        Seq(sty.`flex-grow`, sty.exactly15rem, sty.marginr1rem),
        fblock(t.sheetSettings, EPStyle.min5rem,
          (t.chatOutput -> char.chatOutputSelect),
          char.chatOutput.hidden,
          flexFill)),
      fcol(
        Seq(sty.`flex-grow`, sty.exactly15rem, sty.marginr1rem),
        fblock(t.miscModifiers, EPStyle.min5rem,
          (t.woundsIgnored -> char.woundsIgnored),
          (t.miscActionMod -> char.miscActionMod),
          (t.miscPhysicalMod -> char.miscPhysicalMod),
          (t.miscInitiativeMod -> char.miscInitiativeMod),
          (t.miscDurBonus -> char.durabilityBonus),
          flexFill))),
    frow(
      sty.`flex-stretch`,
      fcol(
        Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
        block(
          t.miscNotes,
          char.miscNotes.like(CoreTabRenderer.largeTextareaField)))));

  override def renderer = CoreTabRenderer;
}
