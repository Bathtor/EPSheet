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

object EPCharModel extends SheetModel {
  import FieldImplicitsLabels._

  implicit class LabelledField[T: Numeric](f: FieldLike[T]) {
    def labelled: RollExprs.LabelledRoll[T] =
      RollExprs.LabelledRoll(f.arith(), f.attr);
  }

  override def version = BuildInfo.version;

  override def outputTemplate: Option[APIOutputTemplate] = Some(APIOutputTemplateRef);

  implicit val ctx = this.renderingContext;

  val sheetName = "EPSheet";
  val author = "Lars Kroll";
  val github = "https://github.com/Bathtor/EPSheet";

  val characterSheet = text("character_sheet").default(s"$sheetName v???");

  val modQueryRaw = InputQuery("Test Modifier", Some(0));
  val modQuery = modQueryRaw.expr.label("user mod");
  // **** rolls ****
  val epRoll = roll(
    "ep-roll",
    Seq(100, 89, 78, 67, 56, 45, 34, 23, 12, 1).foldLeft[IntRollExpression](Dice.d100)((acc, v) => acc.cs `=` v) - 1
  );
  //val justEPRoll = button("just_ep_roll", epRoll);
  lazy val moxieTarget = roll("moxie-target", moxieMax.arith());
  lazy val moxiex10Target = roll("moxiex10-target", modQuery + moxie * 10);
  lazy val durTarget = roll("dur-target", modQuery + durability + globalPhysicalMods);
  lazy val customTarget = roll("custom-target", modQuery);
  lazy val willx2Target = roll("willx2-target", modQuery + wilTotal * 2 + globalMods);
  lazy val willx3Target = roll("willx3-target", modQuery + wilTotal * 3 + globalMods);
  lazy val somx3Target = roll("somx3-target", modQuery + somTotal * 3 + globalPhysicalMods);
  lazy val intx3Target = roll("intx3-target", modQuery + intTotal * 3 + globalMods);
  lazy val refx3Target = roll("refx3-target", modQuery + refTotal * 3 + globalMods);
  lazy val frayField = "fray_field".ref[Int].editable(false).default(refTotal);
  lazy val frayHalvedTarget = roll("fray-halved-target", modQuery + floor(frayField.altArith / 2) + globalPhysicalMods);
  lazy val durEnergyArmour =
    roll("dur-energy-armour-target", modQuery + durability - damage + armourEnergyTotal + globalPhysicalMods);
  lazy val refCoox2Target = roll("ref-coox2-target", modQuery + refTotal + cooTotal * 2 + globalPhysicalMods);
  lazy val cooSomTarget = roll("coo-som-target", modQuery + cooTotal + somTotal + globalPhysicalMods);
  lazy val cogx3Target = roll("cogx3-target", modQuery + cogTotal * 3 + globalMods);
  lazy val refCooWilTarget = roll("ref-coo-wil-target", modQuery + refTotal + cooTotal + wilTotal + globalPhysicalMods);
  lazy val wilCogTarget = roll("wil-cog-target", modQuery + wilTotal + cogTotal + globalMods);

