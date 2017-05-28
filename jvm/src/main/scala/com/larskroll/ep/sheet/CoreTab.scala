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

object CoreTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val aptitudes = Aptitudes(Seq(
    AptitudeRow(t.aptBase, Seq(char.cogBase, char.cooBase, char.intBase, char.refBase, char.savBase, char.somBase, char.wilBase)),
    AptitudeRow(t.aptMorphBonus, Seq(char.cogMorph, char.cooMorph, char.intMorph, char.refMorph, char.savMorph, char.somMorph, char.wilMorph)),
    AptitudeRow(t.aptMorphMax, Seq(char.cogMorphMax, char.cooMorphMax, char.intMorphMax, char.refMorphMax, char.savMorphMax, char.somMorphMax, char.wilMorphMax)),
    AptitudeRow(t.aptTotal, Seq(char.cogTotal, char.cooTotal, char.intTotal, char.refTotal, char.savTotal, char.somTotal, char.wilTotal))));

  val members: Seq[SheetElement] = Seq(
    eprow(frow(sty.`flex-centre`,
      flexFill,
      sblock(t.mox,
        roll(char, "moxie-roll", Chat.Default, EPDefaultTemplate(char.characterName, t.mox.fullLabel, char.epRoll, char.moxieTarget)),
        sty.max10rem,
        char.currentMoxie, span(" / "), dualMode(char.moxie)),
      flexFill,
      sblock(t.mentalHealth, sty.max15rem,
        (t.stress -> char.stress),
        (t.trauma -> char.trauma)),
      flexFill,
      sblock(t.physicalHealth, sty.max15rem,
        (t.damage -> char.damage),
        (t.wounds -> char.wounds)),
      flexFill,
      sblock(t.armour, sty.max15rem,
        (t.energy -> char.armourEnergyTotal),
        (t.kinetic -> char.armourKineticTotal)),
      flexFill,
      sblock(t.rezPoints, sty.max5rem, char.rezPoints),
      flexFill)),
    frow(sty.`flex-start`,
      fcol(Seq(EPStyle.`flex-grow`, EPStyle.exactly15rem, EPStyle.marginr1rem), fblock(t.characterInfo, EPStyle.min5rem,
        (t.background -> dualMode(char.background)),
        (t.faction -> dualMode(char.faction)),
        (t.genderId -> dualMode(char.genderId)),
        (t.actualAge -> dualMode(char.actualAge)),
        (t.motivations -> dualMode(char.motivations.like(CoreTabRenderer.textareaField))))),
      fcol(Seq(EPStyle.exactly23rem), block(t.aptitudes,
        aptitudes),
        fblock(t.stats, EPStyle.max2p5rem,
          (t.tt -> char.traumaThreshold),
          (t.luc -> char.lucidity),
          (t.ir -> char.insanityRating),
          (t.wt -> char.woundThreshold),
          (t.dur -> char.durability),
          (t.dr -> char.deathRating),
          roll(char, "initiative-roll", Chat.Default, EPIniTemplate(char.characterName, char.iniRoll), (t.init -> char.initiative)),
          (t.spd -> dualMode(char.speed)),
          (t.db -> char.damageBonus)))));

  override def renderer = CoreTabRenderer;

}

object CoreTabRenderer extends GroupRenderer {
  import GroupRenderer._
  import RenderMode._

  implicit def obool2Checked(ob: Option[Boolean]): Modifier = ob match {
    case Some(true)  => checked
    case Some(false) => ()
    case None        => ()
  }

