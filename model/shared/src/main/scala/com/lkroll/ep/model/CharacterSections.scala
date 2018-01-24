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

object CharacterTraitSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "charactertrait";

  val traitName = text("name");
  val traitType = "type".options(TraitType).default(TraitType.Neutral);
  val traitTypeShort = text("type_short").editable(false);
  val description = text("description");
}

object DerangementSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "derangements";

  val conditionName = text("name");
  val severity = "severity".options(DerangementSeverity).default(DerangementSeverity.Minor);
  val description = text("description");
  val duration = "duration".default(0.0).validIn(0.0, 24.0, 0.5);
}

object DisorderSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "disorders";

  val conditionName = text("name");
  val description = text("description");
  val treatmentRemaining = "treatment_remaining".default(40).validIn(0, 40, 1);
}
