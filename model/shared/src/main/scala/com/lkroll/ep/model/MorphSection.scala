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
package com.lkroll.ep.model

import com.lkroll.roll20.sheet.model._;
import com.lkroll.roll20.core._;

object MorphSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "morphs";

  val id = "id".editable(false).default("?");
  val active = "active".default(false);
  val morphLabel = "morph_label".default("Unnamed");
  val morphType = "type".options(MorphType).default(MorphType.None);
  val morphName = text("morphs_name");
  val morphLocation = text("morph_location");
  val description = text("morphs_description");
  val visibleGender = text("visible_gender");
  val visibleAge = text("visible_age");
  val traits = "traits".default("");
  val implants = "implants".default("");
  val mobilitySystem = "mobility_system".default("Walker 4/20");
  val durability = "durability".default(0);
  val armourEnergy = "armour_energy".default(0);
  val armourKinetic = "armour_kinetic".default(0);
  val aptitudeBoni = "aptitude_boni".default("");
  val aptitudeMax = "aptitude_max".default("");
  val skillBoni = "skill_boni".default("");
}
