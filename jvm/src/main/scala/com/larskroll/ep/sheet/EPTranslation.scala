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

object EPTranslation extends SheetI18N {

  val version = text("version", "v");
  val charName = text("character-name", "Character Name");
  val author = text("author", "Author");
  val github = text("github", "Github");

  val note = text("note", "Note");

  val characterInfo = text("character-info", "Character Info");
  val background = text("background", "Background");
  val faction = text("faction", "Faction");
  val genderId = text("gender-id", "Gender Identity");
  val actualAge = text("actual-age", "Actual Age");
  val currentMoxie = text("current-moxie", "Current Moxie Points");
  val rezPoints = abbr("rez", "REZ", "rez-points", "Rez Points");
  val motivations = text("motivations", "Motivations");
  val traits = text("traits", "Traits");
  val specialRolls = text("special-rolls", "Special Rolls");
  val successRoll = text("success-roll", "Success Roll");
  val wilx2Roll = abbr("wilx2", "WIL × 2", "willpowerx2", "Willpower doubled");
  val wilx3Roll = abbr("wilx3", "WIL × 3", "willpowerx3", "Willpower tripled");
  val somx3Roll = abbr("somx3", "SOM × 3", "somaticsx3", "Somatics tripled");
  val intx3Roll = abbr("intx3", "INT × 3", "intuitionx3", "Intuition tripled");
  val cogx3Roll = abbr("cogx3", "COG × 3", "cognitionx3", "Cognition tripled");
  val frayHalvedRoll = abbr("fraydiv2", "Fray/2", "frayhalved", "Fray halved");
  val durEnergyRoll = abbr("dur-energy-armor", "DUR + Energy Armor", "durability-energy-armor", "Durability + Energy Armor");
  val refCoox2Roll = abbr("ref-coox2", "REF + COO × 2", "reflex-coordinationx2", "Reflect + Coordination doubled");
  val cooSomRoll = abbr("coo-som", "COO + SOM", "coordination-somatics", "Coordination + Somatics");
  val psiDefense = text("psi-defense", "Psi Defense");
  val continuityTest = text("continutity-test", "Continuity Test");
  val resistTraumaDisorientation = text("resist-trauma-disorientation", "Resist Trauma Disorientation");
  val stressTest = text("stress-test", "Stress Test");
  val healTrauma = text("heal-trauma", "Heal Trauma");
  val resistWoundKnockdown = text("resist-wound-knockdown", "Resist Knockdown from Wound");
  val integrationTest = text("integration-test", "Integration Test");
  val alienationTest = text("alienation-test", "Alienation Test");
  val rangedDefence = text("ranged-defence", "Ranged Defence");
  val resistShock = text("resist-shock", "Resist Shock");
  val bruteStrength = text("brute-strength", "Brute Strength");
  val catchingObjects = text("catching-objects", "Catching Objects");
  val escapeArtist = text("escape-artist", "Escape Artist");
  val havingAnIdea = text("having-an-idea", "Having an Idea");
  val memoriseRecall = text("memorise-recall", "Memorise/Recall");

  val aptitudes = text("aptitudes", "Aptitudes");
  val cog = abbr("cog", "COG", "cognition", "Cognition");
  val coo = abbr("coo", "COO", "coordination", "Coordination");
  val int = abbr("int", "INT", "intuition", "Intuition");
  val ref = abbr("ref", "REF", "reflex", "Reflex");
  val sav = abbr("sav", "SAV", "savy", "Savy");
  val som = abbr("som", "SOM", "somatics", "Somatics");
  val wil = abbr("wil", "WIL", "willpower", "Willpower");
  val aptBase = text("apt-base", "Base");
  val aptMorphBonus = text("apt-morph-bonus", "Morph Bonus");
  val aptMorphMax = text("apt-morph-max", "Morph Max");
  val aptTemp = text("apt-temp", "Temp");
  val aptTotal = text("apt-total", "Total");

