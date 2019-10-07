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

object EPInfoTemplate extends RollTemplate {
  override def name: String = "ep-info";

  val t = EPTranslation;
  val sty = EPStyle;

  val title = value[String]("title");
  val subtitle = value[String]("subtitle");
  val description = value[String]("description");
  val importButton = button("import");

  // **** Layout ****
  override def content: Tag =
    div(
      sty.`template-wrapper`,
      h3(title),
      switchExists(subtitle, {
        h4(importButton, raw("&nbsp;"), subtitle)
      }, {
        h4(importButton)
      }),
      allProps(title, subtitle, description, importButton) { (key, value) =>
        p(sty.tightkv, span(sty.tightkey, key), span(raw(" ")), span(value))
      },
      exists(description) {
        Seq(h4("Description"), p(description))
      }
    );
}
