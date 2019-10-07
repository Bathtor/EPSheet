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
import com.lkroll.ep.model.APIOutputTemplateRef
import scalatags.Text.all._
import SheetImplicits._

object EPAPIOutputTemplate extends APIOutputRollTemplate {
  override def name: String = APIOutputTemplateRef.name;

  val t = EPTranslation;
  val sty = EPStyle;

  lazy val titleRender: Modifier = switchExists(isWarning, {
    // WARN
    h3(sty.`api-warn`, titleField)
  }, switchExists(isError, {
    // ERROR
    h3(sty.`api-error`, titleField)
  }, {
    // NORMAL
    h3(titleField)
  }));

  // **** Layout ****
  override def content: Tag =
    div(
      switchExists(
        showHeader, {
          switchExists(
            showFooter, {
              // FULL
              div(sty.`template-wrapper`, sty.`template-wrapper-full`, titleRender, div(contentField))
            }, {
              // Header-only
              div(sty.`template-wrapper`, sty.`template-wrapper-header`, titleRender, div(contentField))
            }
          )
        }, {
          switchExists(
            showFooter, {
              // Footer-only
              div(sty.`template-wrapper`, sty.`template-wrapper-footer`, div(contentField))
            }, {
              // Body
              div(sty.`template-wrapper`, sty.`template-wrapper-body`, div(contentField))
            }
          )
        }
      )
    );
}
