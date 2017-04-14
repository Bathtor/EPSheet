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
import SheetImplicits._

object Header extends FieldGroup {
  import Roll20Predef._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val members: Seq[SheetElement] = Seq(
    GroupWithRenderer(CharNameRenderer, Seq(t.charName -> dualMode(char.characterName))),
    GroupWithRenderer(LogoRenderer, Seq(
      img(src := "http://files.lars-kroll.com/EclipsePhase_Logo_Black.png", alt := "Eclipse Phase"),
      char.versionField like { f =>
        div(EPStyle.aRight,
          input(`type` := "hidden", name := f.name, value := f.initialValue),
          span(t.version), span(name := f.name))
      })));

  override def renderer = HeaderRenderer;
}

object HeaderRenderer extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.headerBox, EPStyle.`flex-container`, EPStyle.`flex-centre`, tags)
  };

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.labelGroup,
      e,
      div(EPStyle.subLabel, l));

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
    div(EPStyle.labelGroup,
      e,
      div(EPStyle.subLabel, l));

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
}