  val stats = text("stats", "Character Stats");
  val mox = abbr("mox", "MOX", "moxie", "Moxie");
  val tt = abbr("tt", "TT", "trauma-threshold", "Trauma Threshold");
  val luc = abbr("luc", "LUC", "lucidity", "Lucidity");
  val ir = abbr("ir", "IR", "insanity-rating", "Insanity Rating");
  val wt = abbr("wt", "WT", "wound-threshold", "Wound Threshold");
  val dur = abbr("dur", "DUR", "durability", "Durability");
  val dr = abbr("dr", "DR", "death-rating", "Death Rating");
  val init = abbr("init", "INIT", "initiative", "Initiative");
  val spd = abbr("spd", "SPD", "speed", "Speed");
  val db = abbr("db", "DB", "damage-bonus", "Damage Bonus");

  val mentalHealth = text("mental-health", "Mental Health");
  val stress = text("stress", "Stress");
  val stressValue = abbr("sv", "SV", "stress-value", "Stress Value");
  val trauma = text("trauma", "Trauma");
  val physicalHealth = text("physical-health", "Physical Health");
  val damage = text("damage", "Damage");
  val wounds = text("wounds", "Wounds");
  val woundsIgnored = text("wounds-ignored", "Ignored Wounds")

  val armour = text("armour", "Armour");
  val kinetic = text("kinetic", "Kinetic");
  val energy = text("energy", "Energy");

  val skills = text("skills", "Skills");
  val activeSkills = text("active-skills", "Active Skills");
  val knowledgeSkills = text("knowledge-skills", "Knowledge Skills");
  val skillName = text("skill-name", "Name");
  val skillField = text("skill-field", "Field");
  val skillCategories = text("skill-categories", "Categories");
  val skillSpecialisations = text("skill-specialisations", "Specialisations");
  val skillLinkedAptitude = text("skill-linked-aptitude", "Aptitude");
  val skillNoDefaulting = text("skill-no-defaulting", "No Defaulting");
  val skillRanks = text("skill-ranks", "Ranks");
  val skillMorphBonus = text("skill-morph-bonus", "Morph Bonus");
  val skillTotal = text("skill-total", "Total");
  val skillCategoryOptions = {
    import Skills.SkillCategory;
    import SkillCategory._;
    val opts = SkillCategory.values.map {
      case Combat    => (Combat.toString() -> "Combat")
      case Mental    => (Mental.toString() -> "Mental")
      case Physical  => (Physical.toString() -> "Physical")
      case Psi       => (Psi.toString() -> "Psi & Mental") // always go together
      case Social    => (Social.toString() -> "Social")
      case Technical => (Technical.toString() -> "Technical")
      case Vehicle   => (Vehicle.toString() -> "Vehicle")
      case NA        => (NA.toString() -> "None")
    }.toMap;
    enum(SkillCategory.labelPrefix, opts)
  }
  val skillCategoryOptionsShort = {
    import Skills.SkillCategory;
    import SkillCategory._;
    val opts = SkillCategory.values.map {
      case Combat    => (Combat.toString() -> "C")
      case Mental    => (Mental.toString() -> "M")
      case Physical  => (Physical.toString() -> "P")
      case Psi       => (Psi.toString() -> "\u03A8&M")
      case Social    => (Social.toString() -> "S")
      case Technical => (Technical.toString() -> "T")
      case Vehicle   => (Vehicle.toString() -> "V")
      case NA        => (NA.toString() -> " ")
    }.toMap;
    enum(SkillCategory.labelShortPrefix, opts)
  }

  val skillClassOptions = {
    import Skills.SkillClass;
    import SkillClass._;
    val opts = SkillClass.values.map {
      case Active    => (Active.toString() -> "Active")
      case Knowledge => (Knowledge.toString() -> "Knowledge")
    }.toMap;
    enum(SkillClass.labelPrefix, opts)
  }

  //  val skillClassOptionsShort = {
  //    import Skills.SkillClass;
  //    import SkillClass._;
  //    val opts = SkillClass.values.map {
  //      case Active    => (Active.toString() -> "A")
  //      case Knowledge => (Knowledge.toString() -> "K")
  //    }.toMap;
  //    enum(SkillClass.labelShortPrefix, opts)
  //  }

