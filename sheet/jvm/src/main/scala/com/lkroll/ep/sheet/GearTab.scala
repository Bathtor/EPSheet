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
import com.lkroll.ep.model.{EPTranslation => TranslationKeys, _}
import scalatags.Text.all._

object GearTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val rowItemName: GroupRenderer.FieldSingleRenderer = (f) => span(name := f.name, sty.tfrowName);
  //  val weightUnit: SheetElement = {
  //    val f = char.weightUnit;
  //    span(input(`type` := "hidden", name := f.name, value := f.initialValue), span(name := f.name))
  //  }
  //val weightUnit: SheetElement = span("kg");
  // Note: Roll20 doesn't allow datalists :(
  //  def skillDataList(f: EnumField): Tag = {
  //    val listName = f.qualifiedAttr + "list";
  //    datalist(name := listName,
  //      f.options.map(o => option(value := o)).toSeq)
  //  }
  val skillSearchBox: GroupRenderer.FieldSingleRenderer = (f) =>
    div(sty.inlineLabelGroup, input(`type` := "text", name := f.name, t.weaponSkillSearch.placeholder));
  //f match {
  //    case o: EnumField => {
  //      val listName = o.qualifiedAttr + "list";
  //      input(`type` := "search", name := o.name, list := listName)
  //    }
  //    case _ => {

  //    }
  //  }
  val dtRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
    sup(span(EPStyle.`cat-tag-field`, name := f.name, SheetI18NAttrs.datai18nDynamic))
  };

  //  val daRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
  //    span(sty.`area-field`, name := f.name, SheetI18NAttrs.datai18nDynamic)
  //  }
  //
  //  val ubaRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
  //    span(sty.`uniform-radius-field`, raw("("), span(name := f.name), raw("m)"))
  //  }

  val specRenderer: GroupRenderer.FieldDualRenderer = (f, mode) =>
    mode match {
      case RenderMode.Presentation =>
        sup(input(`type` := "hidden", name := f.name), span(EPStyle.`spec-tag-field`, "+"))
      case RenderMode.Edit =>
        div(sty.inlineLabelGroup,
            label(t.weaponSpecialisation),
            input(`type` := "checkbox", name := f.name, value := "10"))
      case RenderMode.Normal => ???
    };

  case class AreaGroup(area: FieldLike[_], areaShort: FieldLike[_], radius: FieldLike[_]) extends FieldGroup {
    val fieldRenderer = CoreTabRenderer.fieldRenderers;

    override def render(mode: RenderMode = RenderMode.Normal): Tag = {
      span(
        span(sty.`area-field`, name := areaShort.name, SheetI18NAttrs.datai18nDynamic),
        input(sty.`area-field`, tpe := "hidden", name := area.name),
        span(sty.`uniform-radius-field`, raw("("), span(name := radius.name), raw("m)"))
      )
    }
    override def renderer: GroupRenderer = null;
    override def members: Seq[SheetElement] = null;
  }

  def area(area: FieldLike[_], areaShort: FieldLike[_], radius: FieldLike[_]) = AreaGroup(area, areaShort, radius);

  case class RangeGroup(rangeLabel: LabelsI18N, rangeStart: FieldLike[_], rangeEnd: FieldLike[_], unit: FieldLike[_])
      extends FieldGroup {

    val fieldRenderer = CoreTabRenderer.fieldRenderers;

    override def render(mode: RenderMode = RenderMode.Normal): Tag = {
      div(
        EPStyle.inlineLabelGroup,
        span(EPStyle.inlineLabel, rangeLabel),
        fieldRenderer(rangeStart, mode),
        span(raw(" - ")),
        fieldRenderer(rangeEnd, mode),
        span(name := unit.name)
      )
    }
    override def renderer: GroupRenderer = null;
    override def members: Seq[SheetElement] = null;
  }

  def range(rangeLabel: LabelsI18N, rangeStart: FieldLike[_], rangeEnd: FieldLike[_], unit: FieldLike[_]): RangeGroup =
    RangeGroup(rangeLabel, rangeStart, rangeEnd, unit);

  def checklabel(label: LabelsI18N, innerSep: Option[String] = None): GroupRenderer.FieldSingleRenderer =
    (f) =>
      f match {
        case ff: FlagField => {
          import CoreTabRenderer.obool2Checked;
          innerSep match {
            case Some(sep) =>
              span(input(`type` := "checkbox", style := "display: none", name := ff.name, ff.defaultValue),
                   span(sty.`checklabel`, raw(sep)),
                   span(sty.`checklabel`, label))
            case None =>
              span(input(`type` := "checkbox", style := "display: none", name := ff.name, ff.defaultValue),
                   span(sty.`checklabel`, label))
          }
        }
      }
  def checklabellike(label: LabelsI18N, ff: FlagField, innerSep: Option[String] = None): FieldWithRenderer =
    ff.like(checklabel(label, innerSep));

  val meleeDamageRollExcellent60: Button = roll(
    char.meleeWeapons,
    "damage_roll_excellent60",
    char.chatOutputEPRolls,
    EPDamageTemplate(char.characterName,
                     char.meleeWeapons.weapon,
                     char.meleeWeapons.damageRollExcellent60,
                     char.meleeWeapons.damageType,
                     char.meleeWeapons.armourPenetration)
  );
  val meleeDamageRollExcellent30: Button = roll(
    char.meleeWeapons,
    "damage_roll_excellent30",
    char.chatOutputEPRolls,
    EPDamageTemplate(char.characterName,
                     char.meleeWeapons.weapon,
                     char.meleeWeapons.damageRollExcellent30,
                     char.meleeWeapons.damageType,
                     char.meleeWeapons.armourPenetration)
  );
  val meleeDamageRoll: Button = roll(
    char.meleeWeapons,
    "damage_roll",
    char.chatOutputEPRolls,
    EPDamageTemplate(char.characterName,
                     char.meleeWeapons.weapon,
                     char.meleeWeapons.damageRoll,
                     char.meleeWeapons.damageType,
                     char.meleeWeapons.armourPenetration)
  );
  val meleeDamageRollQuery: RollElement = roll(
    char.meleeWeapons,
    "damage_query_roll",
    char.chatOutputEPRolls,
    EPDamageTemplate(char.characterName,
                     char.meleeWeapons.weapon,
                     char.meleeWeapons.damageRollQuery,
                     char.meleeWeapons.damageType,
                     char.meleeWeapons.armourPenetration),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.dmg),
      char.meleeWeapons.numDamageDice,
      span("d10"),
      input(`type` := "checkbox", sty.`divide-not-one`, name := char.meleeWeapons.showDivisor.name),
      span(sty.divisor, name := char.meleeWeapons.damageDivisor.name),
      span("+"),
      char.meleeWeapons.damageBonus,
      span(" / "),
      char.meleeWeapons.armourPenetration.like(f => span(span(name := f.name), span(t.ap)))
    )
  );
  val meleeAttackRoll: RollElement = roll(
    char.meleeWeapons,
    "weapon_roll",
    char.chatOutputEPRolls,
    EPDefaultTemplate(
      char.characterName,
      char.meleeWeapons.skillName,
      char.meleeWeapons.weapon,
      char.epRoll,
      char.meleeWeapons.attackTarget,
      CommandButton("damage", meleeDamageRoll),
      CommandButton("damage+5", meleeDamageRollExcellent30),
      CommandButton("damage+10", meleeDamageRollExcellent60)
    ),
    char.meleeWeapons.weapon.like(rowItemName)
  );

  val meleeWeapons: SheetElement = block(
    t.meleeWeapons,
    //skillDataList(char.meleeWeapons.skillSearch),
    char.meleeWeapons {
      TightRepRow(
        presOnly(
          flowpar(
            meleeAttackRoll,
            char.meleeWeapons.damageTypeShort.like(dtRenderer),
            span(" ("),
            char.meleeWeapons.skillName,
            char.meleeWeapons.specialisation.like(specRenderer),
            span(raw(" + ")),
            char.meleeWeapons.miscMod,
            span(")"),
            span(raw(" ~ ")),
            meleeDamageRollQuery,
            meleeDamageRoll.hidden,
            meleeDamageRollExcellent30.hidden,
            meleeDamageRollExcellent60.hidden,
            char.meleeWeapons.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill
          ),
          descrpar(char.meleeWeapons.showDescription, char.meleeWeapons.description)
        ),
        editOnly(
          tightcol(
            tightfrow(
              char.meleeWeapons.weapon.like(CoreTabRenderer.textWithPlaceholder(t.weaponName.placeholder)),
              span(EPStyle.inlineLabel, t.weaponSkill),
              char.meleeWeapons.skillSearch.like(skillSearchBox),
              char.meleeWeapons.skillName,
              char.meleeWeapons.skillTotal.hidden,
              span(raw(" + ")),
              char.meleeWeapons.miscMod,
              span(raw(" + ")),
              char.meleeWeapons.specialisation.like(specRenderer),
              flexFill
            ),
            tightfrow(
              span(EPStyle.lineLabel, t.dmg),
              char.meleeWeapons.damageType,
              char.meleeWeapons.damageTypeShort.hidden,
              char.meleeWeapons.numDamageDice,
              span("d10÷"),
              char.meleeWeapons.damageDivisor,
              span("+"),
              char.meleeWeapons.damageBonus,
              span(" / "),
              (t.ap -> char.meleeWeapons.armourPenetration),
              flexFill
            ),
            tightfrow(sty.halfRemRowSeparator,
                      span(sty.lineLabel, t.weaponDescription),
                      MarkupElement(char.meleeWeapons.description.like(CoreTabRenderer.textareaFieldGrow)))
          )
        )
      )
    }
  );

  val rangedDamageConcExtraBF: Button = roll(
    char,
    "ranged_damage_conc_extra_bf",
    char.chatOutputOther,
    EPDamageTemplate(char.characterName, t.concentrateFire, char.rangedConcBFXDmg)
  );
  val rangedDamageConcExtraFA: Button = roll(
    char,
    "ranged_damage_conc_extra_fa",
    char.chatOutputOther,
    EPDamageTemplate(char.characterName, t.concentrateFire, char.rangedConcFAXDmg)
  );
  val rangedDamageRollExcellent60: Button = roll(
    char.rangedWeapons,
    "damage_roll_excellent60",
    char.chatOutputEPRolls,
    EPDamageTemplate(
      char.characterName,
      char.rangedWeapons.weapon,
      char.rangedWeapons.damageRollExcellent60,
      char.rangedWeapons.damageType,
      char.rangedWeapons.armourPenetration,
      CommandButton("+1d10 extra damage", rangedDamageConcExtraBF),
      CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)
    )
  );
  val rangedDamageRollExcellent30: Button = roll(
    char.rangedWeapons,
    "damage_roll_excellent30",
    char.chatOutputEPRolls,
    EPDamageTemplate(
      char.characterName,
      char.rangedWeapons.weapon,
      char.rangedWeapons.damageRollExcellent30,
      char.rangedWeapons.damageType,
      char.rangedWeapons.armourPenetration,
      CommandButton("+1d10 extra damage", rangedDamageConcExtraBF),
      CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)
    )
  );
  val rangedDamageRoll: Button = roll(
    char.rangedWeapons,
    "damage_roll",
    char.chatOutputEPRolls,
    EPDamageTemplate(
      char.characterName,
      char.rangedWeapons.weapon,
      char.rangedWeapons.damageRoll,
      char.rangedWeapons.damageType,
      char.rangedWeapons.armourPenetration,
      CommandButton("+1d10 extra damage", rangedDamageConcExtraBF),
      CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)
    )
  );
  val rangedDamageRollQuery: RollElement = roll(
    char.rangedWeapons,
    "damage_roll",
    char.chatOutputEPRolls,
    EPDamageTemplate(char.characterName,
                     char.rangedWeapons.weapon,
                     char.rangedWeapons.damageRollQuery,
                     char.rangedWeapons.damageType,
                     char.rangedWeapons.armourPenetration),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.dmg),
      char.rangedWeapons.numDamageDice,
      span("d10"),
      input(`type` := "checkbox", sty.`divide-not-one`, name := char.rangedWeapons.showDivisor.name),
      span(sty.divisor, name := char.rangedWeapons.damageDivisor.name),
      span("+"),
      char.rangedWeapons.damageBonus,
      span(" / "),
      char.rangedWeapons.armourPenetration.like(f => span(span(name := f.name), span(t.ap)))
    )
  );
  val rangedAttackRoll: RollElement = roll(
    char.rangedWeapons,
    "weapon_roll",
    char.chatOutputEPRolls,
    EPDefaultTemplate(
      char.characterName,
      char.rangedWeapons.skillName,
      char.rangedWeapons.weapon,
      char.epRoll,
      char.rangedWeapons.attackTarget,
      CommandButton("damage", rangedDamageRoll),
      CommandButton("damage+5", rangedDamageRollExcellent30),
      CommandButton("damage+10", rangedDamageRollExcellent60)
    ),
    char.rangedWeapons.weapon.like(rowItemName)
  );

  val rangedWeapons: SheetElement = block(
    t.rangedWeapons,
    rangedDamageConcExtraFA.hidden,
    rangedDamageConcExtraBF.hidden,
    char.rangedWeapons {
      TightRepRow(
        presOnly(
          flowpar(
            rangedAttackRoll,
            char.rangedWeapons.damageTypeShort.like(dtRenderer),
            span("["),
            char.rangedWeapons.magazineCurrent.like(CoreTabRenderer.presEditableNum),
            span("/"),
            char.rangedWeapons.magazineSize,
            span(" - "),
            char.rangedWeapons.magazineType,
            span("] "),
            span(raw("(")),
            char.rangedWeapons.skillName,
            char.rangedWeapons.specialisation.like(specRenderer),
            span(raw(" + ")),
            char.rangedWeapons.miscMod,
            span(raw(") {")),
            checklabellike(t.singleShot, char.rangedWeapons.singleShot, Some(" ")),
            checklabellike(t.semiAutomatic, char.rangedWeapons.semiAutomatic, Some(" ")),
            checklabellike(t.burstFire, char.rangedWeapons.burstFire, Some(" ")),
            checklabellike(t.fullAutomatic, char.rangedWeapons.fullAutomatic, Some(" ")),
            span(raw(" } ")),
            span(raw(" ~ ")),
            rangedDamageRollQuery,
            span(raw(" ")),
            area(char.rangedWeapons.damageArea,
                 char.rangedWeapons.damageAreaShort,
                 char.rangedWeapons.uniformBlastArea),
            rangedDamageRoll.hidden,
            rangedDamageRollExcellent30.hidden,
            rangedDamageRollExcellent60.hidden,
            span(raw(" ~ ")),
            span(EPStyle.subtleInlineLabel, t.weaponRanges),
            span(EPStyle.inlineLabel, t.shortRange),
            char.rangedWeapons.shortRangeLower,
            span(raw("-")),
            char.rangedWeapons.shortRangeUpper,
            span(raw("m ")),
            span(EPStyle.inlineLabel, t.mediumRange),
            char.rangedWeapons.mediumRangeLower,
            span(raw("-")),
            char.rangedWeapons.mediumRangeUpper,
            span(raw("m ")),
            span(EPStyle.inlineLabel, t.longRange),
            char.rangedWeapons.longRangeLower,
            span(raw("-")),
            char.rangedWeapons.longRangeUpper,
            span(raw("m ")),
            span(EPStyle.inlineLabel, t.extremeRange),
            char.rangedWeapons.extremeRangeLower,
            span(raw("-")),
            char.rangedWeapons.extremeRangeUpper,
            span(raw("m")),
            char.rangedWeapons.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill
          ),
          descrpar(char.rangedWeapons.showDescription, char.rangedWeapons.description)
        ),
        editOnly(
          tightcol(
            tightfrow(
              char.rangedWeapons.weapon.like(CoreTabRenderer.textWithPlaceholder(t.weaponName.placeholder)),
              span(EPStyle.inlineLabel, t.weaponSkill),
              char.rangedWeapons.skillSearch.like(skillSearchBox),
              char.rangedWeapons.skillName,
              char.rangedWeapons.skillTotal.hidden,
              span(raw(" + ")),
              char.rangedWeapons.miscMod,
              span(raw(" + ")),
              char.rangedWeapons.specialisation.like(specRenderer),
              flexFill
            ),
            tightfrow(
              span(EPStyle.lineLabel, t.dmg),
              char.rangedWeapons.damageType,
              char.rangedWeapons.damageTypeShort.hidden,
              char.rangedWeapons.numDamageDice,
              span("d10÷"),
              char.rangedWeapons.damageDivisor,
              span("+"),
              char.rangedWeapons.damageBonus,
              span(" / "),
              (t.ap -> char.rangedWeapons.armourPenetration),
              (t.damageArea -> char.rangedWeapons.damageArea),
              (t.uniformRadius -> char.rangedWeapons.uniformBlastArea),
              flexFill
            ),
            tightfrow(
              span(EPStyle.lineLabel, t.firingModes),
              (t.singleShot -> char.rangedWeapons.singleShot),
              (t.semiAutomatic -> char.rangedWeapons.semiAutomatic),
              (t.burstFire -> char.rangedWeapons.burstFire),
              (t.fullAutomatic -> char.rangedWeapons.fullAutomatic),
              flexFill
            ),
            tightfrow(
              span(EPStyle.lineLabel, t.weaponRanges),
              char.rangedWeapons.rangeUnitSymbol.hidden,
              span(raw(" ")),
              (t.thrown -> char.rangedWeapons.thrown),
              range(t.shortRange,
                    char.rangedWeapons.shortRangeLower,
                    char.rangedWeapons.shortRangeUpperInput,
                    char.rangedWeapons.rangeUnitSymbol),
              range(t.mediumRange,
                    char.rangedWeapons.mediumRangeLower,
                    char.rangedWeapons.mediumRangeUpperInput,
                    char.rangedWeapons.rangeUnitSymbol),
              range(t.longRange,
                    char.rangedWeapons.longRangeLower,
                    char.rangedWeapons.longRangeUpperInput,
                    char.rangedWeapons.rangeUnitSymbol),
              range(t.extremeRange,
                    char.rangedWeapons.extremeRangeLower,
                    char.rangedWeapons.extremeRangeUpperInput,
                    char.rangedWeapons.rangeUnitSymbol),
              flexFill
            ),
            tightfrow(
              span(EPStyle.lineLabel, t.magazine),
              char.rangedWeapons.magazineCurrent,
              span("/"),
              char.rangedWeapons.magazineSize,
              (t.ammoType -> char.rangedWeapons.magazineType),
              flexFill
            ),
            tightfrow(sty.halfRemRowSeparator,
                      span(sty.lineLabel, t.weaponDescription),
                      MarkupElement(char.rangedWeapons.description.like(CoreTabRenderer.textareaFieldGrow)))
          )
        )
      )
    }
  );

  val armourWorn: SheetElement = block(
    t.armourWorn,
    tightfrow(flexFill,
              (t.energy -> char.armourEnergyBonus),
              (t.kinetic -> char.armourKineticBonus),
              (t.layeringPenalty -> char.layeringPenalty),
              flexFill),
    div(sty.smallWrapBoxTitle, sty.halfRemRowSeparator, span(t.armourActiveTotal)),
    char.armourItems {
      TightRepRow(
        presOnly(
          flowpar(
            char.armourItems.active,
            char.armourItems.itemName.like(rowItemName),
            span("(",
                 span(name := char.armourItems.energyBonus.name),
                 "/",
                 span(name := char.armourItems.kineticBonus.name),
                 ")"),
            char.armourItems.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill
          ),
          descrpar(char.armourItems.showDescription, char.armourItems.description)
        ),
        editOnly(
          tightfrow(
            char.armourItems.active,
            char.armourItems.itemName.like(CoreTabRenderer.textWithPlaceholder(t.armourName.placeholder)),
            (t.armourAccessory -> char.armourItems.accessory),
            (t.energy -> char.armourItems.energyBonus),
            (t.kinetic -> char.armourItems.kineticBonus),
            span(sty.inlineLabel, t.equipmentDescription),
            MarkupElement(char.armourItems.description.like(CoreTabRenderer.textareaFieldGrow))
          )
        )
      )
    }
  );

  val currency: SheetElement =
    sblock(t.currency, sty.nop, tightfrow((t.cryptoCredits -> char.cryptoCredits), flexFill, (t.cash -> char.cash)));

  val equipment: SheetElement = block(
    t.equipment,
    char.equipment {
      TightRepRow(
        presOnly(
          flowpar(
            char.equipment.itemName.like(rowItemName),
            span("["),
            char.equipment.amount.like(CoreTabRenderer.presEditableNum),
            span("]"),
            char.equipment.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill
          ),
          descrpar(char.equipment.showDescription, char.equipment.description)
        ),
        editOnly(
          tightfrow(
            char.equipment.itemName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
            char.equipment.amount,
            span(sty.inlineLabel, t.equipmentDescription),
            MarkupElement(char.equipment.description.like(CoreTabRenderer.textareaFieldGrow))
          )
        )
      )
    }
  );

  val software: SheetElement = block(
    t.software,
    char.software {
      TightRepRow(
        presOnly(
          flowpar(
            char.software.itemName.like(rowItemName),
            span(raw("(")),
            char.software.qualityMod,
            span(raw(" – ")),
            char.software.quality,
            span(raw(")")),
            char.software.showDescription.like(CoreTabRenderer.descriptionToggleWrapped),
            flexFill
          ),
          descrpar(char.software.showDescription, char.software.description)
        ),
        editOnly(
          tightfrow(
            char.software.itemName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
            char.software.quality.like(CoreTabRenderer.textWithPlaceholder(t.softwareQuality.placeholder)),
            (t.qualityMod -> char.software.qualityMod),
            span(sty.inlineLabel, t.equipmentDescription),
            MarkupElement(char.software.description.like(CoreTabRenderer.textareaFieldGrow))
          )
        )
      )
    }
  );

  val fireModes: SheetElement = block(
    t.firingModes,
    flowpar(
      span(sty.tfrowName, t.singleShot.fullLabel.text),
      span(raw(" (")),
      span(t.singleShot),
      span(raw(")")),
      CoreTabRenderer.labelDescription(t.singleShotDescription)
    ),
    flowpar(
      span(sty.tfrowName, t.semiAutomatic.fullLabel.text),
      span(raw(" (")),
      span(t.semiAutomatic),
      span(raw(")")),
      CoreTabRenderer.labelDescription(t.semiAutomaticDescription)
    ),
    flowpar(
      span(sty.tfrowName, t.burstFire.fullLabel.text),
      span(raw(" (")),
      span(t.burstFire),
      span(raw(")")),
      CoreTabRenderer.labelDescription(t.burstFireDescription)
    ),
    flowpar(
      span(sty.tfrowName, t.fullAutomatic.fullLabel.text),
      span(raw(" (")),
      span(t.fullAutomatic),
      span(raw(")")),
      CoreTabRenderer.labelDescription(t.fullAutomaticDescription)
    )
  );

  val members: Seq[SheetElement] = Seq(
    frow(
      sty.`flex-start`,
      fcol(Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem), meleeWeapons, rangedWeapons, fireModes),
      fcol(Seq(EPStyle.`flex-grow`, sty.exactly20rem), armourWorn, currency, equipment, software)
    ),
    frow(
      sty.`flex-stretch`,
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
           block(t.gearFreeform, char.gear1.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
           block(t.gearFreeform, char.gear2.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.exactly15rem),
           block(t.gearFreeform, char.gear3.like(CoreTabRenderer.largeTextareaField)))
    )
  );

  override def renderer = CoreTabRenderer;
}
