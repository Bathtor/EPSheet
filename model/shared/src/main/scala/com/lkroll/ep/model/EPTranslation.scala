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

object EPTranslation extends SheetI18N {
  val version = text("version");
  val charName = text("character-name");
  val author = text("author");
  val github = text("github");
  val note = text("note");

  // character
  val characterInfo = text("character-info");
  val background = text("background");
  val faction = text("faction");
  val genderId = text("gender-id");
  val actualAge = text("actual-age");
  val currentMoxie = text("current-moxie");
  val rezPoints = abbr("rez", "rez-points");
  val motivations = text("motivations");
  val effects = text("effects");
  val effectName = text("effect-name");
  val effectDuration = text("effect-duration");
  val effectDurationExample = text("effect-duration-example");
  val effectOnGame = text("effect-game");
  val effectOnGameExample = text("effect-game-example");
  val effectDescription = text("effect-description");
  val showHideDescription = text("show-hide-description");
  val traits = text("traits");
  val traitName = text("trait-name");
  val traitDescription = text("trait-description");
  val characterTraits = text("character-traits");
  val specialRolls = text("special-rolls");
  val successRoll = text("success-roll");
  val moxx10Roll = abbr("moxx10", "moxiex10");
  val wilx2Roll = abbr("wilx2", "willpowerx2");
  val wilx3Roll = abbr("wilx3", "willpowerx3");
  val somx3Roll = abbr("somx3", "somaticsx3");
  val intx3Roll = abbr("intx3", "intuitionx3");
  val cogx3Roll = abbr("cogx3", "cognitionx3");
  val refx3Roll = abbr("refx3", "reflexx3");
  val frayHalvedRoll = abbr("fraydiv2", "frayhalved");
  val durEnergyRoll = abbr("dur-energy-armor", "durability-energy-armor");
  val refCoox2Roll = abbr("ref-coox2", "reflex-coordinationx2");
  val refCooWilRoll = abbr("ref-coo-will", "reflex-coordination-willpower");
  val cooSomRoll = abbr("coo-som", "coordination-somatics");
  val wilCogRoll = abbr("wil-cog", "willpower-cognition");
  val psiDefense = text("psi-defense");
  val continuityTest = text("continutity-test");
  val resistTraumaDisorientation = text("resist-trauma-disorientation");
  val stressTest = text("stress-test");
  val healTrauma = text("heal-trauma");
  val resistWoundKnockdown = text("resist-wound-knockdown");
  val integrationTest = text("integration-test");
  val alienationTest = text("alienation-test");
  val rangedDefence = text("ranged-defence");
  val resistShock = text("resist-shock");
  val bruteStrength = text("brute-strength");
  val catchingObjects = text("catching-objects");
  val escapeArtist = text("escape-artist");
  val havingAnIdea = text("having-an-idea");
  val memoriseRecall = text("memorise-recall");
  val resistAsphyxiation = text("resist-asphyxiation");
  val resistBackupComplications = text("resist-backup-complications");
  val holdBreath = text("hold-breath");
  val jumpOnGrenade = text("jump-on-grenade");
  val resistBrainSeizure = text("resist-brain-serizure");
  val avoidLemon = text("avoid-lemon");
  val avoidVirusExposure = text("avoid-virus-exposure");
  val aptitudes = text("aptitudes");
  val cog = abbr("cog", "cognition");
  val coo = abbr("coo", "coordination");
  val int = abbr("int", "intuition");
  val ref = abbr("ref", "reflex");
  val sav = abbr("sav", "savy");
  val som = abbr("som", "somatics");
  val wil = abbr("wil", "willpower");
  val aptBase = text("apt-base");
  val aptMorphBonus = text("apt-morph-bonus");
  val aptMorphMax = text("apt-morph-max");
  val aptTemp = text("apt-temp");
  val aptTotal = text("apt-total");
  val stats = text("stats");
  val mox = abbr("mox", "moxie");
  val tt = abbr("tt", "trauma-threshold");
  val luc = abbr("luc", "lucidity");
  val ir = abbr("ir", "insanity-rating");
  val wt = abbr("wt", "wound-threshold");
  val dur = abbr("dur", "durability");
  val dr = abbr("dr", "death-rating");
  val init = abbr("init", "initiative");
  val spd = abbr("spd", "speed");
  val spdExtra = abbr("spd-extra", "speed-extra");
  val moa = abbr("moa", "mental-only-actions");
  val moaExtra = abbr("moa-extra", "mental-only-actions-extra");
  val db = abbr("db", "damage-bonus");
  val mentalHealth = text("mental-health");
  val stress = text("stress");
  val stressValue = abbr("sv", "stress-value");
  val trauma = text("trauma");
  val traumasIgnored = text("traumas-ignored");
  val physicalHealth = text("physical-health");
  val damage = text("damage");
  val wounds = text("wounds");
  val woundsIgnored = text("wounds-ignored");
  val armour = text("armour");
  val kinetic = text("kinetic");
  val energy = text("energy");

