package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._

object OptionsTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val members: Seq[SheetElement] = Seq(
    frow(sty.`flex-start`,
      fcol(Seq(sty.`flex-grow`, sty.exactly15rem, sty.marginr1rem),
        fblock(t.sheetSettings, EPStyle.min5rem,
          //(t.woundsIgnored -> char.woundsIgnored),
          flexFill)),
      fcol(Seq(sty.`flex-grow`, sty.exactly15rem, sty.marginr1rem),
        fblock(t.miscModifiers, EPStyle.min5rem,
          (t.woundsIgnored -> char.woundsIgnored),
          flexFill))));

  override def renderer = CoreTabRenderer;
}