  // **** core ****
  val background = text("background");
  val faction = text("faction");
  val genderId = text("gender_id");
  val actualAge = number[Int]("actual_age");
  val moxie = "moxie".default(0).validIn(0, 99, 1);
  val moxieMax = "moxie_max".default(0).validIn(0, 99, 1);
  val rezPoints = "rez_points".default(0).validIn(0, 999, 1);
  val motivations = text("motivations");
  // aptitudes
  val cogBase = "cog_base".default(0);
  val cogTemp = "cog_temp".default(0);
  val cogMorph = "cog_morph".editable(false).default(0);
  val cogMorphMax = "cog_morph_max".editable(false).default(20);
  val cogTotal = "cog_total".editable(false).default(0);
  lazy val cogTarget = roll("cog-target", modQuery + cogTotal + globalMods);
  val cooBase = "coo_base".default(0);
  val cooTemp = "coo_temp".default(0);
  val cooMorph = "coo_morph".editable(false).default(0);
  val cooMorphMax = "coo_morph_max".editable(false).default(20);
  val cooTotal = "coo_total".editable(false).default(0);
  lazy val cooTarget = roll("coo-target", modQuery + cooTotal + globalPhysicalMods);
  val intBase = "int_base".default(0);
  val intTemp = "int_temp".default(0);
  val intMorph = "int_morph".editable(false).default(0);
  val intMorphMax = "int_morph_max".editable(false).default(20);
  val intTotal = "int_total".editable(false).default(0);
  lazy val intTarget = roll("int-target", modQuery + intTotal + globalMods);
  val refBase = "ref_base".default(0);
  val refTemp = "ref_temp".default(0);
  val refMorph = "ref_morph".editable(false).default(0);
  val refMorphMax = "ref_morph_max".editable(false).default(20);
  val refTotal = "ref_total".editable(false).default(0);
  lazy val refTarget = roll("ref-target", modQuery + refTotal + globalPhysicalMods);
  val savBase = "sav_base".default(0);
  val savTemp = "sav_temp".default(0);
  val savMorph = "sav_morph".editable(false).default(0);
  val savMorphMax = "sav_morph_max".editable(false).default(20);
  val savTotal = "sav_total".editable(false).default(0);
  lazy val savTarget = roll("sav-target", modQuery + savTotal + globalMods);
  val somBase = "som_base".default(0);
  val somTemp = "som_temp".default(0);
  val somMorph = "som_morph".editable(false).default(0);
  val somMorphMax = "som_morph_max".editable(false).default(20);
  val somTotal = "som_total".editable(false).default(0);
  lazy val somTarget = roll("som-target", modQuery + somTotal + globalPhysicalMods);
  val wilBase = "wil_base".default(0);
  val wilTemp = "wil_temp".default(0);
  val wilMorph = "wil_morph".editable(false).default(0);
  val wilMorphMax = "wil_morph_max".editable(false).default(20);
  val wilTotal = "wil_total".editable(false).default(0);
  lazy val wilTarget = roll("wil-target", modQuery + wilTotal + globalMods);
  // base stats
  val traumaThreshold = "trauma_threshold".editable(false).default(0);
  val lucidity = "lucidity".editable(false).default(0);
  val lucidityExtra = "lucidity_extra".default(0).validIn(-99, 99, 1);
  val lucidityTarget = roll("lucidity-target", modQuery + lucidity); // p. 272 I don't think any other mods apply
  val insanityRating = "insanity_rating".editable(false).default(0);
  val woundThreshold = "wound_threshold".editable(false).default(0);
  val durability = "durability".editable(false).default(0);
  val durabilityBonus = "durability_bonus".default(0).validIn(-99, 99, 1);
  val deathRating = "death_rating".editable(false).default(0);
  val initiative = "initiative".editable(false).default(0);
  val initiativeExtra = "initiative_extra".default(0).validIn(-99, 99, 1);
  //val initiativeFormula = number[Int]("initiative_formula").editable(false);
  lazy val iniRoll =
    roll("ini_roll", Dice.d10 + initiative - woundsApplied - traumasApplied + miscInitiativeMod & RollOptions.Tracker);
  val speed = "speed".editable(false).default(1);
  val speedExtra = "speed_extra".default(0).validIn(0, 99, 1);
  val mentalOnlyActions = "mental_only_actions".editable(false).default(0);
  val mentalOnlyActionsExtra = "mental_only_actions_extra".default(0).validIn(0, 99, 1);
  val damageBonus = "damage_bonus".editable(false).default(0);
  val stress = "stress".default(0).validIn(0, 999, 1);
  val stressMax = "stress_max".editable(false).default(0);
  val trauma = "trauma".default(0).validIn(0, 99, 1);
  val traumasIgnored = "traumas_ignored".default(0).validIn(0, 99, 1);
  val traumasIgnoredEffects = "traumas_ignored_effects".editable(false).default(0);
  val traumasApplied = "traumas_applied".editable(false).default(0);
  val traumaMod = "trauma_mod".editable(false).default(0);
  val damage = "damage".default(0).validIn(0, 999, 1);
  val damageMax = "damage_max".editable(false).default(0);
  val wounds = "wounds".default(0).validIn(0, 99, 1);
  val woundMod = "wound_mod".editable(false).default(0);
  val woundsIgnored = "wounds_ignored".default(0).validIn(0, 99, 1);
  val woundsIgnoredEffects = "wounds_ignored_effects".editable(false).default(0);
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
  //val morphLookupTraits = "morph_traits_lookup".default(false);
  val morphImplants = "morph_implants".editable(false).default("");
  //val morphLookupImplants = roll("implant_lookup", APIButton.)
  val morphMobilitySystem = "morph_mobility_system".editable(false).default("Walker 4/20");
  val morphDurability = "morph_durability".editable(false).default(0);
  val morphArmourEnergy = "morph_armour_energy".editable(false).default(0);
  val morphArmourKinetic = "morph_armour_kinetic".editable(false).default(0);
  val morphSkillBoni = "morph_skill_boni".editable(false).default("");
  val morphSpeed = "morph_speed".editable(false).default(1);
  val morphMOA = "morph_moa".editable(false).default(0);
  val morphIniBonus = "morph_ini_bonus".editable(false).default(0);
  val morphIgnoredWounds = "morph_ignored_wounds".editable(false).default(0);

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
  val rangeQueryRaw = LabelledSelectQuery(
    "Range",
    Seq("Short" -> 0, "Medium" -> -10, "Long" -> -20, "Extreme" -> -30, "Point Blank (<2m)" -> 10)
  );
  val rangeQuery = rangeQueryRaw.expr.label("range mod");
  val extraDamageQueryRaw =
    LabelledSelectQuery("Extra Damage", Seq("None" -> 0, "Excellent 30+" -> 5, "Excellent 60+" -> 10));
  val extraDamageQuery = extraDamageQueryRaw.expr.label("extra damage");
  val extraDamageDiceQueryRaw =
    LabelledSelectQuery("Extra Dice", Seq("None" -> 0, "Burst Fire" -> 1, "Full Automatic" -> 3));
  val extraDamageDiceQuery = DiceExprs.BasicRoll(extraDamageDiceQueryRaw.param, 10).label("extra damage");
  lazy val rangedWeapons = RangedWeaponSection;
  val rangedConcBFXDmg = roll("ranged_conc_bf_xdmg", 1.d(10));
  val rangedConcFAXDmg = roll("ranged_conc_fa_xdmg", 3.d(10));
  val armourItems = ArmourItemSection;
  val armourEnergyBonus = "armour_energy_bonus".editable(false).default(0);
  val armourKineticBonus = "armour_kinetic_bonus".editable(false).default(0);
  val layeringPenalty = "layering_penalty".editable(false).default(0);
  val equipment = GearSection;
  val software = SoftwareSection;
  val cryptoCredits = "crypto_currency".default(0).validIn(-999999999, 999999999, 1);
  val cash = "cash".default(0).validIn(-999999999, 999999999, 1);
  val gear1 = "gear1".default("");
  val gear2 = "gear2".default("");
  val gear3 = "gear3".default("");