  // skills
  val skills = text("skills");
  val activeSkills = text("active-skills");
  val knowledgeSkills = text("knowledge-skills");
  val skillName = text("skill-name");
  val skillField = text("skill-field");
  val skillCategories = text("skill-categories");
  val skillSpecialisations = text("skill-specialisations");
  val skillLinkedAptitude = text("skill-linked-aptitude");
  val skillNoDefaulting = text("skill-no-defaulting");
  val skillRanks = text("skill-ranks");
  val skillMorphBonus = text("skill-morph-bonus");
  val skillTotal = text("skill-total");
  val skillsGenerate = text("generate-skills");
  val skillsGenerating = text("generating-skills");
  val skillsSortBy = text("skills-sort-by");
  val skillsSort = text("skills-sort");
  val skillCommands = text("skill-commands");
  val skillReloadPage = text("skill-reload-page");
  val skillNoSortManual = text("skill-no-sort-manual");
  // default skills
  val defaultSkills: Map[String, DataKey] = {
    val pregen = Skills.pregen.map { s =>
      val skillKey = "skill-" + s.name.toLowerCase().replaceAll(" ", "-");
      (s.name -> text(skillKey))
    }.toMap;
    // add special muse skills
    pregen + ("[Custom]" -> text("skill-custom"))
  };
  val defaultFields: Map[String, DataKey] = {
    val pregen = Skills.pregen.flatMap { s =>
      s.field match {
        case Some("???") => None
        case Some(fieldName) => {
          val fieldKey = "field-" + fieldName.toLowerCase().replaceAll(" ", "-");
          Some((fieldName -> text(fieldKey)))
        }
        case None => None
      }
    }.toMap;
    // add special muse skills
    pregen + ("Psychology" -> text("field-psychology")) +
      ("Electronics" -> text("field-electronics")) +
      ("Accounting" -> text("field-accounting"))
  };

  val core = text("core");

  // morphs
  val morph = text("morphs");
  val activeMorph = text("active-morph");
  val morphBank = text("morph-bank");
  val morphType = text("morph-type");
  val morphName = text("morph-name");
  val morphLabel = text("morph-label");
  val morphDescription = text("morph-description");
  val morphLocation = text("morph-location");
  val morphTraits = text("morph-traits");
  val morphImplants = text("morph-implants");
  val morphMobilitySystem = text("morph-mobility-system");
  val morphDurability = text("morph-durability");
  val morphArmour = text("morph-armour");
  val morphArmourEnergy = text("morph-armour-energy");
  val morphArmourKinetic = text("morph-armour-kinetic");
  val morphAptitudeBoni = text("morph-aptitude-boni");
  val morphAptitudeMax = text("morph-aptitude-max");
  val morphSkillBoni = text("morph-skill-boni");
  val morphVisibleAge = text("morph-visible-age");
  val morphVisibleGender = text("morph-visible-gender");
  val morphSpeed = abbr("morph-spd", "morph-speed");
  val morphMOA = abbr("morph-moa", "morph-mental-only-actions");
  val morphIniBonus = abbr("morph-ini-mod", "morph-initiative_modifier");
  val morphIgnoredWounds = text("morph-ignored-wounds");