  val skillSortOptions = {
    import Skills.SortBy;
    import SortBy._;

    val opts = SortBy.values.map {
      case None     => (None.toString() -> " - ")
      case Name     => (Name.toString() -> "Name")
      case Category => (Category.toString() -> "Category")
      case Aptitude => (Aptitude.toString() -> "Aptitude")
    }.toMap;
    enum(SortBy.labelPrefix, opts)
  }

  // ****************
  // ADD OPTIONS HERE
  // ****************
  lazy val allFullOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptions,
    Skills.SkillClass -> skillClassOptions,
    Skills.SortBy -> skillSortOptions,
    DamageType -> dmgType,
    PsiType -> psiType);

  lazy val allShortOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptionsShort,
    DamageType -> dmgTypeShort,
    PsiType -> psiTypeShort);
  //Skills.SkillClass -> skillClassOptionsShort);

  val skillsGenerate = text("generate-skills", "Generate Default Skills");
  val skillsGenerating = text("generating-skills", "Generating Skills...");
  val skillsSortBy = text("skills-sort-by", "Sort by");
  val skillsSort = text("skills-sort", "Sort Now");
  val skillCommands = text("skill-commands", "Commands");

  val skillReloadPage = text("skill-reload-page", "Sorting result is only shown after reopening the sheet.");
  val skillNoSortManual = text("skill-no-sort-manual", "Due to Roll20 limitations, manually added items can not be sorted automatically at this time.");

  val core = text("core", "Core");
  val morph = text("morphs", "Morph");
  val activeMorph = text("active-morph", "Active Morph");
  val morphBank = text("morph-bank", "Morph Bank");
  val morphType = text("morph-type", "Type");
  val morphName = text("morph-name", "Model");
  val morphLabel = text("morph-label", "Label");
  val morphDescription = text("morph-description", "Description");
  val morphLocation = text("morph-location", "Storage Location");
  val morphTraits = text("morph-traits", "Traits");
  val morphImplants = text("morph-implants", "Implants/Enhancements");
  val morphMobilitySystem = text("morph-mobility-system", "Mobility System(s)");
  val morphDurability = text("morph-durability", "Durability");
  val morphArmour = text("morph-armour", "Armour");
  val morphArmourEnergy = text("morph-armour-energy", "Energy Armour");
  val morphArmourKinetic = text("morph-armour-kinetic", "Kinetic Armour");
  val morphAptitudeBoni = text("morph-aptitude-boni", "Aptitude Boni");
  val morphAptitudeMax = text("morph-aptitude-max", "Aptitude Max");
  val morphSkillBoni = text("morph-skill-boni", "Skill Boni");
  val morphVisibleAge = text("morph-visible-age", "Visible Age");
  val morphVisibleGender = text("morph-visible-gender", "Visible Gender");

  val gear = text("gear", "Gear");
  val gearFreeform = text("gear-freeform", "Freeform Gear");
  val meleeWeapons = text("melee-weapons", "Melee Weapons");
  val rangedWeapons = text("ranged-weapons", "Ranged Weapons");
  val armourWorn = text("armour-worn", "Armour Worn");
  val armourActiveTotal = text("armour-active-total", "Active Bonus");
  val layeringPenalty = text("armour-layering-penalty", "Layered Armour Penalty")
  val armourName = text("armour-name", "Name");
  val armourAccessory = text("armour-accessory", "Armour Accessory");
  val equipment = text("equipment", "Equipment");
  val equipmentName = text("equipment-name", "Name");
  val equipmentDescription = text("equipment-description", "Description");
  val currency = text("currency", "Currency");
  val cryptoCredits = text("crypto-credit", "Crypto Credits");
  val cash = text("cash", "Cash (Credit Chips)");

  val ap = abbr("ap", "AP", "armour-penetration", "Armour Penetration");
  val orTotalAP = text("or-total-ap", "or ignore armour if critical success");
  val dmg = abbr("weapon-dmg", "Dmg", "weapon-damage", "Damage");
  val dmgType = {
    import DamageType._;
    val opts = DamageType.values.map {
      case Kinetic => (Kinetic.toString -> "Kinetic")
      case Energy  => (Energy.toString -> "Energy")
    }.toMap;
    enum(DamageType.labelPrefix, opts)
  }
  val dmgTypeShort = {
    import DamageType._;
    val opts = DamageType.values.map {
      case Kinetic => (Kinetic.toString -> "K")
      case Energy  => (Energy.toString -> "E")
    }.toMap;
    enum(DamageType.labelShortPrefix, opts)
  }
  val weaponName = text("weapon-name", "Weapon Name");
  val weaponSkill = text("weapon-skill", "Skill");
  val weaponSkillSearch = text("weapon-skill-search", "Search Skill");
  val weaponDescription = text("weapon-description", "Description");
  val firingModes = text("fire-modes", "Firing Modes");
  val singleShot = abbr("ss", "SS", "single-shot", "Single Shot");
  val semiAutomatic = abbr("sa", "SA", "semi-automatic", "Semi-Automatic");
  val burstFire = abbr("bf", "BF", "burst-fire", "Burst Fire");
  val fullAutomatic = abbr("fa", "FA", "full-automatic", "Full Automatic");
  val singleShotDescription = text("single-shot-description", """
Single shot weapons may only be fired once per Complex Action.
""".trim);
  val semiAutomaticDescription = text("semi-automatic-description", """
Semi-automatic weapons may be fired twice with the same Complex Action. Each shot is handled as a separate attack.
""".trim);
  val burstFireDescription = text("burst-fire-description", """
Two bursts maybe fired with the same Complex Action. Each burst is handled as a separate attack. Bursts use up 3 shots worth of ammunition.
A burst may be shot against a single target (concentrated fire) or against two targets within one meter of each other. Against a single target, the attacker can choose either a +10 modifier to hit or increase the DV by +1d10.
""".trim);
  val fullAutomaticDescription = text("full-automatic-description", """
Only one full-auto attack may be made with each Complex Action. This attack may be made on a single target or against up to three separate targets within one meter of another. Against a single individual, the attacker can choose either a +30 modifier to hit or increase the DV by +3d10. Firing in full automatic mode uses up 10 shots.
""".trim);
  val weaponRanges = text("weapon-ranges", "Ranges");
  val shortRange = abbr("s-range", "S", "short-range", "Short");
  val mediumRange = abbr("m-range", "M", "medium-range", "Medium");
  val longRange = abbr("l-range", "L", "long-range", "Long");
  val extremeRange = abbr("x-range", "X", "extreme-range", "Extreme");
  val magazine = text("magazine", "Magazine");
  val size = text("size", "Size");
  val ammoType = text("ammo-type", "Ammo Type");

  val damageInflicts = text("damage-inflicts", "Inflicts");
  val damageValue = abbr("dv", "DV", "damage-value", "Damage Value");
  val concentrateFire = text("concentrate-fire", "Concentrate Fire");

  // identities
  val identities = text("identities", "Identities");
  val identity = text("identity", "Identity");
  val idDescription = text("id-description", "Description");
  val idCredits = text("id-credits", "Credits");
  val idNotes = text("id-notes", "Notes");

  val reputation = text("reputaion", "Reputation");
  val repScore = text("rep-score", "Score");
  val calledInFavours = text("called-in-favours", "Called in Favours");
  val lvl1 = abbr("lvl1", "Lvl 1", "level1", "Level 1");
  val lvl2 = abbr("lvl2", "Lvl 2", "level2", "Level 2");
  val lvl3 = abbr("lvl3", "Lvl 3", "level3", "Level 3");
  val lvl4 = abbr("lvl4", "Lvl 4", "level4", "Level 4");
  val lvl5 = abbr("lvl5", "Lvl 5", "level5", "Level 5");
  val atRep = abbr("atRep", "@-Rep", "circleAlist", "The Circle-A List (Autonomists)");
  val cRep = abbr("cRep", "c-Rep", "civicNet", "CivicNet (Hypercorps)");
  val eRep = abbr("eRep", "e-Rep", "ecoWave", "EcoWave (Ecologists)");
  val fRep = abbr("fRep", "f-Rep", "fame", "Fame (Media)");
  val gRep = abbr("gRep", "g-Rep", "guanxi", "Guanxi (Criminals)");
  val iRep = abbr("iRep", "i-Rep", "theEye", "The Eye (Firewall)");
  val rRep = abbr("rRep", "r-Rep", "rna", "Research Network Associates (Scientists)");
  val uRep = abbr("uRep", "u-Rep", "ultimateRep", "Ultimate (Ultimates)");
  val xRep = abbr("xRep", "x-Rep", "exploreNet", "ExploreNet (Gatecrashers)");

  // PSI
  val async = text("async", "Async");
  val asyncTrait = text("async-trait", "Has Asyc trait");
  val psi = text("psi", "Psi");
  val psiTempTime = text("psi-temp-time", "Temporary Duration");
  val psiTempUnits = text("psi-temp-units", "units");
  val psiSustained = text("psi-sustained", "Sustained Sleights");
  val psiCurrent = text("psi-current", "Currently Sustained");
  val psiSustainedMod = text("psi-sustained-mod", "Skill Penalty");
  val psiChi = text("psi-chi", "Psi-chi Sleights");
  val psiGamma = text("psi-gamma", "Psi-gamma Sleights");
  val sleightName = text("sleight-name", "Name");
  val sleightDescription = text("sleight-description", "Description");
  val psiRange = text("psi-range", "Range");
  val psiAction = text("psi-action", "Action");
  val psiDuration = text("psi-duration", "Duration");
  val strainMod = text("psi-strain-mod", "Strain Mod");
  val psiSkill = text("psi-skill", "Skill");
  val psiType = {
    import PsiType._;
    val opts = PsiType.values.map {
      case Active  => (Active.toString -> "Active")
      case Passive => (Passive.toString -> "Passive")
    }.toMap;
    enum(PsiType.labelPrefix, opts)
  }
  val psiTypeShort = {
    import PsiType._;
    val opts = PsiType.values.map {
      case Active  => (Active.toString -> "A")
      case Passive => (Passive.toString -> "P")
    }.toMap;
    enum(PsiType.labelShortPrefix, opts)
  }

  // MUSE
  val muse = text("muse", "Muse");
  val museInfo = text("muse-info", "Muse Info");
  val museName = text("muse-name", "Name");
  val museNotes = text("muse-notes", "Notes");
  val museSkills = text("muse-skills", "Muse Skills");

  // MISC
  val options = text("options", "Options");
  val sheetSettings = text("sheet-settings", "Sheet Settings");
  val miscModifiers = text("misc-modifiers", "Misc. Modifiers");
  val miscActionMod = text("misc-action-mod", "Misc. All Actions Modifier");
  //val weightUnit = text("weight-unit", "Weight Unit");
  val miscNotes = text("misc-notes", "Misc. Notes");
  val miscPhysicalMod = text("misc-physical-mod", "Misc. Physical Modifier");
  val miscInitiativeMod = text("misc-initiative-mod", "Misc. Initiative Modifier");

  // templates
  val rollsfor = text("rolls-for", "rolls for");
  val rollSuccess = text("roll-success", "Roll is a success");
  val rollCritSuccess = text("roll-crit-success", "Roll is a critical success");
  val rollAutoSuccess = text("roll-auto-success", "Roll is an automatic success");
  val rollFailure = text("roll-failure", "Roll is a failure");
  val rollCritFailure = text("roll-crit-failure", "Roll is a critical failure");
  val rollAutoFailure = text("roll-auto-failure", "Roll is an automatic failure");
  val mos = abbr("mos", "MoS", "margin-of-success", "Margin of Success");
  val mof = abbr("mof", "MoF", "margin-of-failure", "Margin of Failure");

}
