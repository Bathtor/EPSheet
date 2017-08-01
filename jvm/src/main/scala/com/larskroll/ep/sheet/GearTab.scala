package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
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
  val weightUnit: SheetElement = span("kg");
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
    sup(span(EPStyle.`cat-tag-field`, name := f.name, SheetI18N.datai18nDynamic))
  }
  def checklabel(label: LabelsI18N, innerSep: Option[String] = None): GroupRenderer.FieldSingleRenderer = (f) => f match {
    case ff: FlagField => {
      import CoreTabRenderer.obool2Checked;
      innerSep match {
        case Some(sep) => span(input(`type` := "checkbox", style := "display: none", name := ff.name, ff.defaultValue), span(sty.`checklabel`, raw(sep)), span(sty.`checklabel`, label))
        case None      => span(input(`type` := "checkbox", style := "display: none", name := ff.name, ff.defaultValue), span(sty.`checklabel`, label))
      }
    }
  }
  def checklabellike(label: LabelsI18N, ff: FlagField, innerSep: Option[String] = None): FieldWithRenderer = ff.like(checklabel(label, innerSep));

  val meleeDamageRollExcellent60 = roll(char.meleeWeapons, "damage_roll_excellent60", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRollExcellent60, char.meleeWeapons.damageType, char.meleeWeapons.armourPenetration));
  val meleeDamageRollExcellent30 = roll(char.meleeWeapons, "damage_roll_excellent30", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRollExcellent30, char.meleeWeapons.damageType, char.meleeWeapons.armourPenetration));
  val meleeDamageRoll = roll(char.meleeWeapons, "damage_roll", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRoll, char.meleeWeapons.damageType, char.meleeWeapons.armourPenetration),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.dmg),
      char.meleeWeapons.numDamageDice,
      span("d10+"),
      char.meleeWeapons.damageBonus,
      span(" / "),
      char.meleeWeapons.armourPenetration.like(f => span(span(name := f.name), span(t.ap)))));
  val meleeAttackRoll = roll(char.meleeWeapons, "weapon_roll", Chat.Default,
    EPDefaultTemplate(char.characterName, char.meleeWeapons.skillName, char.meleeWeapons.weapon, char.epRoll, char.meleeWeapons.attackTarget, CommandButton("damage", meleeDamageRoll.roll), CommandButton("damage+5", meleeDamageRollExcellent30), CommandButton("damage+10", meleeDamageRollExcellent60)),
    char.meleeWeapons.weapon.like(rowItemName));

  val meleeWeapons: SheetElement = block(t.meleeWeapons,
    //skillDataList(char.meleeWeapons.skillSearch),
    char.meleeWeapons {
      TightRepRow(
        presOnly(flowpar(
          meleeAttackRoll,
          char.meleeWeapons.damageTypeShort.like(dtRenderer),
          span(" ("),
          char.meleeWeapons.skillName,
          span(")"),
          span(raw(" ~ ")),
          meleeDamageRoll,
          meleeDamageRollExcellent30.hidden,
          meleeDamageRollExcellent60.hidden,
          char.meleeWeapons.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightcol(
          tightfrow(
            char.meleeWeapons.weapon.like(CoreTabRenderer.textWithPlaceholder(t.weaponName.placeholder)),
            span(EPStyle.inlineLabel, t.weaponSkill),
            char.meleeWeapons.skillSearch.like(skillSearchBox),
            char.meleeWeapons.skillName,
            char.meleeWeapons.skillTotal.hidden,
            flexFill),
          tightfrow(
            span(EPStyle.lineLabel, t.dmg),
            char.meleeWeapons.damageType,
            char.meleeWeapons.damageTypeShort.hidden,
            char.meleeWeapons.numDamageDice,
            span("d10+"),
            char.meleeWeapons.damageBonus,
            span(" / "),
            (t.ap -> char.meleeWeapons.armourPenetration),
            flexFill),
          tightrow(sty.halfRemRowSeparator,
            span(sty.lineLabel, t.weaponDescription),
            char.meleeWeapons.description.like(CoreTabRenderer.textareaField)))))
    });

  val rangedDamageConcExtraBF = roll(char, "ranged_damage_conc_extra_bf", Chat.Default, EPDamageTemplate(char.characterName, t.concentrateFire, char.rangedConcBFXDmg));
  val rangedDamageConcExtraFA = roll(char, "ranged_damage_conc_extra_fa", Chat.Default, EPDamageTemplate(char.characterName, t.concentrateFire, char.rangedConcFAXDmg));
  val rangedDamageRollExcellent60 = roll(char.rangedWeapons, "damage_roll_excellent60", Chat.Default, EPDamageTemplate(char.characterName, char.rangedWeapons.weapon, char.rangedWeapons.damageRollExcellent60, char.rangedWeapons.damageType, char.rangedWeapons.armourPenetration, CommandButton("+1d10 extra damage", rangedDamageConcExtraBF), CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)));
  val rangedDamageRollExcellent30 = roll(char.rangedWeapons, "damage_roll_excellent30", Chat.Default, EPDamageTemplate(char.characterName, char.rangedWeapons.weapon, char.rangedWeapons.damageRollExcellent30, char.rangedWeapons.damageType, char.rangedWeapons.armourPenetration, CommandButton("+1d10 extra damage", rangedDamageConcExtraBF), CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)));
  val rangedDamageRoll = roll(char.rangedWeapons, "damage_roll", Chat.Default, EPDamageTemplate(char.characterName, char.rangedWeapons.weapon, char.rangedWeapons.damageRoll, char.rangedWeapons.damageType, char.rangedWeapons.armourPenetration, CommandButton("+1d10 extra damage", rangedDamageConcExtraBF), CommandButton("+3d10 extra damage", rangedDamageConcExtraFA)),
    buttonSeq(
      span(EPStyle.subtleInlineLabel, t.dmg),
      char.rangedWeapons.numDamageDice,
      span("d10+"),
      char.rangedWeapons.damageBonus,
      span(" / "),
      char.rangedWeapons.armourPenetration.like(f => span(span(name := f.name), span(t.ap)))));
  val rangedAttackRoll = roll(char.rangedWeapons, "weapon_roll", Chat.Default,
    EPDefaultTemplate(char.characterName, char.rangedWeapons.skillName, char.rangedWeapons.weapon, char.epRoll, char.rangedWeapons.attackTarget, CommandButton("damage", rangedDamageRoll.roll), CommandButton("damage+5", rangedDamageRollExcellent30), CommandButton("damage+10", rangedDamageRollExcellent60)),
    char.rangedWeapons.weapon.like(rowItemName));

  val rangedWeapons: SheetElement = block(t.rangedWeapons,
    rangedDamageConcExtraFA.hidden,
    rangedDamageConcExtraBF.hidden,
    char.rangedWeapons {
      TightRepRow(
        presOnly(flowpar(
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
          span(raw(") {")),
          checklabellike(t.singleShot, char.rangedWeapons.singleShot, Some(" ")),
          checklabellike(t.semiAutomatic, char.rangedWeapons.semiAutomatic, Some(" ")),
          checklabellike(t.burstFire, char.rangedWeapons.burstFire, Some(" ")),
          checklabellike(t.fullAutomatic, char.rangedWeapons.fullAutomatic, Some(" ")),
          span(raw(" } ")),
          span(raw(" ~ ")),
          rangedDamageRoll,
          rangedDamageRollExcellent30.hidden,
          rangedDamageRollExcellent60.hidden,
          span(raw(" ~ ")), span(EPStyle.subtleInlineLabel, t.weaponRanges),
          span(EPStyle.inlineLabel, t.shortRange),
          char.rangedWeapons.shortRangeLower, span(raw("-")), char.rangedWeapons.shortRangeUpper, span(raw("m ")),
          span(EPStyle.inlineLabel, t.mediumRange),
          char.rangedWeapons.mediumRangeLower, span(raw("-")), char.rangedWeapons.mediumRangeUpper, span(raw("m ")),
          span(EPStyle.inlineLabel, t.longRange),
          char.rangedWeapons.longRangeLower, span(raw("-")), char.rangedWeapons.longRangeUpper, span(raw("m ")),
          span(EPStyle.inlineLabel, t.extremeRange),
          char.rangedWeapons.extremeRangeLower, span(raw("-")), char.rangedWeapons.extremeRangeUpper, span(raw("m")),
          char.rangedWeapons.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightcol(
          tightfrow(
            char.rangedWeapons.weapon.like(CoreTabRenderer.textWithPlaceholder(t.weaponName.placeholder)),
            span(EPStyle.inlineLabel, t.weaponSkill),
            char.rangedWeapons.skillSearch.like(skillSearchBox),
            char.rangedWeapons.skillName,
            char.rangedWeapons.skillTotal.hidden,
            span(raw(" + ")), char.rangedWeapons.miscMod,
            flexFill),
          tightfrow(
            span(EPStyle.lineLabel, t.dmg),
            char.rangedWeapons.damageType,
            char.rangedWeapons.damageTypeShort.hidden,
            char.rangedWeapons.numDamageDice,
            span("d10+"),
            char.rangedWeapons.damageBonus,
            span(" / "),
            (t.ap -> char.rangedWeapons.armourPenetration),
            flexFill),
          tightfrow(
            span(EPStyle.lineLabel, t.firingModes),
            (t.singleShot -> char.rangedWeapons.singleShot),
            (t.semiAutomatic -> char.rangedWeapons.semiAutomatic),
            (t.burstFire -> char.rangedWeapons.burstFire),
            (t.fullAutomatic -> char.rangedWeapons.fullAutomatic),
            flexFill),
          tightfrow(span(EPStyle.lineLabel, t.weaponRanges),
            span(raw(" ")),
            span(EPStyle.inlineLabel, t.shortRange),
            char.rangedWeapons.shortRangeLower, span(raw("-")), char.rangedWeapons.shortRangeUpper, span(raw("m ")),
            span(EPStyle.inlineLabel, t.mediumRange),
            char.rangedWeapons.mediumRangeLower, span(raw("-")), char.rangedWeapons.mediumRangeUpper, span(raw("m ")),
            span(EPStyle.inlineLabel, t.longRange),
            char.rangedWeapons.longRangeLower, span(raw("-")), char.rangedWeapons.longRangeUpper, span(raw("m ")),
            span(EPStyle.inlineLabel, t.extremeRange),
            char.rangedWeapons.extremeRangeLower, span(raw("-")), char.rangedWeapons.extremeRangeUpper, span(raw("m")),
            flexFill),
          tightfrow(
            span(EPStyle.lineLabel, t.magazine),
            char.rangedWeapons.magazineCurrent, span("/"), char.rangedWeapons.magazineSize,
            (t.ammoType -> char.rangedWeapons.magazineType),
            flexFill),
          tightrow(sty.halfRemRowSeparator,
            span(sty.lineLabel, t.weaponDescription),
            char.rangedWeapons.description.like(CoreTabRenderer.textareaField)))))
    });

  val armourWorn: SheetElement = block(t.armourWorn,
    tightfrow(
      flexFill,
      (t.energy -> char.armourEnergyBonus),
      (t.kinetic -> char.armourKineticBonus),
      (t.layeringPenalty -> char.layeringPenalty),
      flexFill),
    div(sty.smallWrapBoxTitle, sty.halfRemRowSeparator, span(t.armourActiveTotal)),
    char.armourItems {
      TightRepRow(
        presOnly(tightfrow(
          char.armourItems.active,
          char.armourItems.itemName.like(rowItemName),
          span("(", span(name := char.armourItems.energyBonus.name), "/", span(name := char.armourItems.kineticBonus.name), ")"),
          flexFill)),
        editOnly(tightfrow(
          char.armourItems.active,
          char.armourItems.itemName.like(CoreTabRenderer.textWithPlaceholder(t.armourName.placeholder)),
          (t.armourAccessory -> char.armourItems.accessory),
          (t.energy -> char.armourItems.energyBonus),
          (t.kinetic -> char.armourItems.kineticBonus),
          flexFill)))
    });

  val currency: SheetElement = sblock(t.currency, sty.growFull,
    (t.cryptoCredits -> char.cryptoCredits));

  val equipment: SheetElement = block(t.equipment,
    char.equipment {
      TightRepRow(
        presOnly(flowpar(
          char.equipment.itemName.like(rowItemName),
          span("["), char.equipment.amount.like(CoreTabRenderer.presEditableNum), span("] "),
          char.equipment.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(
          char.equipment.itemName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          char.equipment.amount,
          (t.equipmentDescription -> char.equipment.description.like(CoreTabRenderer.textareaField)),
          flexFill)))
    });

  val fireModes: SheetElement = block(t.firingModes,
    flowpar(
      span(sty.tfrowName, t.singleShot.fullLabel.text),
      span(raw(" (")), span(t.singleShot), span(raw(")")),
      CoreTabRenderer.labelDescription(t.singleShotDescription)),
    flowpar(
      span(sty.tfrowName, t.semiAutomatic.fullLabel.text),
      span(raw(" (")), span(t.semiAutomatic), span(raw(")")),
      CoreTabRenderer.labelDescription(t.semiAutomaticDescription)),
    flowpar(
      span(sty.tfrowName, t.burstFire.fullLabel.text),
      span(raw(" (")), span(t.burstFire), span(raw(")")),
      CoreTabRenderer.labelDescription(t.burstFireDescription)),
    flowpar(
      span(sty.tfrowName, t.fullAutomatic.fullLabel.text),
      span(raw(" (")), span(t.fullAutomatic), span(raw(")")),
      CoreTabRenderer.labelDescription(t.fullAutomaticDescription)));

  val members: Seq[SheetElement] = Seq(frow(sty.`flex-start`,
    fcol(Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem),
      meleeWeapons,
      rangedWeapons,
      fireModes),
    fcol(Seq(EPStyle.`flex-grow`, sty.exactly20rem),
      armourWorn,
      currency,
      equipment)),
    frow(sty.`flex-stretch`,
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear1.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear2.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear3.like(CoreTabRenderer.largeTextareaField)))));

  override def renderer = CoreTabRenderer;
}
