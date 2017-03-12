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
  val github = "FILL URL!";

  val characterSheet = text("character_sheet").default(s"$sheetName v$version");

  val modQuery = InputQuery("Test Modifier", Some(0));
  // **** rolls ****
  val epRoll = roll("ep-roll", Seq(100, 89, 78, 67, 56, 45, 34, 23, 12, 1).
    foldLeft[IntRollExpression](Dice.d100)((acc, v) => acc.cs `=` v) - 1);
  //val justEPRoll = button("just_ep_roll", epRoll);
  lazy val moxieTarget = roll("moxie-target", modQuery.expr + currentMoxie);

  // **** core ****
  val background = text("background");
  val faction = text("faction");
  val genderId = text("gender_id");
  val actualAge = number[Int]("actual_age");
  val currentMoxie = "current_moxie".default(0);
  val rezPoints = "rez_points".default(0);
  val motivations = text("motivations");
  // aptitudes
  val cogBase = "cog_base".default(0);
  val cogMorph = "cog_morph".editable(false).default(0);
  val cogMorphMax = "cog_morph_max".editable(false).default(20);
  val cogTotal = "cog_total".editable(false).default(0);
  val cooBase = "coo_base".default(0);
  val cooMorph = "coo_morph".editable(false).default(0);
  val cooMorphMax = "coo_morph_max".editable(false).default(20);
  val cooTotal = "coo_total".editable(false).default(0);
  val intBase = "int_base".default(0);
  val intMorph = "int_morph".editable(false).default(0);
  val intMorphMax = "int_morph_max".editable(false).default(20);
  val intTotal = "int_total".editable(false).default(0);
  val refBase = "ref_base".default(0);
  val refMorph = "ref_morph".editable(false).default(0);
  val refMorphMax = "ref_morph_max".editable(false).default(20);
  val refTotal = "ref_total".editable(false).default(0);
  val savBase = "sav_base".default(0);
  val savMorph = "sav_morph".editable(false).default(0);
  val savMorphMax = "sav_morph_max".editable(false).default(20);
  val savTotal = "sav_total".editable(false).default(0);
  val somBase = "som_base".default(0);
  val somMorph = "som_morph".editable(false).default(0);
  val somMorphMax = "som_morph_max".editable(false).default(20);
  val somTotal = "som_total".editable(false).default(0);
  val wilBase = "wil_base".default(0);
  val wilMorph = "wil_morph".editable(false).default(0);
  val wilMorphMax = "wil_morph_max".editable(false).default(20);
  val wilTotal = "wil_total".editable(false).default(0);
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
  lazy val iniRoll = roll("ini_roll", Dice.d10 + initiative - wounds - trauma & RollOptions.Tracker);
  val speed = "speed".default(1);
  val damageBonus = "damage_bonus".editable(false).default(0);
  val stress = number[Int]("stress");
  val trauma = number[Int]("trauma");
  val damage = number[Int]("damage");
  val wounds = number[Int]("wounds");
  val armourEnergyTotal = "armour_energy_total".editable(false).default(0);
  val armourKineticTotal = "armour_kinetic_total".editable(false).default(0);

  // morph
  val currentMorph = "current_morph".editable(false).default("none");
  val morphType = "morph_type".editable(false).options(MorphType).default(MorphType.None);
  val morphName = text("morph_name").editable(false);
  val morphDescription = text("morph_description").editable(false);
  val morphTraits = "morph_traits".editable(false).default("");
  val morphImplants = "morph_implants".editable(false).default("");
  val morphMobilitySystem = "morph_mobility_system".editable(false).default("Walker 4/20");
  val morphDurability = "morph_durability".editable(false).default(0);
  val morphArmourEnergy = "morph_armour_energy".editable(false).default(0);
  val morphArmourKinetic = "morph_armour_kinetic".editable(false).default(0);

  val skills = SkillSection;
  val morphs = MorphSection;
}

object SkillSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "skills";
  val mod = "skillmod".default(0);
}

object MorphSection extends RepeatingSection {
  import FieldImplicits._;
  implicit val ctx = this.renderingContext;

  def name = "morphs";
  val id = "morphs_id".editable(false).default("?");
  val active = "morphs_active".default(false);
  val morphType = "morphs_type".options(MorphType).default(MorphType.None);
  val morphName = text("morphs_name");
  val morphDescription = text("morphs_description");
  val morphTraits = "morphs_traits".default("");
  val morphImplants = "morphs_implants".default("");
  val morphMobilitySystem = "morphs_mobility_system".default("Walker 4/20");
  val morphDurability = "morphs_durability".default(0);
  val morphArmourEnergy = "morphs_armour_energy".default(0);
  val morphArmourKinetic = "morphs_armour_kinetic".default(0);
  val aptitudeBoni = "morphs_aptitude_boni".default("");
  val aptitudeMax = "morphs_aptitude_max".default("");
  val skillBoni = "morphs_skill_boni".default("");
}
