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

object Blocks {
  def block(title: LabelsI18N, elems: SheetElement*): CoreBlock = CoreBlock(title, elems);
  def fblock(title: LabelsI18N, growStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FlexBlock = FlexBlock(Some(Left(title)), growStyle, elems);
  def fblock(title: TextField, growStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FlexBlock = FlexBlock(Some(Right(title)), growStyle, elems);
  def fblock(growStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FlexBlock = FlexBlock(None, growStyle, elems);
  def frow(alignStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FlexRow = FlexRow(alignStyle, elems);
  def fcol(growStyles: Seq[scalatags.stylesheet.Cls], elems: SheetElement*): FlexColumn = FlexColumn(growStyles, elems);
  def tightfrow(elems: SheetElement*): FieldGroup = GroupWithRenderer(TightFlexRow(None), elems);
  def tightfrow(sepStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FieldGroup = GroupWithRenderer(TightFlexRow(Some(sepStyle)), elems);
  def tightrow(elems: SheetElement*): FieldGroup = GroupWithRenderer(TightRow(None), elems);
  def tightrow(sepStyle: scalatags.stylesheet.Cls, elems: SheetElement*): FieldGroup = GroupWithRenderer(TightRow(Some(sepStyle)), elems);
  def tightcol(elems: SheetElement*): FieldGroup = GroupWithRenderer(TightCol, elems);
  def sblock(title: LabelsI18N, growStyle: scalatags.stylesheet.Cls, elems: SheetElement*): SmallBlock = SmallBlock(title, None, growStyle, elems);
  def sblock(title: LabelsI18N, titleRoll: Button, growStyle: scalatags.stylesheet.Cls, elems: SheetElement*): SmallBlock = SmallBlock(title, Some(titleRoll), growStyle, elems);
  def coreSeq(elems: SheetElement*) = GroupWithRenderer(CoreTabRenderer, elems);
  def eprow(elems: SheetElement*): FieldGroup = GroupWithRenderer(DivRenderer(Seq(EPStyle.`ep-row`)), elems);
  def ep2colrow(elems: SheetElement*): FieldGroup = GroupWithRenderer(DivRenderer(Seq(EPStyle.`ep-twocolrow`)), elems);
  def ep3colrow(elems: SheetElement*): FieldGroup = GroupWithRenderer(DivRenderer(Seq(EPStyle.`ep-threecolrow`)), elems);
  def epcol(elems: SheetElement*): FieldGroup = GroupWithRenderer(DivRenderer(Seq(EPStyle.`ep-col`)), elems);
  def pseudoButton(toggle: FlagField, buttonLabel: FieldLike[_]): PseudoButton = PseudoButton(toggle, Left(buttonLabel));
  def pseudoButton(toggle: FlagField, buttonLabel: LabelI18N): PseudoButton = PseudoButton(toggle, Right(buttonLabel));
  def note(s: String): Tag = div(EPStyle.note, span(EPStyle.inlineLabel, EPTranslation.note), span(s));
  def note(l: LabelI18N): Tag = div(EPStyle.note, span(EPStyle.inlineLabel, EPTranslation.note), span(l));
  def flowpar(elems: SheetElement*): FieldGroup = GroupWithRenderer(FlowPar, elems);
  def buttonSeq(elems: SheetElement*) = GroupWithRenderer(ButtonSeq, elems);
  def arrowList(elems: SheetElement*) = GroupWithRenderer(ArrowList, elems);
  def rwd(roll: RollElement, descriptions: LabelI18N*) = RollWithDescription(roll, descriptions);

  val flexFill = span(EPStyle.`flex-grow`, EPStyle.min1rem);
  val flexFillNarrow = span(EPStyle.`flex-grow`, EPStyle.min02rem);
  val flexBreak = div(width := "100%");
  //def roll(roll: Button, members: SheetElement*): RollContent = RollContent(roll, members);
}

case class RollWithDescription(roll: RollElement, descriptions: Seq[LabelI18N]) extends FieldGroup {
  override def renderer = coreBlockRenderer;

  override def members: Seq[SheetElement] = Seq(roll);

  val coreBlockRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      val descs = descriptions flatMap { d => Seq(span(EPStyle.smallDescription, ", "), span(EPStyle.smallDescription, d.attrs)) };
      span(tags, descs)
    };

    override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
      span(EPStyle.inlineLabelGroup,
        e,
        div(EPStyle.inlineLabel, l.attrs));

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case object ArrowList extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(tags map { t => div(EPStyle.rollList, t) })
  };

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.rollList,
      span(EPStyle.inlineLabel, l),
      e);
}

case object ButtonSeq extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(tags)
  };

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.inlineLabelGroup,
      span(EPStyle.inlineLabel, l),
      e);
}

case object FlowPar extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    p(EPStyle.flowPar, tags)
  };

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.inlineLabelGroup,
      span(EPStyle.inlineLabel, l),
      e);
}

