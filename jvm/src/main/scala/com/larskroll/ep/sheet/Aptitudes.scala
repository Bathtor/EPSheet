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
  val lsty = EPStyle.fieldLabelBold;
  val char = EPCharModel;
  val sty = EPStyle;

  override def fieldRenderers = DefaultRenderer.fieldRenderers;
  override def fieldCombiner = { tags =>
    table(EPStyle.aptTable,
      tr(td(EPStyle.`left-top-corner`), th(lsty, cog), th(lsty, coo), th(lsty, int), th(lsty, ref), th(lsty, sav), th(lsty, som), th(lsty, wil)),
      tags)
  };

  val cog = renderElement(
    roll(char, "cog-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.cog.fullLabel, char.epRoll, char.cogTarget),
      span(fontWeight.bold, t.cog)),
    RenderMode.Normal);
  val coo = renderElement(
    roll(char, "coo-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.coo.fullLabel, char.epRoll, char.cooTarget),
      span(fontWeight.bold, t.coo)),
    RenderMode.Normal);
  val int = renderElement(
    roll(char, "int-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.int.fullLabel, char.epRoll, char.intTarget),
      span(fontWeight.bold, t.int)),
    RenderMode.Normal);
  val ref = renderElement(
    roll(char, "ref-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.ref.fullLabel, char.epRoll, char.refTarget),
      span(fontWeight.bold, t.ref)),
    RenderMode.Normal);
  val sav = renderElement(
    roll(char, "sav-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.sav.fullLabel, char.epRoll, char.savTarget),
      span(fontWeight.bold, t.sav)),
    RenderMode.Normal);
  val som = renderElement(
    roll(char, "som-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.som.fullLabel, char.epRoll, char.somTarget),
      span(fontWeight.bold, t.som)),
    RenderMode.Normal);
  val wil = renderElement(
    roll(char, "will-roll", Chat.Default,
      EPDefaultTemplate(char.characterName, t.wil.fullLabel, char.epRoll, char.wilTarget),
      span(fontWeight.bold, t.wil)),
    RenderMode.Normal);
}

case class MuseAptitudes(rows: Seq[AptitudeRow]) extends FieldGroup {
  override def renderer = MuseAptitudeRenderer;
  override def members: Seq[SheetElement] = rows.map(GroupElement(_));
}

object MuseAptitudeRenderer extends GroupRenderer {

  val t = EPTranslation;
  val lsty = EPStyle.fieldLabelBold;
  val char = EPCharModel;
  val sty = EPStyle;

  override def fieldRenderers = DefaultRenderer.fieldRenderers;
  override def fieldCombiner = { tags =>
    table(EPStyle.aptTable,
      tr(td(EPStyle.`left-top-corner`), th(lsty, cog), th(lsty, coo), th(lsty, int), th(lsty, ref), th(lsty, sav), th(lsty, som), th(lsty, wil)),
      tags)
  };

  val cog = renderElement(
    roll(char, "cog-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.cog.fullLabel, char.epRoll, char.museCogTarget),
      span(fontWeight.bold, t.cog)),
    RenderMode.Normal);
  val coo = renderElement(
    roll(char, "coo-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.coo.fullLabel, char.epRoll, char.museCooTarget),
      span(fontWeight.bold, t.coo)),
    RenderMode.Normal);
  val int = renderElement(
    roll(char, "int-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.int.fullLabel, char.epRoll, char.museIntTarget),
      span(fontWeight.bold, t.int)),
    RenderMode.Normal);
  val ref = renderElement(
    roll(char, "ref-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.ref.fullLabel, char.epRoll, char.museRefTarget),
      span(fontWeight.bold, t.ref)),
    RenderMode.Normal);
  val sav = renderElement(
    roll(char, "sav-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.sav.fullLabel, char.epRoll, char.museSavTarget),
      span(fontWeight.bold, t.sav)),
    RenderMode.Normal);
  val som = renderElement(
    roll(char, "som-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.som.fullLabel, char.epRoll, char.museSomTarget),
      span(fontWeight.bold, t.som)),
    RenderMode.Normal);
  val wil = renderElement(
    roll(char, "will-roll", Chat.Default,
      EPDefaultTemplate(char.museName, t.wil.fullLabel, char.epRoll, char.museWilTarget),
      span(fontWeight.bold, t.wil)),
    RenderMode.Normal);
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
