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

import com.larskroll.roll20.sheet._;

object EPCharModel extends SheetModel {
  import FieldImplicits._;

  override def version = BuildInfo.version; //"0.1.0";

  implicit val ctx = this.renderingContext;

  val sheetName = "EPSheet";
  val author = "Lars Kroll";
  val github = "https://github.com/Bathtor/EPSheet";

  val characterSheet = text("character_sheet").default(s"$sheetName v$version");

  val modQuery = InputQuery("Test Modifier", Some(0));
  // **** rolls ****
  val epRoll = roll("ep-roll", Seq(100, 89, 78, 67, 56, 45, 34, 23, 12, 1).
    foldLeft[IntRollExpression](Dice.d100)((acc, v) => acc.cs `=` v) - 1);
  //val justEPRoll = button("just_ep_roll", epRoll);
  lazy val moxieTarget = roll("moxie-target", modQuery.expr + currentMoxie);
  lazy val customTarget = roll("custom-target", modQuery.expr);
  lazy val willx2Target = roll("willx2-target", modQuery.expr + wilTotal * 2);
  lazy val willx3Target = roll("willx3-target", modQuery.expr + wilTotal * 3);
  lazy val somx3Target = roll("somx3-target", modQuery.expr + somTotal * 3);
  lazy val intx3Target = roll("intx3-target", modQuery.expr + intTotal * 3);
  // TODO fray/2 for ranged defense

  // **** core ****
  val background = text("background");
  val faction = text("faction");
  val genderId = text("gender_id");
  val actualAge = number[Int]("actual_age");
  val currentMoxie = "current_moxie".default(0);
  val rezPoints = "rez_points".default(0);
  val motivations = text("motivations");
  val charTraits = text("character_traits");
  // aptitudes
  val cogBase = "cog_base".default(0);
  val cogTemp = "cog_temp".default(0);
  val cogMorph = "cog_morph".editable(false).default(0);
  val cogMorphMax = "cog_morph_max".editable(false).default(20);
  val cogTotal = "cog_total".editable(false).default(0);
  lazy val cogTarget = roll("cog-target", modQuery.expr + cogTotal - EPCharModel.woundTraumaMods);
  val cooBase = "coo_base".default(0);
  val cooTemp = "coo_temp".default(0);
  val cooMorph = "coo_morph".editable(false).default(0);
  val cooMorphMax = "coo_morph_max".editable(false).default(20);
  val cooTotal = "coo_total".editable(false).default(0);
  lazy val cooTarget = roll("coo-target", modQuery.expr + cooTotal - EPCharModel.woundTraumaMods);
  val intBase = "int_base".default(0);
  val intTemp = "int_temp".default(0);
  val intMorph = "int_morph".editable(false).default(0);
  val intMorphMax = "int_morph_max".editable(false).default(20);
  val intTotal = "int_total".editable(false).default(0);
  lazy val intTarget = roll("int-target", modQuery.expr + intTotal - EPCharModel.woundTraumaMods);
  val refBase = "ref_base".default(0);
  val refTemp = "ref_temp".default(0);
  val refMorph = "ref_morph".editable(false).default(0);
  val refMorphMax = "ref_morph_max".editable(false).default(20);
  val refTotal = "ref_total".editable(false).default(0);
  lazy val refTarget = roll("ref-target", modQuery.expr + refTotal - EPCharModel.woundTraumaMods);
  val savBase = "sav_base".default(0);
  val savTemp = "sav_temp".default(0);
  val savMorph = "sav_morph".editable(false).default(0);
  val savMorphMax = "sav_morph_max".editable(false).default(20);
  val savTotal = "sav_total".editable(false).default(0);
  lazy val savTarget = roll("sav-target", modQuery.expr + savTotal - EPCharModel.woundTraumaMods);
  val somBase = "som_base".default(0);
  val somTemp = "som_temp".default(0);
  val somMorph = "som_morph".editable(false).default(0);
  val somMorphMax = "som_morph_max".editable(false).default(20);
  val somTotal = "som_total".editable(false).default(0);
  lazy val somTarget = roll("som-target", modQuery.expr + somTotal - EPCharModel.woundTraumaMods);
  val wilBase = "wil_base".default(0);
  val wilTemp = "wil_temp".default(0);
  val wilMorph = "wil_morph".editable(false).default(0);
  val wilMorphMax = "wil_morph_max".editable(false).default(20);
  val wilTotal = "wil_total".editable(false).default(0);
  lazy val wilTarget = roll("wil-target", modQuery.expr + wilTotal - EPCharModel.woundTraumaMods);
  // base stats
  val moxie = "moxie".default(0);
  val traumaThreshold = "trauma_threshold".editable(false).default(0);
  val lucidity = "lucidity".editable(false).default(0);
  val insanityRating = "insanity_rating".editable(false).default(0);
  val woundThreshold = "wound_threshold".editable(false).default(0);
  val durability = "durability".editable(false).default(0);
  val durabilityBonus = "durability_bonus".default(0);
  val deathRating = "death_rating".editable(false).default(0);
  val initiative = "initiative".editable(false).default(0);
  //val initiativeFormula = number[Int]("initiative_formula").editable(false);
  lazy val iniRoll = roll("ini_roll", Dice.d10 + initiative - woundsApplied - trauma & RollOptions.Tracker);
  val speed = "speed".default(1);
  val damageBonus = "damage_bonus".editable(false).default(0);
  val stress = number[Int]("stress");
  val trauma = number[Int]("trauma");
  val traumaMod = "trauma_mod".editable(false).default(0);
  val damage = number[Int]("damage");
  val wounds = number[Int]("wounds");
  val woundMod = "wound_mod".editable(false).default(0);
  val woundsIgnored = "wounds_ignored".default(0);
  val woundsApplied = "wounds_applied".editable(false).default(0);
  val woundTraumaMods = (traumaMod + woundMod).paren;
  val armourEnergyTotal = "armour_energy_total".editable(false).default(0);
  val armourKineticTotal = "armour_kinetic_total".editable(false).default(0);

