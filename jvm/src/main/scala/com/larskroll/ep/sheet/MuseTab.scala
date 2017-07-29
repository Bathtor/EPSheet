package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._
import SheetImplicits._

object MuseTab extends FieldGroup {
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val aptitudes = MuseAptitudes(Seq(
    AptitudeRow(t.aptTotal, Seq(char.museCog, char.museCoo, char.museInt, char.museRef, char.museSav, char.museSom, char.museWil))));

  val leftCol = fcol(Seq(EPStyle.`flex-grow`, EPStyle.exactly15rem, EPStyle.marginr1rem),
    fblock(t.museInfo, EPStyle.min5rem,
      (t.museName -> dualMode(char.museName)),
      (t.tt -> char.museTraumaThreshold),
      (t.luc -> char.museLucidity),
      (t.ir -> char.museInsanityRating),
      (t.museNotes -> dualMode(char.museNotes.like(CoreTabRenderer.textareaField)))));
  val rightCol = fcol(Seq(EPStyle.exactly23rem),
    block(t.aptitudes, aptitudes),
    block(t.museSkills,
      char.museSkills {
        TightRepRow(
          presOnly(tightfrow(
            char.museSkills.total like { total => span(sty.skillTotal, name := total.name) },
            roll(char.museSkills, "muse_skill_roll", Chat.Default, EPDefaultTemplate(char.museName, char.museSkills.skillName, char.museSkills.field, char.epRoll, char.museSkills.rollTarget), char.museSkills.skillName like { sname => span(sty.skillName, name := sname.name) }),
            char.museSkills.field like { sfield => span(sty.skillField, name := sfield.name) },
            char.museSkills.linkedAptitude like { apt => span(sty.skillApt, "(", span(name := apt.name), ")") },
            flexFill)),
          editOnly(tightfrow(sty.halfRemRowSeparator,
            char.museSkills.skillName.like(CoreTabRenderer.textWithPlaceholder(t.skillName.placeholder)),
            char.museSkills.field.like(CoreTabRenderer.textWithPlaceholder(t.skillField.placeholder)),
            char.museSkills.linkedAptitude,
            (t.skillRanks -> char.museSkills.ranks),
            (t.skillTotal -> char.museSkills.total),
            flexFill)))
      }),
    block(t.skillCommands,
      char.generateMuseSkillsLabel like { rid => input(`type` := "hidden", name := rid.name, value := rid.initialValue) },
      tightfrow(
        pseudoButton(char.generateMuseSkills, char.generateMuseSkillsLabel),
        flexFill)));

  val members: Seq[SheetElement] = Seq(
    frow(sty.`flex-start`,
      leftCol,
      rightCol));

  override def renderer = CoreTabRenderer;
}