  def renderNumberField[N](f: NumberField[N]): Tag = {
    import Math.{ max => nmax, abs, floor, log10 };
    implicit val nev = f.numericEvidence;
    f.valid match {
      case Some(NumberValidity(minV, maxV, stepV)) => {
        val (minD, maxD, stepD) = (nev.toDouble(minV), nev.toDouble(maxV), nev.toDouble(stepV));
        assert(maxD > minD);
        val largestBound = nmax(abs(maxD), abs(minD));
        val integerDigits = floor(log10(abs(largestBound)) + 2.0).toInt; // +1 for first digit and +1 for possible sign
        var count = 0;
        var v = stepD;
        while (!v.isWhole()) {
          v *= 10.0;
          count += 1;
        }
        val decimalDigits = count + 1; // for the decimal point
        val digits = integerDigits + decimalDigits;
        span(display.`inline-block`, maxWidth := digits.em,
          input(`type` := "number", name := f.name, value := f.initialValue, min := minV.toString(),
            max := maxV.toString(), step := stepV.toString()))
      }
      case None => {
        span(EPStyle.max3charinline, input(`type` := "number", name := f.name, value := f.initialValue))
      }
    }

  }

  override def fieldRenderers: FieldRenderer = {
    case (b: Button, _) =>
      button(`type` := "roll", name := b.name, value := b.roll.render)
    case (f: AutocalcField[_], _) =>
      span(input(`type` := "hidden", name := f.name, value := f.initialValue), span(name := f.name))
    case (f: Field[_], Normal) if f.editable() => f match {
      case n: NumberField[_] => renderNumberField(n)
      case ff: FlagField     => input(`type` := "checkbox", name := ff.name, ff.defaultValue)
      case ef: EnumField => ef.enum match {
        case Some(e) => EPTranslation.allFullOptions.get(e) match {
          case Some(l) => select(name := ef.name, ef.options.map(o => option(value := o, l.apply(o).attr)).toSeq)
          case None    => println(s"Translation missing for enumeration: ${e.toString()}"); select(name := ef.name, ef.options.map(o => option(value := o, o)).toSeq)
        }
        case None => select(name := ef.name, ef.options.map(o => option(value := o, o)).toSeq)
      }
      case _ => input(`type` := "text", name := f.name, value := f.initialValue)
    }
    case (f: Field[_], Presentation) if f.editable() => f match {
      case ff: FlagField => input(`type` := "checkbox", name := ff.name, ff.defaultValue)
      case _             => span(name := f.name)
    }

    case (f: Field[_], Edit) if f.editable() => f match {
      case n: NumberField[_] => renderNumberField(n)
      case ff: FlagField     => input(`type` := "checkbox", name := ff.name, ff.defaultValue)
      case ef: EnumField => ef.enum match {
        case Some(e) => EPTranslation.allFullOptions.get(e) match {
          case Some(l) => select(name := ef.name, ef.options.map(o => option(value := o, l.apply(o).attr)).toSeq)
          case None    => println(s"Translation missing for enumeration: ${e.toString()}"); select(name := ef.name, ef.options.map(o => option(value := o, o)).toSeq)
        }
        case None => select(name := ef.name, ef.options.map(o => option(value := o, o)).toSeq)
      }

      case _ => input(`type` := "text", name := f.name, value := f.initialValue)
    }
    case (f: Field[_], _) if !(f.editable()) =>
      span(input(`type` := "hidden", name := f.name, value := f.initialValue), span(name := f.name))
  };

  val textareaField: FieldDualRenderer = (f, mode) => {
    mode match {
      case RenderMode.Edit | RenderMode.Normal => textarea(EPStyle.`two-line-textarea`, name := f.name, f.initialValue)
      case RenderMode.Presentation             => span(name := f.name)
    }
  }

  val largeTextareaField: FieldDualRenderer = (f, mode) => {
    mode match {
      case RenderMode.Edit | RenderMode.Normal => textarea(EPStyle.`eight-line-textarea`, name := f.name, f.initialValue)
      case RenderMode.Presentation             => span(name := f.name)
    }
  }

  val presEditableNum: FieldDualRenderer = (f, mode) => {
    f match {
      case n: NumberField[_] => renderNumberField(n)
      case _                 => span(EPStyle.max3charinline, input(`type` := "number", name := f.name, value := f.initialValue))
    }
  }

  def textWithPlaceholder(placeholder: PlaceholderLabel): FieldSingleRenderer = (f) =>
    div(EPStyle.inlineLabelGroup, input(`type` := "text", name := f.name, value := f.initialValue, placeholder.attrs))
}