  // gear
  val gear = text("gear");
  val gearFreeform = text("gear-freeform");
  val meleeWeapons = text("melee-weapons");
  val rangedWeapons = text("ranged-weapons");
  val armourWorn = text("armour-worn");
  val armourActiveTotal = text("armour-active-total");
  val layeringPenalty = text("armour-layering-penalty");
  val armourName = text("armour-name");
  val armourAccessory = text("armour-accessory");
  val equipment = text("equipment");
  val equipmentName = text("equipment-name");
  val equipmentDescription = text("equipment-description");
  val currency = text("currency");
  val cryptoCredits = text("crypto-credit");
  val cash = text("cash");
  val ap = abbr("ap", "armour-penetration");
  val orTotalAP = text("or-total-ap");
  val dmg = abbr("weapon-dmg", "weapon-damage");
  val weaponName = text("weapon-name");
  val weaponSkill = text("weapon-skill");
  val weaponSkillSearch = text("weapon-skill-search");
  val weaponDescription = text("weapon-description");
  val firingModes = text("fire-modes");
  val singleShot = abbr("ss", "single-shot");
  val semiAutomatic = abbr("sa", "semi-automatic");
  val burstFire = abbr("bf", "burst-fire");
  val fullAutomatic = abbr("fa", "full-automatic");
  val singleShotDescription = text("single-shot-description");
  val semiAutomaticDescription = text("semi-automatic-description");
  val burstFireDescription = text("burst-fire-description");
  val fullAutomaticDescription = text("full-automatic-description");
  val weaponRanges = text("weapon-ranges");
  val shortRange = abbr("s-range", "short-range");
  val mediumRange = abbr("m-range", "medium-range");
  val longRange = abbr("l-range", "long-range");
  val extremeRange = abbr("x-range", "extreme-range");
  val magazine = text("magazine");
  val size = text("size");
  val ammoType = text("ammo-type");
  val damageInflicts = text("damage-inflicts");
  val damageValue = abbr("dv", "damage-value");
  val concentrateFire = text("concentrate-fire");
  val software = text("software");
  val softwareQuality = text("software-quality");
  val qualityMod = text("quality-modifier");

  // identities and reputation
  val identities = text("identities");
  val identity = text("identity");
  val idDescription = text("id-description");
  val idCredits = text("id-credits");
  val idNotes = text("id-notes");
  val reputation = text("reputaion");
  val repScore = text("rep-score");
  val calledInFavours = text("called-in-favours");
  val lvl1 = abbr("lvl1", "level1");
  val lvl2 = abbr("lvl2", "level2");
  val lvl3 = abbr("lvl3", "level3");
  val lvl4 = abbr("lvl4", "level4");
  val lvl5 = abbr("lvl5", "level5");
  val atRep = abbr("atRep", "circleAlist");
  val cRep = abbr("cRep", "civicNet");
  val eRep = abbr("eRep", "ecoWave");
  val fRep = abbr("fRep", "fame");
  val gRep = abbr("gRep", "guanxi");
  val iRep = abbr("iRep", "theEye");
  val rRep = abbr("rRep", "rna");
  val uRep = abbr("uRep", "ultimateRep");
  val xRep = abbr("xRep", "exploreNet");

  // psi
  val async = text("async");
  val asyncTrait = text("async-trait");
  val psi = text("psi");
  val psiTempTime = text("psi-temp-time");
  val psiTempUnits = text("psi-temp-units");
  val psiSustained = text("psi-sustained");
  val psiCurrent = text("psi-current");
  val psiSustainedMod = text("psi-sustained-mod");
  val psiChi = text("psi-chi");
  val psiGamma = text("psi-gamma");
  val sleightName = text("sleight-name");
  val sleightDescription = text("sleight-description");
  val psiRange = text("psi-range");
  val psiAction = text("psi-action");
  val psiDuration = text("psi-duration");
  val strainMod = text("psi-strain-mod");
  val strain = text("strain");
  val psiSkill = text("psi-skill");
  val psychicStab = text("psychic-stab");
  val psychicDamage = text("psychic-damage");