  // identities
  lazy val identities = IdentitiesSection;

  // PSI
  val async = "is_async".default(false);
  val psiTempTime = "psi_temp_time".editable(false).default(0);
  val psiCurrentSustained = "psi_current_sustained".default(0).validIn(0, 10, 1);
  val psiSustainedMod = "psi_sustained_mod".editable(false).default(0);
  val targetQueryRaw = LabelledSelectQuery(
    "Target Type",
    Seq("Normal" -> 0, "Partially Sapient/Uplifted Animals" -> -20, "Non-sapient Animals" -> -30)
  );
  val targetQuery = targetQueryRaw.expr.label("sentience mod");
  val targetStrainQueryRaw = LabelledSelectQuery(
    "Target Type",
    Seq("Normal" -> 0, "Partially Sapient/Uplifted Animals" -> 1, "Non-sapient Animals" -> 3)
  );
  val targetStrainQuery = targetStrainQueryRaw.expr.label("sentience mod");
  lazy val psiChi = PsiChiSection;
  lazy val psiGamma = PsiGammaSection;
  val psychicStabDamage = roll("psychic-stab-damage", 1.d(10) + ceil(wilTotal / 10) + extraDamageQuery.arith);

  // MUSE
  val museName = text("muse_name");
  val museCog = "muse_cog".default(10);
  lazy val museCogTarget = roll("muse-cog-target", modQuery + museCog - museTraumaMod);
  val museCoo = "muse_coo".default(10);
  lazy val museCooTarget = roll("muse-coo-target", modQuery + museCoo - museTraumaMod);
  val museInt = "muse_int".default(20);
  lazy val museIntTarget = roll("muse-int-target", modQuery + museInt - museTraumaMod);
  val museRef = "muse_ref".default(10);
  lazy val museRefTarget = roll("muse-ref-target", modQuery + museRef - museTraumaMod);
  val museSav = "muse_sav".default(10);
  lazy val museSavTarget = roll("muse-sav-target", modQuery + museSav - museTraumaMod);
  val museSom = "muse_som".default(10);
  lazy val museSomTarget = roll("muse-som-target", modQuery + museSom - museTraumaMod);
  val museWil = "muse_wil".default(10);
  lazy val museWilTarget = roll("muse-wil-target", modQuery + museWil - museTraumaMod);
  val museTraumaThreshold = "muse_trauma_threshold".editable(false).default(4);
  val museLucidity = "muse_lucidity".editable(false).default(20);
  val museInsanityRating = "muse_insanity_rating".editable(false).default(40);
  val museStress = "muse_stress".default(0).validIn(0, 990, 1);
  val museTrauma = "muse_trauma".default(0).validIn(0, 99, 1);
  val museTraumaMod = "muse_trauma_mod".editable(false).default(0);
  val museSkills = MuseSkillSection;
  val museNotes = text("muse_notes");
  val generateMuseSkills = "muse_skills_generate".default(false);
  val generateMuseSkillsLabel = "muse_skills_generate_label".editable(false).default("generate-skills"); // other possibility is "generating-skills"