  // morph
  val currentMorph = "current_morph".editable(false).default("none");
  val morphType = "morph_type".editable(false).options(MorphType).default(MorphType.None);
  val morphName = text("morph_name").editable(false);
  val morphDescription = text("morph_description").editable(false);
  val morphVisibleGender = text("visible_gender").editable(false);
  val morphVisibleAge = text("visible_age").editable(false);
  val morphTraits = "morph_traits".editable(false).default("");
  val morphImplants = "morph_implants".editable(false).default("");
  val morphMobilitySystem = "morph_mobility_system".editable(false).default("Walker 4/20");
  val morphDurability = "morph_durability".editable(false).default(0);
  val morphArmourEnergy = "morph_armour_energy".editable(false).default(0);
  val morphArmourKinetic = "morph_armour_kinetic".editable(false).default(0);
  val morphSkillBoni = "morph_skill_boni".editable(false).default("");

  // skills
  lazy val activeSkills = ActiveSkillSection;
  lazy val knowledgeSkills = KnowledgeSkillSection;
  val generateSkills = "skills_generate".default(false);
  val generateSkillsLabel = "skills_generate_label".editable(false).default("generate-skills"); // other possibility is "generating-skills"
  val sortSkills = "skills_sort".default(false);
  val sortSkillsBy = "skills_sort_by".options(Skills.SortBy).default(Skills.SortBy.None);
  val morphs = MorphSection;

  // gear
  lazy val meleeWeapons = MeleeWeaponSection;
  val rangeQuery = LabelledSelectQuery("Range",
    Seq("Short" -> 0, "Medium" -> -10, "Long" -> -20, "Extreme" -> -30));
  lazy val rangedWeapons = RangedWeaponSection;
  val rangedConcBFXDmg = roll("ranged_conc_bf_xdmg", 1.d(10));
  val rangedConcFAXDmg = roll("ranged_conc_fa_xdmg", 3.d(10));
  val armourItems = ArmourItemSection;
  val armourEnergyBonus = "armour_energy_bonus".editable(false).default(0);
  val armourKineticBonus = "armour_kinetic_bonus".editable(false).default(0);
  val layeringPenalty = "layering_penalty".editable(false).default(0);
  val equipment = GearSection;
  val cryptoCredits = "crypto_currency".default(0).validIn(-999999999, 999999999, 1);
  val gear1 = "gear1".default("");
  val gear2 = "gear2".default("");
  val gear3 = "gear3".default("");

  // identities
  lazy val identities = IdentitiesSection;

  // PSI
  lazy val psiChi = PsiChiSection;
  lazy val psiGamma = PsiGammaSection;

