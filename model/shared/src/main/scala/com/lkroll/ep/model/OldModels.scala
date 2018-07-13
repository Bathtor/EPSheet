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

object OldModels {
  object V4 extends SheetModel {
    import FieldImplicitsLabels._
    implicit val ctx = this.renderingContext;

    override def version = "1.4.0";

    val async = "async".default(false);
  }

  object V5 extends SheetModel {
    import FieldImplicitsLabels._
    implicit val ctx = this.renderingContext;

    override def version = "1.5.0";

    object MorphSection extends RepeatingSection {
      import FieldImplicits._;

      implicit val ctx = this.renderingContext;

      def name = "morphs";

      val aptitudeMax = "aptitude_max".default(""); // this causes to roll20 to think there's a current aptitude field
    }
  }

  object V8 extends SheetModel {
    import FieldImplicitsLabels._
    implicit val ctx = this.renderingContext;

    override def version = "1.8.0";

    val speed = "speed".default(1).validIn(0, 99, 1);
    val mentalOnlyActions = "mental_only_actions".default(0).validIn(0, 99, 1);
  }
}
