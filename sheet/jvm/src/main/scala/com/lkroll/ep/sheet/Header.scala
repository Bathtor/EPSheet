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
import com.lkroll.ep.model.{EPTranslation => TranslationKeys, _}
import scalatags.Text.all._
import SheetImplicits._

object Header extends FieldGroup {
  import Roll20Predef._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val members: Seq[SheetElement] = Seq(
    GroupWithRenderer(
      LogoRenderer,
      Seq(
        div(sty.logoPlaceholder),
        char.versionField like { f =>
          div(sty.aRight,
              input(`type` := "hidden", name := f.name, value := f.initialValue),
              span(t.version),
              span(name := f.name))
        }
      )
    ),
    GroupWithRenderer(CharNameRenderer, Seq(t.charName -> dualMode(char.characterName)))
  );

  override def renderer = HeaderRenderer;
}

object HeaderRenderer extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.headerBox, EPStyle.`flex-container`, EPStyle.`flex-header`, tags)
  };

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.labelGroup, e, div(EPStyle.subLabel, l));

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
}

object LogoRenderer extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.logoBox, tags)
  };
  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
}

object CharNameRenderer extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.wrapBox, EPStyle.largeText, EPStyle.`flex-grow`, EPStyle.margin5px, tags)
  };

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.labelGroup, e, div(EPStyle.subLabel, l));

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
}