  // muse
  val muse = text("muse");
  val museInfo = text("muse-info");
  val museName = text("muse-name");
  val museNotes = text("muse-notes");
  val museSkills = text("muse-skills");

  // derangements
  val derangements = text("derangements");
  val derangementDescription = text("derangement-description");
  val hours = abbr("hours-short", "hours-long");
  val derangementDuration = text("derangement-duration");
  val derangementSeverity = text("derangement-severity");
  val disorders = text("disorders");
  val disorderDescription = text("disorder-description");
  val disorderRemainingTreatment = text("disorder-remaining-treatment");

  // options
  val options = text("options");
  val sheetSettings = text("sheet-settings");
  val miscModifiers = text("misc-modifiers");
  val miscActionMod = text("misc-action-mod");
  val miscNotes = text("misc-notes");
  val miscPhysicalMod = text("misc-physical-mod");
  val miscInitiativeMod = text("misc-initiative-mod");
  val miscDurBonus = text("misc-dur-bonus");
  val chatOutput = text("chat-output");
  val usingAPIScript = text("using-api-script");
  val apiLookup = text("api-lookup");
  val apiText = text("api-text");

  // template
  val rollsfor = text("rolls-for");
  val rollSuccess = text("roll-success");
  val rollCritSuccess = text("roll-crit-success");
  val rollAutoSuccess = text("roll-auto-success");
  val rollFailure = text("roll-failure");
  val rollCritFailure = text("roll-crit-failure");
  val rollAutoFailure = text("roll-auto-failure");
  val mos = abbr("mos", "margin-of-success");
  val mof = abbr("mof", "margin-of-failure");

  val traitTypeOptions = {
    val opts = TraitType.values.map(v => (v -> v.toString)).toMap;
    enum[TraitType.type](TraitType.labelPrefix, opts)
  }
  val traitTypeOptionsShort = {
    val opts = TraitType.values.map(v => (v -> v.toString)).toMap;
    enum[TraitType.type](TraitType.labelShortPrefix, opts)
  }
  val skillCategoryOptions = {
    import Skills.SkillCategory;
    val opts = SkillCategory.values.map(v => (v -> v.toString)).toMap;
    enum[SkillCategory.type](SkillCategory.labelPrefix, opts)
  }
  val skillCategoryOptionsShort = {
    import Skills.SkillCategory;
    val opts = SkillCategory.values.map(v => (v -> v.toString)).toMap;
    enum[SkillCategory.type](SkillCategory.labelShortPrefix, opts)
  }
  val skillClassOptions = {
    import Skills.SkillClass;
    val opts = SkillClass.values.map(v => (v -> v.toString)).toMap;
    enum[SkillClass.type](SkillClass.labelPrefix, opts)
  }
  val skillSortOptions = {
    import Skills.SortBy;
    val opts = SortBy.values.map(v => (v -> v.toString)).toMap;
    enum[SortBy.type](SortBy.labelPrefix, opts)
  }
  val dmgType = {
    val opts = DamageType.values.map(v => (v -> v.toString)).toMap;
    enum[DamageType.type](DamageType.labelPrefix, opts)
  }
  val dmgTypeShort = {
    val opts = DamageType.values.map(v => (v -> v.toString)).toMap;
    enum[DamageType.type](DamageType.labelShortPrefix, opts)
  }
  val psiType = {
    val opts = PsiType.values.map(v => (v -> v.toString)).toMap;
    enum[PsiType.type](PsiType.labelPrefix, opts)
  }
  val psiTypeShort = {
    val opts = PsiType.values.map(v => (v -> v.toString)).toMap;
    enum[PsiType.type](PsiType.labelShortPrefix, opts)
  }
  val derangementSeverityOptions = {
    val opts = DerangementSeverity.values.map(v => (v -> v.toString)).toMap;
    enum[DerangementSeverity.type](DerangementSeverity.labelPrefix, opts)
  }
  val chatOutputOptions = {
    val opts = ChatOutput.values.map(v => (v -> v.toString)).toMap;
    enum[ChatOutput.type](ChatOutput.labelPrefix, opts)
  }
}