  // MUSE
  val museName = text("muse_name");
  val museCog = "muse_cog".default(10);
  val museCogTarget = roll("muse-cog-target", modQuery.expr + museCog);
  val museCoo = "muse_coo".default(10);
  val museCooTarget = roll("muse-coo-target", modQuery.expr + museCoo);
  val museInt = "muse_int".default(20);
  val museIntTarget = roll("muse-int-target", modQuery.expr + museInt);
  val museRef = "muse_ref".default(10);
  val museRefTarget = roll("muse-ref-target", modQuery.expr + museRef);
  val museSav = "muse_sav".default(10);
  val museSavTarget = roll("muse-sav-target", modQuery.expr + museSav);
  val museSom = "muse_som".default(10);
  val museSomTarget = roll("muse-som-target", modQuery.expr + museSom);
  val museWil = "muse_wil".default(10);
  val museWilTarget = roll("muse-wil-target", modQuery.expr + museWil);
  val museTraumaThreshold = "muse_trauma_threshold".editable(false).default(4);
  val museLucidity = "muse_lucidity".editable(false).default(20);
  val museInsanityRating = "muse_insanity_rating".editable(false).default(40);
  val museSkills = MuseSkillSection;
  val museNotes = text("muse_notes");
  val generateMuseSkills = "muse_skills_generate".default(false);
  val generateMuseSkillsLabel = "muse_skills_generate_label".editable(false).default("generate-skills"); // other possibility is "generating-skills"

  // settings
  //val weightUnit = "weight_unit".options("kg", "lb").default("kg"); // not feasible with current roll20 repeating sections as field can't be accessed from within a RS
  val miscNotes = text("misc_notes");
}

object MuseSkillSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "museskills";

  val skillName = text("name");
  val field = "field".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val ranks = "ranks".default(0);
  val total = number[Int]("total").editable(false);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total);
}

object PsiChiSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "psichi";

  val sleight = text("sleight");
  val psiType = "psi_type".options(PsiType).default(PsiType.Passive);
  val psiTypeShort = text("psi_type_short").editable(false).default(PsiType.dynamicLabelShort(PsiType.Passive));
  val range = "range".default("Self");
  val action = "action".default("Automatic");
  val duration = "duration".default("Constant");
  val strainMod = "strain_mod".default(0);
  val description = text("description");
}

object PsiGammaSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "psigamma";

  val sleight = text("sleight");
  val psiType = "psi_type".options(PsiType).default(PsiType.Active);
  val psiTypeShort = text("psi_type_short").editable(false).default(PsiType.dynamicLabelShort(PsiType.Active));
  val range = "range".default("Touch");
  val action = "action".default("Complex");
  val duration = "duration".default("Temp (Action Turns)");
  val strainMod = "strain_mod".default(0);
  val skill = text("skill");
  val description = text("description");
}

object IdentitiesSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "identities";

  val identity = text("identity");
  val description = text("description");
  val credits = "credits".default(0).validIn(-999999999, 999999999, 1);
  val notes = text("notes");

  val atRepScore = "at_rep_score".default(0).validIn(0, 99, 1);
  val atRepFavour1 = "at_rep_favour1".default(false);
  val atRepFavour2 = "at_rep_favour2".default(false);
  val atRepFavour3 = "at_rep_favour3".default(false);
  val atRepFavour4 = "at_rep_favour4".default(false);
  val atRepFavour5 = "at_rep_favour5".default(false);

  val cRepScore = "c_rep_score".default(0).validIn(0, 99, 1);
  val cRepFavour1 = "c_rep_favour1".default(false);
  val cRepFavour2 = "c_rep_favour2".default(false);
  val cRepFavour3 = "c_rep_favour3".default(false);
  val cRepFavour4 = "c_rep_favour4".default(false);
  val cRepFavour5 = "c_rep_favour5".default(false);

  val eRepScore = "e_rep_score".default(0).validIn(0, 99, 1);
  val eRepFavour1 = "e_rep_favour1".default(false);
  val eRepFavour2 = "e_rep_favour2".default(false);
  val eRepFavour3 = "e_rep_favour3".default(false);
  val eRepFavour4 = "e_rep_favour4".default(false);
  val eRepFavour5 = "e_rep_favour5".default(false);

  val fRepScore = "f_rep_score".default(0).validIn(0, 99, 1);
  val fRepFavour1 = "f_rep_favour1".default(false);
  val fRepFavour2 = "f_rep_favour2".default(false);
  val fRepFavour3 = "f_rep_favour3".default(false);
  val fRepFavour4 = "f_rep_favour4".default(false);
  val fRepFavour5 = "f_rep_favour5".default(false);

  val gRepScore = "g_rep_score".default(0).validIn(0, 99, 1);
  val gRepFavour1 = "g_rep_favour1".default(false);
  val gRepFavour2 = "g_rep_favour2".default(false);
  val gRepFavour3 = "g_rep_favour3".default(false);
  val gRepFavour4 = "g_rep_favour4".default(false);
  val gRepFavour5 = "g_rep_favour5".default(false);

  val iRepScore = "i_rep_score".default(0).validIn(0, 99, 1);
  val iRepFavour1 = "i_rep_favour1".default(false);
  val iRepFavour2 = "i_rep_favour2".default(false);
  val iRepFavour3 = "i_rep_favour3".default(false);
  val iRepFavour4 = "i_rep_favour4".default(false);
  val iRepFavour5 = "i_rep_favour5".default(false);

  val rRepScore = "r_rep_score".default(0).validIn(0, 99, 1);
  val rRepFavour1 = "r_rep_favour1".default(false);
  val rRepFavour2 = "r_rep_favour2".default(false);
  val rRepFavour3 = "r_rep_favour3".default(false);
  val rRepFavour4 = "r_rep_favour4".default(false);
  val rRepFavour5 = "r_rep_favour5".default(false);

  val uRepScore = "u_rep_score".default(0).validIn(0, 99, 1);
  val uRepFavour1 = "u_rep_favour1".default(false);
  val uRepFavour2 = "u_rep_favour2".default(false);
  val uRepFavour3 = "u_rep_favour3".default(false);
  val uRepFavour4 = "u_rep_favour4".default(false);
  val uRepFavour5 = "u_rep_favour5".default(false);

  val xRepScore = "x_rep_score".default(0).validIn(0, 99, 1);
  val xRepFavour1 = "x_rep_favour1".default(false);
  val xRepFavour2 = "x_rep_favour2".default(false);
  val xRepFavour3 = "x_rep_favour3".default(false);
  val xRepFavour4 = "x_rep_favour4".default(false);
  val xRepFavour5 = "x_rep_favour5".default(false);
}

object MeleeWeaponSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "meleeweapons";

  val weapon = text("weapon");
  val skillSearch = "skill_search".options("Blades", "Clubs", "Exotic Melee Weapon: ...", "Unarmed Combat");
  val skillName = "skill_name".editable(false).default("none");
  val skillTotal = "skill_total".ref(EPCharModel.activeSkills.total);
  val attackTarget = roll("attack_target", EPCharModel.modQuery.arith + skillTotal.altArith - EPCharModel.woundTraumaMods + EPCharModel.layeringPenalty);
  val armourPenetration = "armour_penetration".default(0);
  val numDamageDice = "num_damage_dice".default(0);
  val damageBonus = "damage_bonus".default(0);
  val damageRoll = roll("damage", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus + EPCharModel.damageBonus);
  val damageRollExcellent30 = roll("damage_excellent30", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus + EPCharModel.damageBonus + 5);
  val damageRollExcellent60 = roll("damage_excellent60", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus + EPCharModel.damageBonus + 10);
  val damageType = "damage_type".options(DamageType).default(DamageType.Kinetic);
  val damageTypeShort = text("damage_type_short").editable(false).default(DamageType.dynamicLabelShort(DamageType.Kinetic));
  val description = text("description");
}

object RangedWeaponSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "rangedweapons";

  val weapon = text("weapon");
  val skillSearch = "skill_search".options("Beam Weapons", "Exotic Ranged Weapon: ...", "Kinetic Weapons", "Seeker Weapons", "Spray Weapons", "Throwing Weapons");
  val skillName = "skill_name".editable(false).default("none");
  val skillTotal = "skill_total".ref(EPCharModel.activeSkills.total);
  val miscMod = "misc_mod".default(0);
  val attackTarget = roll("attack_target", EPCharModel.modQuery.arith + skillTotal.altArith + EPCharModel.rangeQuery.arith + miscMod - EPCharModel.woundTraumaMods + EPCharModel.layeringPenalty);
  val armourPenetration = "armour_penetration".default(0);
  val numDamageDice = "num_damage_dice".default(0);
  val damageBonus = "damage_bonus".default(0);
  val damageRoll = roll("damage", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus);
  val damageRollExcellent30 = roll("damage_excellent30", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus + 5);
  val damageRollExcellent60 = roll("damage_excellent60", DiceExprs.BasicRoll(numDamageDice.expr, 10) + damageBonus + 10);
  val damageType = "damage_type".options(DamageType).default(DamageType.Kinetic);
  val damageTypeShort = text("damage_type_short").editable(false).default(DamageType.dynamicLabelShort(DamageType.Kinetic));
  val singleShot = "single_shot".default(false);
  val semiAutomatic = "semi_automatic".default(false);
  val burstFire = "burst_fire".default(false);
  val fullAutomatic = "full_automatic".default(false);
  val shortRangeLower = "short_range_lower".default(0).editable(false);
  val shortRangeUpper = "short_range_upper".default(0).validIn(0, 99999, 1);
  val mediumRangeLower = "medium_range_lower".default(0).editable(false);
  val mediumRangeUpper = "medium_range_upper".default(0).validIn(0, 99999, 1);
  val longRangeLower = "long_range_lower".default(0).editable(false);
  val longRangeUpper = "long_range_upper".default(0).validIn(0, 99999, 1);
  val extremeRangeLower = "extreme_range_lower".default(0).editable(false);
  val extremeRangeUpper = "extreme_range_upper".default(0).validIn(0, 99999, 1);
  val magazineSize = "ammo_max".default(0);
  val magazineCurrent = "ammo".default(0);
  val magazineType = "ammo_type".default("standard");
  val description = text("description");
}

object ArmourItemSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "armouritems";

  val itemName = text("item_name");
  val active = flag("active").default(false);
  val accessory = flag("accessory").default(false);
  val energyBonus = "energy_bonus".default(0);
  val kineticBonus = "kinetic_bonus".default(0);
  val description = text("description");
}

object GearSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "gearitems";
  val itemName = text("item_name");
  val amount = "amount".default(0);
  val description = text("description");
}

object ActiveSkillSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "activeskills";

  val rowId = text("rowid").editable(false).default("?");
  val skillName = text("name");
  val field = "field".default("");
  val category = "category".options(Skills.SkillCategory);
  val categoryShort = text("category_short").editable(false);
  val specialisations = "specialisations".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val noDefaulting = "no_defaulting".default(false);
  val ranks = "ranks".default(0);
  val morphBonus = "morph_bonus".editable(false).default(0);
  val total = number[Int]("total").editable(false);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total - EPCharModel.woundTraumaMods + EPCharModel.layeringPenalty);
  val rollSpecTarget = roll("spec_target", EPCharModel.modQuery.arith + total - EPCharModel.woundTraumaMods + EPCharModel.layeringPenalty + 10);
}

object KnowledgeSkillSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "knowledgeskills";

  val rowId = text("rowid").editable(false).default("?");
  val skillName = text("name");
  val field = "field".default("");
  val specialisations = "specialisations".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val noDefaulting = "no_defaulting".default(false);
  val ranks = "ranks".default(0);
  val morphBonus = "morph_bonus".editable(false).default(0);
  val total = number[Int]("total").editable(false);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total - EPCharModel.woundTraumaMods);
  val rollSpecTarget = roll("spec_target", EPCharModel.modQuery.arith + total - EPCharModel.woundTraumaMods + 10);

}

object MorphSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "morphs";

  val id = "id".editable(false).default("?");
  val active = "active".default(false);
  val morphLabel = "morph_label".default("Unnamed");
  val morphType = "type".options(MorphType).default(MorphType.None);
  val morphName = text("morphs_name");
  val description = text("morphs_description");
  val visibleGender = text("visible_gender");
  val visibleAge = text("visible_age");
  val traits = "traits".default("");
  val implants = "implants".default("");
  val mobilitySystem = "mobility_system".default("Walker 4/20");
  val durability = "durability".default(0);
  val armourEnergy = "armour_energy".default(0);
  val armourKinetic = "armour_kinetic".default(0);
  val aptitudeBoni = "aptitude_boni".default("");
  val aptitudeMax = "aptitude_max".default("");
  val skillBoni = "skill_boni".default("");
}
