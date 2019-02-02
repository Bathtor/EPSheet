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
import com.lkroll.roll20.sheet.tabbed.TabbedStyle
import com.lkroll.roll20.sheet.model._
import com.lkroll.ep.model._
import scalatags.Text.all._
import SheetImplicits._

object IdentitiesTab extends FieldGroup {
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val ci = char.identities;

  val repTable = RepTable(Seq(
    RepRow(ci.atNameShort, ci.atNameLong, Seq(ci.atRepScore, td(raw(" ")), ci.atRepFavour1, ci.atRepFavour2, ci.atRepFavour3, ci.atRepFavour4, ci.atRepFavour5)),
    RepRow(ci.cNameShort, ci.cNameLong, Seq(ci.cRepScore, td(raw(" ")), ci.cRepFavour1, ci.cRepFavour2, ci.cRepFavour3, ci.cRepFavour4, ci.cRepFavour5)),
    RepRow(ci.eNameShort, ci.eNameLong, Seq(ci.eRepScore, td(raw(" ")), ci.eRepFavour1, ci.eRepFavour2, ci.eRepFavour3, ci.eRepFavour4, ci.eRepFavour5)),
    RepRow(ci.fNameShort, ci.fNameLong, Seq(ci.fRepScore, td(raw(" ")), ci.fRepFavour1, ci.fRepFavour2, ci.fRepFavour3, ci.fRepFavour4, ci.fRepFavour5)),
    RepRow(ci.gNameShort, ci.gNameLong, Seq(ci.gRepScore, td(raw(" ")), ci.gRepFavour1, ci.gRepFavour2, ci.gRepFavour3, ci.gRepFavour4, ci.gRepFavour5)),
    RepRow(ci.iNameShort, ci.iNameLong, Seq(ci.iRepScore, td(raw(" ")), ci.iRepFavour1, ci.iRepFavour2, ci.iRepFavour3, ci.iRepFavour4, ci.iRepFavour5)),
    RepRow(ci.rNameShort, ci.rNameLong, Seq(ci.rRepScore, td(raw(" ")), ci.rRepFavour1, ci.rRepFavour2, ci.rRepFavour3, ci.rRepFavour4, ci.rRepFavour5)),
    RepRow(ci.uNameShort, ci.uNameLong, Seq(ci.uRepScore, td(raw(" ")), ci.uRepFavour1, ci.uRepFavour2, ci.uRepFavour3, ci.uRepFavour4, ci.uRepFavour5)),
    RepRow(ci.xNameShort, ci.xNameLong, Seq(ci.xRepScore, td(raw(" ")), ci.xRepFavour1, ci.xRepFavour2, ci.xRepFavour3, ci.xRepFavour4, ci.xRepFavour5))));

  val identityInfo = fblock(t.identity, EPStyle.min5rem,
    editOnly((t.identity -> ci.identity)),
    (t.idDescription -> dualMode(ci.description)),
    (t.idCredits -> ci.credits),
    flexBreak,
    (t.idNotes -> ci.notes.like(CoreTabRenderer.textareaField)));

  val nameBarrier: GroupRenderer.FieldSingleRenderer = (f) => span(sty.`h2hr`, name := f.name);

  val members: Seq[SheetElement] = Seq(char.identities(
    fcol(
      Seq(sty.marginr1rem),
      ci.identity.like(nameBarrier),
      identityInfo,
      block(
        t.reputation,
        repTable))));

  override def renderer = IdentitiesRenderer;
}

object IdentitiesRenderer extends CoreTabRenderer {
  import GroupRenderer._;

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.identities, tags)
  }
}

case class RepTable(rows: Seq[RepRow]) extends FieldGroup {
  override def members: Seq[SheetElement] = rows.map(GroupElement(_));

  val lsty = EPStyle.fieldLabelBold;
  val t = EPTranslation;

  override def renderer = new GroupRenderer() {
    override def fieldCombiner = { tags =>
      table(
        EPStyle.repTable,
        // roll20 deletes colgroup during sheet parsing -.-
        //colgroup(col(EPStyle.yLabelCol), col(EPStyle.repScoreCol), col(EPStyle.favLvlCol), col(EPStyle.favLvlCol), col(EPStyle.favLvlCol), col(EPStyle.favLvlCol), col(EPStyle.favLvlCol)),
        tr(height := "1px", td(EPStyle.yLabelCol, raw(" ")), td(EPStyle.repScoreCol, raw(" ")), td(EPStyle.spacerCol, raw(" ")), td(EPStyle.favLvlCol, raw(" ")), td(EPStyle.favLvlCol, raw(" ")), td(EPStyle.favLvlCol, raw(" ")), td(EPStyle.favLvlCol, raw(" ")), td(EPStyle.favLvlCol, raw(" "))),
        tr(td(colspan := 3), th(colspan := 5, EPStyle.secondTableHeader, t.calledInFavours)),
        tr(td(EPStyle.`left-top-corner`), th(lsty, t.repScore), th(lsty, raw(" ")), th(lsty, t.lvl1), th(lsty, t.lvl2), th(lsty, t.lvl3), th(lsty, t.lvl4, th(lsty, t.lvl5))),
        tags)
    };
    override def fieldRenderers = CoreTabRenderer.fieldRenderers;
  };
}
case class RepRow(rowNameShort: TextField, rowNameLong: TextField, members: Seq[SheetElement]) extends FieldGroup {
  import RenderMode._
  import CoreTabRenderer.obool2Checked

  override def renderer = new GroupRenderer {

    override def fieldRenderers: GroupRenderer.FieldRenderer = {
      case (f, _) if !f.editable() => td(
        span(name := f.name), input(`type` := "hidden", name := f.name, value := f.initialValue))
      case (f: NumberField[_], _) if f.editable => td(
        span(TabbedStyle.presentation, name := f.name),
        span(TabbedStyle.edit, CoreTabRenderer.renderNumberFieldNoWidth(f)))
      case (ff: FlagField, _) => td(input(`type` := "checkbox", name := ff.name, ff.defaultValue))
    };

    override def fieldCombiner = { tags =>
      tr(th(EPStyle.fieldLabelBold, textAlign.right,
        div(
          TabbedStyle.presentation,
          Blocks.tooltipped(span(SheetI18NAttrs.datai18nDynamic, name := rowNameShort.name), span(SheetI18NAttrs.datai18nDynamic, name := rowNameLong.name))),
        div(
          TabbedStyle.edit,
          input(EPStyle.max5em, `type` := "text", name := rowNameShort.name, value := rowNameShort.initialValue),
          raw("&nbsp;("), input(EPStyle.max8em, `type` := "text", name := rowNameLong.name, value := rowNameLong.initialValue), raw(")"))), tags)
    };
  }
}
