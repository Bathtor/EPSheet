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

import scalatags.Text.all._
import com.larskroll.roll20.sheet._
import SheetImplicits._

case class Aptitudes(rows: Seq[AptitudeRow]) extends FieldGroup {
  override def renderer = AptitudeRenderer;
  override def members: Seq[SheetElement] = rows.map(GroupElement(_));
}

object AptitudeRenderer extends GroupRenderer {

  val t = EPTranslation;
  val lsty = EPStyle.fieldLabel;

  override def fieldRenderers = DefaultRenderer.fieldRenderers;
  override def fieldCombiner = { tags =>
    table(EPStyle.aptTable,
      tr(td(EPStyle.`left-top-corner`), th(lsty, t.cog), th(lsty, t.coo), th(lsty, t.int), th(lsty, t.ref), th(lsty, t.sav), th(lsty, t.som), th(lsty, t.wil)),
      tags)
  };
}

case class AptitudeRow(rowName: LabelsI18N, members: Seq[SheetElement]) extends FieldGroup {

  import RenderMode._

  val aptitudeRowRenderer = new GroupRenderer {

    override def fieldRenderers: GroupRenderer.FieldRenderer = {
      case (f, _) if !f.editable() => td(
        span(name := f.name), input(`type` := "hidden", name := f.name, value := f.initialValue))
      case (f, _) if f.editable() => td(
        div(span(TabbedStyle.presentation, name := f.name)),
        div(span(TabbedStyle.edit, input(`type` := "number", name := f.name, value := f.initialValue, max := "99"))))
    };

    override def fieldCombiner = { tags =>
      tr(th(AptitudeRenderer.lsty, textAlign.right, rowName), tags)
    };
  }

  override def renderer = aptitudeRowRenderer;

}