  lazy val effects = EffectsSection;
  val appliedEffectsSummary = "applied_effects_summary".editable(false).default("");
  val freeformEffectsSummary = "freeform_effects_summary".editable(false).default("");
  lazy val characterTraits = CharacterTraitSection;
  lazy val derangements = DerangementSection;
  lazy val disorders = DisorderSection;

  // settings
  //val weightUnit = "weight_unit".options("kg", "lb").default("kg"); // not feasible with current roll20 repeating sections as field can't be accessed from within a RS
  val miscNotes = text("misc_notes");
  val miscActionMod = "misc_action_mod".default(0);
  val miscPhysicalMod = "misc_physical_mod".default(0);
  val miscInitiativeMod = "misc_initiative_mod".default(0);
  val chatOutputOther = "chat_output_other".default(Chat.Default);
  val chatOutputEPRolls = "chat_output_ep_rolls".default(Chat.Default);
  val chatOutputSelect = "chat_output_select".options(ChatOutput).default(ChatOutput.Public);
  val usingAPIScript = "using_api_script".default(false);
  val apiText = text("api_text");

  val globalMods = (miscActionMod - woundTraumaMods + psiSustainedMod).paren;
  val globalPhysicalMods = (globalMods + miscPhysicalMod + layeringPenalty).paren;
}
