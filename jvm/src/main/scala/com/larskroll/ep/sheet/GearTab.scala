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

  val meleeDamageRollExcellent60 = roll(char.meleeWeapons, "damage_roll_excellent60", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRollExcellent60, char.meleeWeapons.armourPenetration));
  val meleeDamageRollExcellent30 = roll(char.meleeWeapons, "damage_roll_excellent30", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRollExcellent30, char.meleeWeapons.armourPenetration));
  val meleeDamageRoll = roll(char.meleeWeapons, "damage_roll", Chat.Default, EPDamageTemplate(char.characterName, char.meleeWeapons.weapon, char.meleeWeapons.damageRoll, char.meleeWeapons.armourPenetration),
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
    char.meleeWeapons(
      TightRepRow(
        presOnly(flowpar(
          meleeAttackRoll,
          span(" ("),
          char.meleeWeapons.skillName,
          span(") "),
          meleeDamageRoll,
          meleeDamageRollExcellent30.hidden,
          meleeDamageRollExcellent60.hidden,
          span(raw(" &mdash; ")),
          char.meleeWeapons.description,
          flexFill)),
        editOnly(tightfrow(
          char.meleeWeapons.weapon.like(CoreTabRenderer.textWithPlaceholder(t.weaponName.placeholder)),
          span(EPStyle.inlineLabel, t.weaponSkill),
          char.meleeWeapons.skillSearch.like(skillSearchBox),
          char.meleeWeapons.skillName,
          char.meleeWeapons.skillTotal.hidden,
          span(raw(" &mdash; ")),
          span(EPStyle.inlineLabel, t.dmg),
          char.meleeWeapons.numDamageDice,
          span("d10+"),
          char.meleeWeapons.damageBonus,
          span(" / "),
          (t.ap -> char.meleeWeapons.armourPenetration),
          (t.weaponDescription -> char.meleeWeapons.description.like(CoreTabRenderer.textareaField)),
          flexFill)))));

  val rangedWeapons: SheetElement = block(t.rangedWeapons, p("lala"));

  val armourWorn: SheetElement = block(t.armourWorn,
    tightfrow(
      flexFill,
      (t.energy -> char.armourEnergyBonus),
      (t.kinetic -> char.armourKineticBonus),
      (t.layeringPenalty -> char.layeringPenalty),
      flexFill),
    div(sty.smallWrapBoxTitle, sty.halfRemRowSeparator, span(t.armourActiveTotal)),
    char.armourItems(
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
          flexFill)))));

  val equipment: SheetElement = block(t.equipment,
    char.equipment(
      TightRepRow(
        presOnly(flowpar(
          char.equipment.itemName.like(rowItemName),
          span("("), char.equipment.amount.like(CoreTabRenderer.presEditableNum), span(") "),
          char.equipment.description,
          flexFill)),
        editOnly(tightfrow(
          char.equipment.itemName.like(CoreTabRenderer.textWithPlaceholder(t.equipmentName.placeholder)),
          char.equipment.amount,
          (t.equipmentDescription -> char.equipment.description.like(CoreTabRenderer.textareaField)),
          flexFill)))));

  val members: Seq[SheetElement] = Seq(frow(sty.`flex-start`,
    fcol(Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem),
      meleeWeapons,
      rangedWeapons),
    fcol(Seq(EPStyle.`flex-grow`, sty.exactly20rem),
      armourWorn,
      equipment)),
    frow(sty.`flex-stretch`,
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear1.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.marginrp5rem, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear1.like(CoreTabRenderer.largeTextareaField))),
      fcol(Seq(EPStyle.`flex-grow`, sty.exactly15rem),
        block(t.gearFreeform,
          char.gear1.like(CoreTabRenderer.largeTextareaField)))));

  override def renderer = CoreTabRenderer;
}
