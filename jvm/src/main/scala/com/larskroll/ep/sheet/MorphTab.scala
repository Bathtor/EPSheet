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

object MorphTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val morphsSection: SheetElement = char.morphs(
    eprow(
      epcol(fblock(char.morphs.morphLabel, EPStyle.min5rem,
        char.morphs.id.hidden,
        char.morphs.active,
        editOnly((t.morphLabel -> char.morphs.morphLabel)),
        (t.morphName -> dualMode(char.morphs.morphName)),
        (t.morphType -> dualMode(char.morphs.morphType)),
        (t.morphVisibleGender -> dualMode(char.morphs.visibleGender)),
        (t.morphVisibleAge -> dualMode(char.morphs.visibleAge)),
        (t.morphAptitudeMax -> dualMode(char.morphs.aptitudeMax)),
        (t.morphAptitudeBoni -> dualMode(char.morphs.aptitudeBoni)),
        (t.morphSkillBoni -> dualMode(char.morphs.skillBoni)),
        (t.morphDurability -> dualMode(char.morphs.durability)),
        (t.morphMobilitySystem -> dualMode(char.morphs.mobilitySystem)),
        (t.morphArmourEnergy -> dualMode(char.morphs.armourEnergy)),
        (t.morphArmourKinetic -> dualMode(char.morphs.armourKinetic)),
        (t.morphTraits -> dualMode(char.morphs.traits.like(CoreTabRenderer.textareaField))),
        (t.morphImplants -> dualMode(char.morphs.implants.like(CoreTabRenderer.textareaField))),
        (t.morphDescription -> dualMode(char.morphs.description.like(CoreTabRenderer.textareaField)))))));

  val members: Seq[SheetElement] = Seq(eprow(
    epcol(fblock(t.activeMorph, EPStyle.min5rem,
      char.morphSkillBoni.hidden,
      char.currentMorph.hidden,
      //char.morphLabel,
      (t.morphName -> char.morphName),
      (t.morphType -> char.morphType),
      (t.morphVisibleGender -> char.morphVisibleGender),
      (t.morphVisibleAge -> char.morphVisibleAge),
      (t.morphDurability -> char.morphDurability),
      (t.morphMobilitySystem -> char.morphMobilitySystem),
      (t.morphArmour -> coreSeq(char.morphArmourEnergy, span(" / "), char.morphArmourKinetic)),
      (t.morphTraits -> char.morphTraits),
      (t.morphImplants -> char.morphImplants),
      (t.morphDescription -> char.morphDescription)))),
    h2(sty.`h2hr`, t.morphBank),
    morphsSection);

  override def renderer = CoreTabRenderer;
}