case class CoreBlock(title: LabelsI18N, members: Seq[SheetElement]) extends FieldGroup {
  override def renderer = coreBlockRenderer;

  val coreBlockRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      div(EPStyle.wrapBox,
        tags,
        div(EPStyle.wrapBoxTitle, title.attrs))
    };

    override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
      div(EPStyle.labelGroup,
        e,
        div(EPStyle.subLabel, l.attrs));

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case class SmallBlock(title: LabelsI18N, titleRoll: Option[Button], growStyle: scalatags.stylesheet.Cls, members: Seq[SheetElement]) extends FieldGroup {
  override def renderer = coreBlockRenderer;

  val coreBlockRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      val baseHeader = span(title.attrs);
      val header = titleRoll match {
        case Some(roll) => div(EPStyle.smallWrapBoxTitle, renderRoll(roll, baseHeader))
        case None       => div(EPStyle.smallWrapBoxTitle, baseHeader)
      }
      div(EPStyle.wrapBox, growStyle,
        tags,
        header)
    };

    override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
      span(EPStyle.inlineLabelGroup,
        span(EPStyle.inlineLabel, l),
        e);

    override def renderDualModeWrapper(edit: Tag, pres: Tag): Tag = {
      div(display.inline, edit, pres);
    }

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case class FlexBlock(title: Option[Either[LabelsI18N, TextField]], growStyle: scalatags.stylesheet.Cls, members: Seq[SheetElement]) extends FieldGroup {
  override def renderer = flexBlockRenderer;

  val flexBlockRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      title match {
        case Some(Left(t)) => div(EPStyle.wrapBox,
          div(EPStyle.`flex-container`, tags),
          div(EPStyle.wrapBoxTitle, t.attrs))
        case Some(Right(f)) => div(EPStyle.wrapBox,
          div(EPStyle.`flex-container`, tags),
          div(EPStyle.wrapBoxTitle, span(name := f.name)))
        case None => div(EPStyle.wrapBox,
          div(EPStyle.`flex-container`, tags))
      }

    };

    override def renderRoll(roll: Button, e: Tag): Tag = {
      button(`type` := "roll", name := roll.name, EPStyle.wrapButton, value := roll.roll.render,
        e)
    }

    override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
      span(EPStyle.labelGroup, EPStyle.`flex-grow`, growStyle,
        e,
        div(EPStyle.subLabel, l.attrs));

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case class FlexRow(alignStyle: scalatags.stylesheet.Cls, members: Seq[SheetElement]) extends FieldGroup {
  override def renderer = flexRowRenderer;

  val flexRowRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      div(EPStyle.flexRow, EPStyle.`flex-container`, alignStyle, tags)
    };

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case class FlexColumn(growStyles: Seq[scalatags.stylesheet.Cls], members: Seq[SheetElement]) extends FieldGroup {

  override def renderer = flexColRenderer;

  val flexColRenderer = new GroupRenderer {
    import GroupRenderer._

    override def fieldCombiner: FieldCombiner = { tags =>
      div(growStyles, EPStyle.`flex-col`, tags)
    };

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

case class TightRow(sepStyle: Option[scalatags.stylesheet.Cls]) extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(sepStyle, tags)
  };

  override def fieldRenderers: FieldRenderer = {
    case (f, mode) => div(EPStyle.inlineContentGroup, CoreTabRenderer.fieldRenderers(f, mode))
  }

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.inlineLabelGroup,
      span(EPStyle.inlineLabel, l),
      e);

}

case class TightFlexRow(sepStyle: Option[scalatags.stylesheet.Cls]) extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.flexRow, EPStyle.`flex-container`, sepStyle, tags)
  };

  override def fieldRenderers: FieldRenderer = {
    case (f, mode) => div(EPStyle.inlineContentGroup, CoreTabRenderer.fieldRenderers(f, mode))
  }

  override def renderLabelled(l: LabelsI18N, e: Tag): Tag =
    div(EPStyle.inlineLabelGroup,
      span(EPStyle.inlineLabel, l),
      e);

}

case object TightCol extends GroupRenderer {
  import GroupRenderer._

  override def fieldCombiner: FieldCombiner = { tags =>
    div(EPStyle.tcol, tags)
  };

  override def fieldRenderers: FieldRenderer = {
    case (f, mode) => div(EPStyle.inlineContentGroup, CoreTabRenderer.fieldRenderers(f, mode))
  }
}

case class PseudoButton(toggle: FlagField, buttonLabel: Either[FieldLike[_], LabelI18N]) extends FieldGroup {
  val members: Seq[SheetElement] = Seq.empty;

  override def renderer = pbRenderer;

  val pbRenderer = new GroupRenderer {
    import GroupRenderer._
    import CoreTabRenderer.obool2Checked

    override def fieldCombiner: FieldCombiner = { tags =>
      label(TabbedStyle.pseudoButtonWrapper,
        input(`type` := "checkbox", name := toggle.name, toggle.defaultValue),
        buttonLabel match {
          case Left(f)  => span(name := f.name, SheetI18N.datai18nDynamic)
          case Right(l) => span(l)
        })
    };

    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
  }
}

//case class RollContent(roll: Button, members: Seq[SheetElement]) extends FieldGroup {
//  override def renderer = rollRenderer;
//
//  val rollRenderer = new GroupRenderer {
//    import GroupRenderer._
//
//    override def fieldCombiner: FieldCombiner = { tags =>
//      button(`type` := "roll", name := roll.name, EPStyle.wrapButton, value := roll.roll.render,
//        tags)
//    };
//
//    override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
//  }
//}
