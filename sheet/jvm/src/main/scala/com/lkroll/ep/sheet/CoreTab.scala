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
import com.lkroll.ep.model._
import scalatags.Text.all._

object CoreTab extends FieldGroup {
  import SheetImplicits._
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
    span(EPStyle.`trait-tag-field`, name := f.name, SheetI18NAttrs.datai18nDynamic)
  }

  val effects = block(
    t.effects,
    char.effects {
      TightRepRow(
        presOnly(
          flowrow(
            char.effects.active,
            char.effects.effectName.like(GearTab.rowItemName),
            span("["), char.effects.duration, span("] "),
            char.effects.gameEffect,
            char.effects.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill),
          descrpar(
            char.effects.showDescription,
            char.effects.description)),
        editOnly(tightfrow(
          char.effects.active,
          char.effects.effectName.like(CoreTabRenderer.textWithPlaceholder(t.effectName.placeholder)),
          span(EPStyle.inlineLabel, t.effectDuration),
          char.effects.duration.like(CoreTabRenderer.textWithPlaceholder(t.effectDurationExample.placeholder)),
          span(EPStyle.inlineLabel, t.effectOnGame),
          char.effects.gameEffect.like(CoreTabRenderer.textWithPlaceholder(t.effectOnGameExample.placeholder)),
          span(EPStyle.inlineLabel, t.effectDescription),
          MarkupElement(char.effects.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val characterTraits = block(
    t.characterTraits,
    char.characterTraits {
      TightRepRow(
        presOnly(
          flowpar(
            char.characterTraits.traitTypeShort.like(traitTypeRenderer),
            char.characterTraits.traitName.like(GearTab.rowItemName),
            char.characterTraits.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill),
          descrpar(
            char.characterTraits.showDescription,
            char.characterTraits.description)),
        editOnly(tightfrow(
          char.characterTraits.traitType,
          char.characterTraits.traitName.like(CoreTabRenderer.textWithPlaceholder(t.traitName.placeholder)),
          span(EPStyle.inlineLabel, t.traitDescription),
          MarkupElement(char.characterTraits.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val derangements = block(
    t.derangements,
    char.derangements {
      TightRepRow(
        presOnly(
          flowpar(
            char.derangements.conditionName.like(GearTab.rowItemName),
            span("["), char.derangements.duration.like(CoreTabRenderer.presEditableNum), span(t.hours), span("] "),
            span("("), char.derangements.severity, span(")"),
            char.derangements.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill),
          descrpar(
            char.derangements.showDescription,
            char.derangements.description)),
        editOnly(tightfrow(
          char.derangements.conditionName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          (t.derangementDuration -> char.derangements.duration), span(t.hours),
          (t.derangementSeverity -> char.derangements.severity),
          span(EPStyle.inlineLabel, t.derangementDescription),
          MarkupElement(char.derangements.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val disorders = block(
    t.disorders,
    char.disorders {
      TightRepRow(
        presOnly(
          flowpar(
            char.disorders.conditionName.like(GearTab.rowItemName),
            span(raw(" ~ ")), span(EPStyle.subtleInlineLabel, t.disorderRemainingTreatment),
            char.disorders.treatmentRemaining.like(CoreTabRenderer.presEditableNum), span(t.hours),
            char.disorders.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill),
          descrpar(
            char.disorders.showDescription,
            char.disorders.description)),
        editOnly(tightfrow(
          char.disorders.conditionName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          (t.disorderRemainingTreatment -> char.disorders.treatmentRemaining), span(t.hours),
          span(EPStyle.inlineLabel, t.derangementDescription),
          MarkupElement(char.disorders.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val specialRolls = block(t.specialRolls, arrowList(
    rwd(roll(char, "simple-success-roll", char.chatOutputEPRolls,
      EPDefaultTemplate(char.characterName, t.successRoll, char.epRoll, char.customTarget),
      span(sty.rollLabel, t.successRoll))),
    rwd(
      roll(char, "moxiex10-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.moxx10Roll.fullLabel, char.epRoll, char.moxiex10Target),
        span(sty.rollLabel, t.moxx10Roll)),
      t.avoidLemon, t.avoidVirusExposure),
    rwd(
      roll(char, "fray-halved-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.frayHalvedRoll.fullLabel, char.epRoll, char.frayHalvedTarget),
        span(sty.rollLabel, t.frayHalvedRoll)),
      t.rangedDefence),
    rwd(
      roll(char, "willx2-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.wilx2Roll.fullLabel, char.epRoll, char.willx2Target),
        span(sty.rollLabel, t.wilx2Roll)),
      t.psiDefense),
    rwd(
      roll(char, "willx3-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.wilx3Roll.fullLabel, char.epRoll, char.willx3Target),
        span(sty.rollLabel, t.wilx3Roll)),
      t.continuityTest, t.stressTest, t.resistTraumaDisorientation, t.healTrauma),
    rwd(
      roll(char, "somx3-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.somx3Roll.fullLabel, char.epRoll, char.somx3Target),
        span(sty.rollLabel, t.somx3Roll)),
      t.integrationTest, t.resistWoundKnockdown, t.bruteStrength),
    rwd(
      roll(char, "intx3-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.intx3Roll.fullLabel, char.epRoll, char.intx3Target),
        span(sty.rollLabel, t.intx3Roll)),
      t.alienationTest, t.havingAnIdea),
    rwd(
      roll(char, "cogx3-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.cogx3Roll.fullLabel, char.epRoll, char.cogx3Target),
        span(sty.rollLabel, t.cogx3Roll)),
      t.memoriseRecall, t.havingAnIdea),
    rwd(
      roll(char, "refx3-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.refx3Roll.fullLabel, char.epRoll, char.refx3Target),
        span(sty.rollLabel, t.refx3Roll)),
      t.holdBreath),
    rwd(
      roll(char, "dur-energy-armour-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.durEnergyRoll.fullLabel, char.epRoll, char.durEnergyArmour),
        span(sty.rollLabel, t.durEnergyRoll)),
      t.resistShock),
    rwd(
      roll(char, "ref-coox2-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.refCoox2Roll.fullLabel, char.epRoll, char.refCoox2Target),
        span(sty.rollLabel, t.refCoox2Roll)),
      t.catchingObjects),
    rwd(
      roll(char, "coo-som-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.cooSomRoll.fullLabel, char.epRoll, char.cooSomTarget),
        span(sty.rollLabel, t.cooSomRoll)),
      t.escapeArtist),
    rwd(
      roll(char, "wil-cog-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.wilCogRoll.fullLabel, char.epRoll, char.wilCogTarget),
        span(sty.rollLabel, t.wilCogRoll)),
      t.resistBrainSeizure),
    rwd(
      roll(char, "ref-coo-wil-roll", char.chatOutputEPRolls,
        EPDefaultTemplate (char.characterName, t.refCooWilRoll.fullLabel, char.epRoll, char.refCooWilTarget),
        span(sty.rollLabel, t.refCooWilRoll)),
      t.jumpOnGrenade),
    span(EPStyle.smallDescription, t.resistAsphyxiation.attrs),
    span(EPStyle.smallDescription, t.resistBackupComplications.attrs)));

  val stats = fblock(t.stats, EPStyle.max2p5rem,
    (t.tt -> char.traumaThreshold),
    roll(char, "lucidity-roll", char.chatOutputEPRolls, EPDefaultTemplate(char.characterName, t.luc.fullLabel, char.epRoll, char.lucidityTarget), (t.luc -> char.lucidity)),
    editOnly(t.lucExtra -> char.lucidityExtra),
    (t.ir -> char.insanityRating),
    (t.wt -> char.woundThreshold),
    roll(char, "dur-roll", char.chatOutputEPRolls, EPDefaultTemplate(char.characterName, t.dur.fullLabel, char.epRoll, char.durTarget), (t.dur -> char.durability)),
    (t.dr -> char.deathRating),
    roll(char, "initiative-roll", char.chatOutputOther, EPIniTemplate(char.characterName, char.iniRoll), (t.init -> char.initiative)),
    (t.spd -> char.speed),
    editOnly(t.spdExtra -> char.speedExtra),
    (t.moa -> char.mentalOnlyActions),
    editOnly(t.moaExtra -> char.mentalOnlyActionsExtra),
    (t.db -> char.damageBonus));

  val topRow = eprow(frow(
    sty.`flex-centre`,
    flexFillNarrow,
    sblock(
      t.mox,
      roll(char, "moxie-roll", char.chatOutputEPRolls, EPDefaultTemplate(char.characterName, t.mox.fullLabel, char.epRoll, char.moxieTarget)),
      sty.max15rem,
      char.moxie, span(" / "), dualMode(char.moxieMax)),
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

  val leftCol = fcol(
    Seq(EPStyle.`flex-grow`, EPStyle.exactly15rem, EPStyle.marginr1rem),
    characterInfo,
    effects,
    characterTraits,
    derangements,
    disorders);

  val rightCol = fcol(
    Seq(EPStyle.exactly23rem),
    block(t.aptitudes, aptitudes),
    stats,
    specialRolls);

  val members: Seq[SheetElement] = Seq(
    topRow,
    frow(
      sty.`flex-start`,
      leftCol,
      rightCol));

  override def renderer = CoreTabRenderer;

}
