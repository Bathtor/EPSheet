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
    AptitudeRow(t.aptTemp, Seq(char.cogTemp, char.cooTemp, char.intTemp, char.refTemp, char.savTemp, char.somTemp, char.wilTemp)),
    AptitudeRow(t.aptTotal, Seq(char.cogTotal, char.cooTotal, char.intTotal, char.refTotal, char.savTotal, char.somTotal, char.wilTotal))));

  val characterInfo = fblock(t.characterInfo, EPStyle.min5rem,
    (t.background -> dualMode(char.background)),
    (t.faction -> dualMode(char.faction)),
    (t.genderId -> dualMode(char.genderId)),
    (t.actualAge -> dualMode(char.actualAge)),
    (t.motivations -> dualMode(char.motivations.like(CoreTabRenderer.textareaField))));

  val traitTypeRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
    span(EPStyle.`trait-tag-field`, name := f.name, SheetI18N.datai18nDynamic)
  }

  val characterTraits = block(t.characterTraits,
    char.characterTraits {
      TightRepRow(
        presOnly(flowpar(
          char.characterTraits.traitTypeShort.like(traitTypeRenderer),
          char.characterTraits.traitName.like(GearTab.rowItemName),
          char.characterTraits.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(
          char.characterTraits.traitType,
          char.characterTraits.traitName.like(CoreTabRenderer.textWithPlaceholder(t.traitName.placeholder)),
          span(EPStyle.inlineLabel, t.traitDescription),
          MarkupElement(char.characterTraits.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val derangements = block(t.derangements,
    char.derangements {
      TightRepRow(
        presOnly(flowpar(
          char.derangements.conditionName.like(GearTab.rowItemName),
          span("["), char.derangements.duration.like(CoreTabRenderer.presEditableNum), span(t.hours), span("] "),
          span("("), char.derangements.severity, span(") "),
          char.derangements.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(
          char.derangements.conditionName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          (t.derangementDuration -> char.derangements.duration), span(t.hours),
          (t.derangementSeverity -> char.derangements.severity),
          span(EPStyle.inlineLabel, t.derangementDescription),
          MarkupElement(char.derangements.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val disorders = block(t.disorders,
    char.disorders {
      TightRepRow(
        presOnly(flowpar(
          char.disorders.conditionName.like(GearTab.rowItemName),
          span(raw(" ~ ")), span(EPStyle.subtleInlineLabel, t.disorderRemainingTreatment),
          char.disorders.treatmentRemaining.like(CoreTabRenderer.presEditableNum), span(t.hours),
          char.disorders.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(
          char.disorders.conditionName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          (t.disorderRemainingTreatment -> char.disorders.treatmentRemaining), span(t.hours),
          span(EPStyle.inlineLabel, t.derangementDescription),
          MarkupElement(char.disorders.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val specialRolls = block(t.specialRolls, arrowList(
    rwd(roll(char, "simple-success-roll", char.chatOutput,
      EPDefaultTemplate(char.characterName, t.successRoll, char.epRoll, char.customTarget),
      span(sty.rollLabel, t.successRoll))),
    rwd(roll(char, "fray-halved-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.frayHalvedRoll.fullLabel, char.epRoll, char.frayHalvedTarget),
      span(sty.rollLabel, t.frayHalvedRoll)),
      t.rangedDefence),
    rwd(roll(char, "willx2-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.wilx2Roll.fullLabel, char.epRoll, char.willx2Target),
      span(sty.rollLabel, t.wilx2Roll)),
      t.psiDefense),
    rwd(roll(char, "willx3-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.wilx3Roll.fullLabel, char.epRoll, char.willx3Target),
      span(sty.rollLabel, t.wilx3Roll)),
      t.continuityTest, t.stressTest, t.resistTraumaDisorientation, t.healTrauma),
    rwd(roll(char, "somx3-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.somx3Roll.fullLabel, char.epRoll, char.somx3Target),
      span(sty.rollLabel, t.somx3Roll)),
      t.integrationTest, t.resistWoundKnockdown, t.bruteStrength),
    rwd(roll(char, "intx3-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.intx3Roll.fullLabel, char.epRoll, char.intx3Target),
      span(sty.rollLabel, t.intx3Roll)),
      t.alienationTest, t.havingAnIdea),
    rwd(roll(char, "cogx3-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.cogx3Roll.fullLabel, char.epRoll, char.cogx3Target),
      span(sty.rollLabel, t.cogx3Roll)),
      t.memoriseRecall, t.havingAnIdea),
    rwd(roll(char, "dur-energy-armour-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.durEnergyRoll.fullLabel, char.epRoll, char.durEnergyArmour),
      span(sty.rollLabel, t.durEnergyRoll)),
      t.resistShock),
    rwd(roll(char, "ref-coox2-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.refCoox2Roll.fullLabel, char.epRoll, char.refCoox2Target),
      span(sty.rollLabel, t.refCoox2Roll)),
      t.catchingObjects),
    rwd(roll(char, "coo-som-roll", char.chatOutput,
      EPDefaultTemplate (char.characterName, t.cooSomRoll.fullLabel, char.epRoll, char.cooSomTarget),
      span(sty.rollLabel, t.cooSomRoll)),
      t.escapeArtist)));

  val stats = fblock(t.stats, EPStyle.max2p5rem,
    (t.tt -> char.traumaThreshold),
    roll(char, "lucidity-roll", char.chatOutput, EPDefaultTemplate(char.characterName, t.luc.fullLabel, char.epRoll, char.lucidityTarget), (t.luc -> char.lucidity)),
    (t.ir -> char.insanityRating),
    (t.wt -> char.woundThreshold),
    (t.dur -> char.durability),
    (t.dr -> char.deathRating),
    roll(char, "initiative-roll", char.chatOutput, EPIniTemplate(char.characterName, char.iniRoll), (t.init -> char.initiative)),
    (t.spd -> dualMode(char.speed)),
    (t.moa -> dualMode(char.mentalOnlyActions)),
    (t.db -> char.damageBonus));

  val topRow = eprow(frow(sty.`flex-centre`,
    flexFillNarrow,
    sblock(t.mox,
      roll(char, "moxie-roll", char.chatOutput, EPDefaultTemplate(char.characterName, t.mox.fullLabel, char.epRoll, char.moxieTarget)),
      sty.max15rem,
      char.currentMoxie, span(" / "), dualMode(char.moxie)),
    flexFillNarrow,
    sblock(t.mentalHealth, sty.max15rem,
      (t.stress -> char.stress),
      (t.trauma -> char.trauma)),
    flexFillNarrow,
    sblock(t.physicalHealth, sty.max15rem,
      (t.damage -> char.damage),
      (t.wounds -> char.wounds)),
    flexFillNarrow,
    sblock(t.armour, sty.max15rem,
      (t.energy -> char.armourEnergyTotal),
      (t.kinetic -> char.armourKineticTotal)),
    flexFillNarrow,
    sblock(t.rezPoints, sty.max5rem, char.rezPoints),
    flexFillNarrow));

  val leftCol = fcol(Seq(EPStyle.`flex-grow`, EPStyle.exactly15rem, EPStyle.marginr1rem),
    characterInfo,
    characterTraits,
    derangements,
    disorders);

  val rightCol = fcol(Seq(EPStyle.exactly23rem),
    block(t.aptitudes, aptitudes),
    stats,
    specialRolls);

  val members: Seq[SheetElement] = Seq(
    topRow,
    frow(sty.`flex-start`,
      leftCol,
      rightCol));

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
        val integerDigits = floor(log10(abs(largestBound))).toInt + 1; // +1 for first digit
        var count = 0;
        var v = stepD;
        while (!v.isWhole()) {
          v *= 10.0;
          count += 1;
        }
        val decimalDigits = count + 1; // for the decimal point
        val digits = integerDigits + decimalDigits + (if (nev.signum(minV) == -1) { 1 } else { 0 }); // +1 for leading minus if necessary
        span(EPStyle.inlineNumber, maxWidth := digits.em,
          input(`type` := "number", name := f.name, value := f.initialValue, min := minV.toString(),
            max := maxV.toString(), step := stepV.toString()))
      }
      case None => {
        span(EPStyle.max3charinline, input(`type` := "number", name := f.name, value := f.initialValue))
      }
    }

  }

  def renderNumberFieldNoWidth[N](f: NumberField[N]): Tag = {
    f.valid match {
      case Some(NumberValidity(minV, maxV, stepV)) => {
        input(`type` := "number", name := f.name, value := f.initialValue,
          min := minV.toString(), max := maxV.toString(), step := stepV.toString())
      }
      case None => {
        input(`type` := "number", name := f.name, value := f.initialValue)
      }
    }
  }

  private def enumRender(ef: EnumField): Tag = {

    val selector: String => Option[Modifier] = ef.defaultValue match {
      case Some(dv) =>
        println(s"Default value for $ef is $dv");
        (o: String) => if (dv == o) {
          //println(s"Match of $dv with $o!");
          Some(selected)
        } else {
          //println(s"No match between $dv and $o");
          None
        }
        case None => println(s"No default value for $ef"); (o: String) => None
    }
    ef.enum match {
      case Some(e) => EPTranslation.allFullOptions.get(e) match {
        case Some(l) => {
          val options = ef.options.map((o) => option(value := o, l.apply(o).attr, selector(o))).toSeq
          select(name := ef.name, options)
        }
        case None => println(s"Translation missing for enumeration: ${e.toString()}"); select(name := ef.name, ef.options.map(o => option(value := o, o, selector(o))).toSeq)
      }
      case None => select(name := ef.name, ef.options.map(o => option(value := o, o, selector(o))).toSeq)
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
      case ef: EnumField     => enumRender(ef)
      case _                 => input(`type` := "text", name := f.name, value := f.initialValue)
    }
    case (f: Field[_], Presentation) if f.editable() => f match {
      case ff: FlagField => input(`type` := "checkbox", name := ff.name, ff.defaultValue)
      case _             => span(EPStyle.labelledValue, name := f.name)
    }

    case (f: Field[_], Edit) if f.editable() => f match {
      case n: NumberField[_] => renderNumberField(n)
      case ff: FlagField     => input(`type` := "checkbox", name := ff.name, ff.defaultValue)
      case ef: EnumField     => enumRender(ef)
      case _                 => input(`type` := "text", name := f.name, value := f.initialValue)
    }
    case (f: Field[_], _) if !(f.editable()) =>
      span(input(`type` := "hidden", name := f.name, value := f.initialValue), span(EPStyle.labelledValue, name := f.name))
  };

  val textareaField: FieldDualRenderer = (f, mode) => {
    mode match {
      case RenderMode.Edit | RenderMode.Normal => textarea(EPStyle.`two-line-textarea`, name := f.name, f.initialValue)
      case RenderMode.Presentation             => span(EPStyle.labelledValue, name := f.name)
    }
  }

  val textareaFieldGrow: FieldDualRenderer = (f, mode) => {
    mode match {
      case RenderMode.Edit | RenderMode.Normal => div(EPStyle.`flex-grow`, EPStyle.inlineContentGroup, textarea(EPStyle.`two-line-textarea`, name := f.name, f.initialValue))
      case RenderMode.Presentation             => span(EPStyle.labelledValue, name := f.name)
    }
  }

  val largeTextareaField: FieldDualRenderer = (f, mode) => {
    mode match {
      case RenderMode.Edit | RenderMode.Normal => textarea(EPStyle.`eight-line-textarea`, name := f.name, f.initialValue)
      case RenderMode.Presentation             => span(EPStyle.labelledValue, name := f.name)
    }
  }

  val presEditableNum: FieldDualRenderer = (f, mode) => {
    f match {
      case n: NumberField[_] => renderNumberField(n)
      case _                 => span(EPStyle.max3charinline, input(`type` := "number", name := f.name, value := f.initialValue))
    }
  }

  def textWithPlaceholder(placeholder: PlaceholderLabel): FieldSingleRenderer = (f) =>
    input(`type` := "text", name := f.name, value := f.initialValue, placeholder.attrs)

  val description: FieldSingleRenderer = (f) => {
    span(EPStyle.description, raw(" &mdash; "), span(name := f.name))
  }

  val italic: FieldSingleRenderer = (f) => {
    span(fontStyle.italic, EPStyle.labelledValue, name := f.name)
  }

  def labelDescription(label: LabelsI18N) = span(EPStyle.description, raw(" &mdash; "), span(label.attrs));
}
