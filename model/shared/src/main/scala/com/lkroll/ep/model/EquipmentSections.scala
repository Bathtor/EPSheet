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
package com.lkroll.ep.model

import com.lkroll.roll20.sheet.model._;
import com.lkroll.roll20.core._;

object MeleeWeaponSection extends RepeatingSection {
  import FieldImplicitsLabels._

  implicit val ctx = this.renderingContext;

  def name = "meleeweapons";

  val weapon = text("weapon");
  val skillSearch = "skill_search".options("Blades", "Clubs", "Exotic Melee Weapon: ...", "Unarmed Combat");
  val skillName = "skill_name".editable(false).default("none");
  val skillTotal = "skill_total".ref(EPCharModel.activeSkills.total);
  val attackTarget = roll("attack_target", EPCharModel.modQuery + skillTotal.altArith + EPCharModel.globalPhysicalMods);
  val armourPenetration = "armour_penetration".default(0).validIn(-99, 0, 1);
  val numDamageDice = "num_damage_dice".default(0).validIn(0, 99, 1);
  val showDivisor = "show_divisor".default(false).editable(false);
  val damageDivisor = "damage_divisor".default(1).validIn(1, 9, 1);
  val damageBonus = "damage_bonus".default(0).validIn(-99, 99, 1);
  val damageRoll = roll("damage", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + EPCharModel.damageBonus);
  val damageRollExcellent30 = roll("damage_excellent30", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + EPCharModel.damageBonus + 5);
  val damageRollExcellent60 = roll("damage_excellent60", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + EPCharModel.damageBonus + 10);
  val damageRollQuery = roll("damage_query", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + EPCharModel.damageBonus + EPCharModel.extraDamageQuery.arith);
  val damageType = "damage_type".options(DamageType).default(DamageType.Kinetic);
  val damageTypeShort = text("damage_type_short").editable(false).default(DamageType.dynamicLabelShort(DamageType.Kinetic));
  val showDescription = flag("show_description").default(false);
  val description = text("description");
}

object RangedWeaponSection extends RepeatingSection {
  import FieldImplicitsLabels._

  implicit val ctx = this.renderingContext;

  def name = "rangedweapons";

  val weapon = text("weapon");
  val skillSearch = "skill_search".options("Beam Weapons", "Exotic Ranged Weapon: ...", "Kinetic Weapons", "Seeker Weapons", "Spray Weapons", "Throwing Weapons");
  val skillName = "skill_name".editable(false).default("none");
  val skillTotal = "skill_total".ref(EPCharModel.activeSkills.total);
  val miscMod = "misc_mod".default(0);
  val attackTarget = roll("attack_target", EPCharModel.modQuery + skillTotal.altArith + EPCharModel.rangeQuery.arith + miscMod + EPCharModel.globalPhysicalMods);
  val armourPenetration = "armour_penetration".default(0).validIn(-99, 0, 1);
  val numDamageDice = "num_damage_dice".default(0).validIn(0, 99, 1);
  val showDivisor = "show_divisor".default(false).editable(false);
  val damageDivisor = "damage_divisor".default(1).validIn(1, 9, 1);
  val damageBonus = "damage_bonus".default(0).validIn(-99, 99, 1);
  val damageRoll = roll("damage", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus);
  val damageRollExcellent30 = roll("damage_excellent30", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + 5);
  val damageRollExcellent60 = roll("damage_excellent60", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + 10);
  val damageRollQuery = roll("damage_query", ceil(DiceExprs.BasicRoll(numDamageDice.expr, 10).arith / damageDivisor) + damageBonus + EPCharModel.extraDamageDiceQuery.arith + EPCharModel.extraDamageQuery.arith);
  val damageType = "damage_type".options(DamageType).default(DamageType.Kinetic);
  val damageTypeShort = text("damage_type_short").editable(false).default(DamageType.dynamicLabelShort(DamageType.Kinetic));
  val singleShot = "single_shot".default(false);
  val semiAutomatic = "semi_automatic".default(false);
  val burstFire = "burst_fire".default(false);
  val fullAutomatic = "full_automatic".default(false);
  val thrown = flag("thrown").default(false);
  val rangeUnitSymbol = "range_unit_symbol".editable(false).default("m");
  val shortRangeLower = "short_range_lower".default(2).editable(false);
  val shortRangeUpperInput = "short_range_upper_input".default(0.0).validIn(0.0, 99999.0, 0.1);
  val shortRangeUpper = "short_range_upper".default(0).editable(false);
  val mediumRangeLower = "medium_range_lower".default(0).editable(false);
  val mediumRangeUpperInput = "medium_range_upper_input".default(0.0).validIn(0.0, 99999.0, 0.1);
  val mediumRangeUpper = "medium_range_upper".default(0).editable(false);
  val longRangeLower = "long_range_lower".default(0).editable(false);
  val longRangeUpperInput = "long_range_upper_input".default(0.0).validIn(0.0, 99999.0, 0.1);
  val longRangeUpper = "long_range_upper".default(0).editable(false);
  val extremeRangeLower = "extreme_range_lower".default(0).editable(false);
  val extremeRangeUpperInput = "extreme_range_upper_input".default(0.0).validIn(0.0, 99999.0, 0.1);
  val extremeRangeUpper = "extreme_range_upper".default(0).editable(false);
  val magazineSize = "ammo_max".default(0).validIn(0, 999, 1);
  val magazineCurrent = "ammo".default(0).validIn(0, 999, 1);
  val magazineType = "ammo_type".default("standard");
  val damageArea = "damage_area".options(DamageArea).default(DamageArea.Point);
  val damageAreaShort = text("damage_area_short").editable(false).default(DamageArea.dynamicLabelShort(DamageArea.Point));
  val uniformBlastArea = "uniform_blast_area".default(1).validIn(0, 99999, 1);
  val showDescription = flag("show_description").default(false);
  val description = text("description");
}

object ArmourItemSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "armouritems";

  val itemName = text("item_name");
  val active = flag("active").default(false);
  val accessory = flag("accessory").default(false);
  val energyBonus = "energy_bonus".default(0).validIn(0, 999, 1);
  val kineticBonus = "kinetic_bonus".default(0).validIn(0, 999, 1);
  val showDescription = flag("show_description").default(false);
  val description = text("description");
}

object GearSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "gearitems";
  val itemName = text("item_name");
  val amount = "amount".default(0);
  val showDescription = flag("show_description").default(false);
  val description = text("description");
}

object SoftwareSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "software";
  val itemName = text("item_name");
  val quality = text("quality");
  val qualityMod = "quality_mod".default(0).validIn(-60, 60, 10);
  val showDescription = flag("show_description").default(false);
  val description = text("description");
}
