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
import com.lkroll.ep.model.{ EPTranslation => TranslationKeys, _ }

object EPTranslation extends SheetI18NDefaults {
  val keys = TranslationKeys;

  val version = keys.version <~ "v";
  val charName = keys.charName <~ "Character Name";
  val author = keys.author <~ "Author";
  val github = keys.github <~ "Github";
  val note = keys.note <~ "Note";

  val characterInfo = keys.characterInfo <~ "Character Info";
  val background = keys.background <~ "Background";
  val faction = keys.faction <~ "Faction";
  val genderId = keys.genderId <~ "Gender Identity";
  val actualAge = keys.actualAge <~ "Actual Age";
  val currentMoxie = keys.currentMoxie <~ "Current Moxie Points";
  val rezPoints = keys.rezPoints <~ ("REZ", "Rez Points");
  val motivations = keys.motivations <~ "Motivations";
  val effects = keys.effects <~ "Active Effects";
  val effectName = keys.effectName <~ "Name";
  val effectDuration = keys.effectDuration <~ "Duration";
  val effectDurationExample = keys.effectDurationExample <~ "e.g., 1min";
  val effectOnGame = keys.effectOnGame <~ "Game Effect";
  val effectOnGameExample = keys.effectOnGameExample <~ "e.g., +5 SOM, -5 COO";
  val effectDescription = keys.effectDescription <~ "Description";
  val showHideDescription = keys.showHideDescription <~ "Show/Hide Description";
  val traits = keys.traits <~ "Traits";
  val traitName = keys.traitName <~ "Name";
  val traitDescription = keys.traitDescription <~ "Description";
  val characterTraits = keys.characterTraits <~ "Character Traits";
  val specialRolls = keys.specialRolls <~ "Special Rolls";
  val successRoll = keys.successRoll <~ "Success Roll";
  val moxx10Roll = keys.moxx10Roll <~ ("MOX × 10", "Moxie times 10");
  val wilx2Roll = keys.wilx2Roll <~ ("WIL × 2", "Willpower doubled");
  val wilx3Roll = keys.wilx3Roll <~ ("WIL × 3", "Willpower tripled");
  val somx3Roll = keys.somx3Roll <~ ("SOM × 3", "Somatics tripled");
  val intx3Roll = keys.intx3Roll <~ ("INT × 3", "Intuition tripled");
  val cogx3Roll = keys.cogx3Roll <~ ("COG × 3", "Cognition tripled");
  val refx3Roll = keys.refx3Roll <~ ("REF × 3", "Reflex tripled");
  val frayHalvedRoll = keys.frayHalvedRoll <~ ("Fray/2", "Fray halved");
  val durEnergyRoll = keys.durEnergyRoll <~ ("DUR + Energy Armor", "Durability + Energy Armor");
  val refCoox2Roll = keys.refCoox2Roll <~ ("REF + COO × 2", "Reflex + Coordination doubled");
  val refCooWilRoll = keys.refCooWilRoll <~ ("REF + COO + WIL", "Reflex + Coordination + Willpower");
  val cooSomRoll = keys.cooSomRoll <~ ("COO + SOM", "Coordination + Somatics");
  val wilCogRoll = keys.wilCogRoll <~ ("WIL + COG", "Willpower + Cognition");
  val psiDefense = keys.psiDefense <~ "Psi Defense";
  val continuityTest = keys.continuityTest <~ "Continuity Test";
  val resistTraumaDisorientation = keys.resistTraumaDisorientation <~ "Resist Trauma Disorientation";
  val stressTest = keys.stressTest <~ "Stress Test";
  val healTrauma = keys.healTrauma <~ "Heal Trauma";
  val resistWoundKnockdown = keys.resistWoundKnockdown <~ "Resist Knockdown from Wound";
  val integrationTest = keys.integrationTest <~ "Integration Test";
  val alienationTest = keys.alienationTest <~ "Alienation Test";
  val rangedDefence = keys.rangedDefence <~ "Ranged Defence";
  val resistShock = keys.resistShock <~ "Resist Shock";
  val bruteStrength = keys.bruteStrength <~ "Brute Strength";
  val catchingObjects = keys.catchingObjects <~ "Catching Objects";
  val escapeArtist = keys.escapeArtist <~ "Escape Artist";
  val havingAnIdea = keys.havingAnIdea <~ "Having an Idea";
  val memoriseRecall = keys.memoriseRecall <~ "Memorise/Recall";
  val resistAsphyxiation = keys.resistAsphyxiation <~ "Roll with DUR (above) to resist asphyxiation.";
  val resistBackupComplications = keys.resistBackupComplications <~ "To resist complications during backup (EP p.270) roll with LUC (above).";
  val holdBreath = keys.holdBreath <~ "Suddenly Holding Breath (EP p.201)";
  val jumpOnGrenade = keys.jumpOnGrenade <~ "Jump on Grenade";
  val resistBrainSeizure = keys.resistBrainSeizure <~ "Resist Psi Brain Seizure (EP p.221)";
  val avoidLemon = keys.avoidLemon <~ "Avoid Lemon Breakdown (EP p.150)";
  val avoidVirusExposure = keys.avoidVirusExposure <~ "Avoid Virus Exposure";
  val aptitudes = keys.aptitudes <~ "Aptitudes";
  val cog = keys.cog <~ ("COG", "Cognition");
  val coo = keys.coo <~ ("COO", "Coordination");
  val int = keys.int <~ ("INT", "Intuition");
  val ref = keys.ref <~ ("REF", "Reflex");
  val sav = keys.sav <~ ("SAV", "Savy");
  val som = keys.som <~ ("SOM", "Somatics");
  val wil = keys.wil <~ ("WIL", "Willpower");
  val aptBase = keys.aptBase <~ "Base";
  val aptMorphBonus = keys.aptMorphBonus <~ "Morph Bonus";
  val aptMorphMax = keys.aptMorphMax <~ "Morph Max";
  val aptTemp = keys.aptTemp <~ "Temp";
  val aptTotal = keys.aptTotal <~ "Total";
  val stats = keys.stats <~ "Character Stats";
  val mox = keys.mox <~ ("MOX", "Moxie");
  val tt = keys.tt <~ ("TT", "Trauma Threshold");
  val luc = keys.luc <~ ("LUC", "Lucidity");
  val lucExtra = keys.lucExtra <~ ("LUC+", "Extra Lucidity");
  val ir = keys.ir <~ ("IR", "Insanity Rating");
  val wt = keys.wt <~ ("WT", "Wound Threshold");
  val dur = keys.dur <~ ("DUR", "Durability");
  val dr = keys.dr <~ ("DR", "Death Rating");
  val init = keys.init <~ ("INIT", "Initiative");
  val spd = keys.spd <~ ("SPD", "Speed");
  val spdExtra = keys.spdExtra <~ ("SPD+", "Extra Speed");
  val moa = keys.moa <~ ("MOA", "Mental Only Actions");
  val moaExtra = keys.moaExtra <~ ("MOA+", "Extra Mental Only Actions");
  val db = keys.db <~ ("DB", "Damage Bonus");
  val mentalHealth = keys.mentalHealth <~ "Mental Health";
  val stress = keys.stress <~ "Stress";
  val stressValue = keys.stressValue <~ ("SV", "Stress Value");
  val trauma = keys.trauma <~ "Trauma";
  val traumasIgnored = keys.traumasIgnored <~ "Ignored Traumas";
  val physicalHealth = keys.physicalHealth <~ "Physical Health";
  val damage = keys.damage <~ "Damage";
  val wounds = keys.wounds <~ "Wounds";
  val woundsIgnored = keys.woundsIgnored <~ "Ignored Wounds";
  val armour = keys.armour <~ "Armour";
  val kinetic = keys.kinetic <~ "Kinetic";
  val energy = keys.energy <~ "Energy";

  // skills
  val skills = keys.skills <~ "Skills";
  val activeSkills = keys.activeSkills <~ "Active Skills";
  val knowledgeSkills = keys.knowledgeSkills <~ "Knowledge Skills";
  val skillName = keys.skillName <~ "Name";
  val skillField = keys.skillField <~ "Field";
  val skillCategories = keys.skillCategories <~ "Categories";
  val skillSpecialisations = keys.skillSpecialisations <~ "Specialisations";
  val skillLinkedAptitude = keys.skillLinkedAptitude <~ "Aptitude";
  val skillNoDefaulting = keys.skillNoDefaulting <~ "No Defaulting";
  val skillRanks = keys.skillRanks <~ "Ranks";
  val skillMorphBonus = keys.skillMorphBonus <~ "Morph Bonus";
  val skillTotal = keys.skillTotal <~ "Total";
  val skillsGenerate = keys.skillsGenerate <~ "Generate Default Skills";
  val skillsGenerating = keys.skillsGenerating <~ "Generating Skills...";
  val skillsSortBy = keys.skillsSortBy <~ "Sort by";
  val skillsSort = keys.skillsSort <~ "Sort Now";
  val skillCommands = keys.skillCommands <~ "Commands";
  val skillReloadPage = keys.skillReloadPage <~ "Sorting result is only shown after reopening the sheet.";
  val skillNoSortManual = keys.skillNoSortManual <~ "Due to Roll20 limitations, manually added items can not be sorted automatically at this time.";
  // default skills
  val defaultSkills: List[DataLabel] = keys.defaultSkills.toList.map {
    case (k, v) => v <~ k
  };
  val defaultFields: List[DataLabel] = keys.defaultFields.toList.map {
    case (k, v) => v <~ k
  };

  val core = keys.core <~ "Core";

  // morphs
  val morph = keys.morph <~ "Morph";
  val activeMorph = keys.activeMorph <~ "Active Morph";
  val morphBank = keys.morphBank <~ "Morph Bank";
  val morphType = keys.morphType <~ "Type";
  val morphName = keys.morphName <~ "Model";
  val morphLabel = keys.morphLabel <~ "Label";
  val morphDescription = keys.morphDescription <~ "Description";
  val morphLocation = keys.morphLocation <~ "Storage Location";
  val morphTraits = keys.morphTraits <~ "Traits";
  val morphImplants = keys.morphImplants <~ "Implants/Enhancements";
  val morphMobilitySystem = keys.morphMobilitySystem <~ "Mobility System(s)";
  val morphDurability = keys.morphDurability <~ "Durability";
  val morphArmour = keys.morphArmour <~ "Armour";
  val morphArmourEnergy = keys.morphArmourEnergy <~ "Energy Armour";
  val morphArmourKinetic = keys.morphArmourKinetic <~ "Kinetic Armour";
  val morphAptitudeBoni = keys.morphAptitudeBoni <~ "Aptitude Boni";
  val morphAptitudeMax = keys.morphAptitudeMax <~ "Aptitude Max";
  val morphSkillBoni = keys.morphSkillBoni <~ "Skill Boni";
  val morphVisibleAge = keys.morphVisibleAge <~ "Visible Age";
  val morphVisibleGender = keys.morphVisibleGender <~ "Visible Gender";
  val morphSpeed = keys.morphSpeed <~ ("SPD", "Speed");
  val morphMOA = keys.morphMOA <~ ("MOA", "Mental Only Actions");
  val morphIniBonus = keys.morphIniBonus <~ ("INI MOD", "Initiative Modifier");
  val morphIgnoredWounds = keys.morphIgnoredWounds <~ "Ignored Wounds";

  // gear
  val gear = keys.gear <~ "Gear";
  val gearFreeform = keys.gearFreeform <~ "Freeform Gear";
  val meleeWeapons = keys.meleeWeapons <~ "Melee Weapons";
  val rangedWeapons = keys.rangedWeapons <~ "Ranged Weapons";
  val armourWorn = keys.armourWorn <~ "Armour Worn";
  val armourActiveTotal = keys.armourActiveTotal <~ "Active Bonus";
  val layeringPenalty = keys.layeringPenalty <~ "Layered Armour Penalty";
  val armourName = keys.armourName <~ "Name";
  val armourAccessory = keys.armourAccessory <~ "Armour Accessory";
  val equipment = keys.equipment <~ "Equipment";
  val equipmentName = keys.equipmentName <~ "Name";
  val equipmentDescription = keys.equipmentDescription <~ "Description";
  val currency = keys.currency <~ "Currency";
  val cryptoCredits = keys.cryptoCredits <~ "Crypto Credits";
  val cash = keys.cash <~ "Cash (Credit Chips)";
  val ap = keys.ap <~ ("AP", "Armour Penetration");
  val orTotalAP = keys.orTotalAP <~ "or ignore armour if critical success";
  val dmg = keys.dmg <~ ("Dmg", "Damage");
  val weaponName = keys.weaponName <~ "Weapon Name";
  val weaponSkill = keys.weaponSkill <~ "Skill";
  val weaponSkillSearch = keys.weaponSkillSearch <~ "Search Skill";
  val weaponDescription = keys.weaponDescription <~ "Description";
  val firingModes = keys.firingModes <~ "Firing Modes";
  val singleShot = keys.singleShot <~ ("SS", "Single Shot");
  val semiAutomatic = keys.semiAutomatic <~ ("SA", "Semi-Automatic");
  val burstFire = keys.burstFire <~ ("BF", "Burst Fire");
  val fullAutomatic = keys.fullAutomatic <~ ("FA", "Full Automatic");
  val singleShotDescription = keys.singleShotDescription <~ """
Single shot weapons may only be fired once per Complex Action.
""".trim;
  val semiAutomaticDescription = keys.semiAutomaticDescription <~ """
Semi-automatic weapons may be fired twice with the same Complex Action. Each shot is handled as a separate attack.
""".trim;
  val burstFireDescription = keys.burstFireDescription <~ """
Two bursts maybe fired with the same Complex Action. Each burst is handled as a separate attack. Bursts use up 3 shots worth of ammunition.
A burst may be shot against a single target (concentrated fire) or against two targets within one meter of each other. Against a single target, the attacker can choose either a +10 modifier to hit or increase the DV by +1d10.
""".trim;
  val fullAutomaticDescription = keys.fullAutomaticDescription <~ """
Only one full-auto attack may be made with each Complex Action. This attack may be made on a single target or against up to three separate targets within one meter of another. Against a single individual, the attacker can choose either a +30 modifier to hit or increase the DV by +3d10. Firing in full automatic mode uses up 10 shots.
""".trim;
  val weaponRanges = keys.weaponRanges <~ "Ranges";
  val shortRange = keys.shortRange <~ ("S", "Short");
  val mediumRange = keys.mediumRange <~ ("M", "Medium");
  val longRange = keys.longRange <~ ("L", "Long");
  val extremeRange = keys.extremeRange <~ ("X", "Extreme");
  val magazine = keys.magazine <~ "Magazine";
  val size = keys.size <~ "Size";
  val ammoType = keys.ammoType <~ "Ammo Type";
  val damageInflicts = keys.damageInflicts <~ "Inflicts";
  val damageValue = keys.damageValue <~ ("DV", "Damage Value");
  val concentrateFire = keys.concentrateFire <~ "Concentrate Fire";
  val software = keys.software <~ "Software";
  val softwareQuality = keys.softwareQuality <~ "Quality";
  val qualityMod = keys.qualityMod <~ "Quality Modifier";

  // identities
  val identities = keys.identities <~ "Identities";
  val identity = keys.identity <~ "Identity";
  val idDescription = keys.idDescription <~ "Description";
  val idCredits = keys.idCredits <~ "Credits";
  val idNotes = keys.idNotes <~ "Notes";
  val reputation = keys.reputation <~ "Reputation";
  val repScore = keys.repScore <~ "Score";
  val calledInFavours = keys.calledInFavours <~ "Called in Favours";
  val lvl1 = keys.lvl1 <~ ("Lvl 1", "Level 1");
  val lvl2 = keys.lvl2 <~ ("Lvl 2", "Level 2");
  val lvl3 = keys.lvl3 <~ ("Lvl 3", "Level 3");
  val lvl4 = keys.lvl4 <~ ("Lvl 4", "Level 4");
  val lvl5 = keys.lvl5 <~ ("Lvl 5", "Level 5");
  val atRep = keys.atRep <~ ("@-Rep", "The Circle-A List (Autonomists)");
  val cRep = keys.cRep <~ ("c-Rep", "CivicNet (Hypercorps)");
  val eRep = keys.eRep <~ ("e-Rep", "EcoWave (Ecologists)");
  val fRep = keys.fRep <~ ("f-Rep", "Fame (Media)");
  val gRep = keys.gRep <~ ("g-Rep", "Guanxi (Criminals)");
  val iRep = keys.iRep <~ ("i-Rep", "The Eye (Firewall)");
  val rRep = keys.rRep <~ ("r-Rep", "Research Network Associates (Scientists)");
  val uRep = keys.uRep <~ ("u-Rep", "Ultimate (Ultimates)");
  val xRep = keys.xRep <~ ("x-Rep", "ExploreNet (Gatecrashers)");

  // psi
  val async = keys.async <~ "Async";
  val asyncTrait = keys.asyncTrait <~ "Has Asyc trait";
  val psi = keys.psi <~ "Psi";
  val psiTempTime = keys.psiTempTime <~ "Temporary Duration";
  val psiTempUnits = keys.psiTempUnits <~ "units";
  val psiSustained = keys.psiSustained <~ "Sustained Sleights";
  val psiCurrent = keys.psiCurrent <~ "Currently Sustained";
  val psiSustainedMod = keys.psiSustainedMod <~ "Skill Penalty";
  val psiChi = keys.psiChi <~ "Psi-chi Sleights";
  val psiGamma = keys.psiGamma <~ "Psi-gamma Sleights";
  val sleightName = keys.sleightName <~ "Name";
  val sleightDescription = keys.sleightDescription <~ "Description";
  val psiRange = keys.psiRange <~ "Range";
  val psiAction = keys.psiAction <~ "Action";
  val psiDuration = keys.psiDuration <~ "Duration";
  val strainMod = keys.strainMod <~ "Strain Mod";
  val strain = keys.strain <~ "Strain";
  val psiSkill = keys.psiSkill <~ "Skill";
  val psychicStab = keys.psychicStab <~ "Psychic Stab Damage";
  val psychicDamage = keys.psychicDamage <~ "Psychic";

  // muse
  val muse = keys.muse <~ "Muse";
  val museInfo = keys.museInfo <~ "Muse Info";
  val museName = keys.museName <~ "Name";
  val museNotes = keys.museNotes <~ "Notes";
  val museSkills = keys.museSkills <~ "Muse Skills";

  // derangements
  val derangements = keys.derangements <~ "Derangements";
  val derangementDescription = keys.derangementDescription <~ "Description";
  val hours = keys.hours <~ ("h", "hours");
  val derangementDuration = keys.derangementDuration <~ "Duration";
  val derangementSeverity = keys.derangementSeverity <~ "Severity";
  val disorders = keys.disorders <~ "Disorders";
  val disorderDescription = keys.disorderDescription <~ "Description";
  val disorderRemainingTreatment = keys.disorderRemainingTreatment <~ "Remaining treatment time";

  // options
  val options = keys.options <~ "Options";
  val sheetSettings = keys.sheetSettings <~ "Sheet Settings";
  val miscModifiers = keys.miscModifiers <~ "Misc. Modifiers";
  val miscActionMod = keys.miscActionMod <~ "Misc. All Actions Modifier";
  val miscNotes = keys.miscNotes <~ "Misc. Notes";
  val miscPhysicalMod = keys.miscPhysicalMod <~ "Misc. Physical Modifier";
  val miscInitiativeMod = keys.miscInitiativeMod <~ "Misc. Initiative Modifier";
  val miscDurBonus = keys.miscDurBonus <~ "Misc. Durability Bonus";
  val chatOutput = keys.chatOutput <~ "Chat Output";
  val apiText = keys.apiText <~ "API Text Exchange";

  // template
  val rollsfor = keys.rollsfor <~ "rolls for";
  val rollSuccess = keys.rollSuccess <~ "Roll is a success";
  val rollCritSuccess = keys.rollCritSuccess <~ "Roll is a critical success";
  val rollAutoSuccess = keys.rollAutoSuccess <~ "Roll is an automatic success";
  val rollFailure = keys.rollFailure <~ "Roll is a failure";
  val rollCritFailure = keys.rollCritFailure <~ "Roll is a critical failure";
  val rollAutoFailure = keys.rollAutoFailure <~ "Roll is an automatic failure";
  val mos = keys.mos <~ ("MoS", "Margin of Success");
  val mof = keys.mof <~ ("MoF", "Margin of Failure");

  val traitTypeOptions = keys.traitTypeOptions <~ {
    case TraitType.Positive => "Positive"
    case TraitType.Neutral  => "Neutral"
    case TraitType.Negative => "Negative"
  };
  val traitTypeOptionsShort = keys.traitTypeOptionsShort <~ {
    case TraitType.Positive => "+"
    case TraitType.Neutral  => "\u25E6"
    case TraitType.Negative => "-"
  };
  val skillCategoryOptions = keys.skillCategoryOptions <~ {
    case Skills.SkillCategory.Combat    => "Combat"
    case Skills.SkillCategory.Mental    => "Mental"
    case Skills.SkillCategory.Physical  => "Physical"
    case Skills.SkillCategory.Psi       => "Psi & Mental" // always go together
    case Skills.SkillCategory.Social    => "Social"
    case Skills.SkillCategory.Technical => "Technical"
    case Skills.SkillCategory.Vehicle   => "Vehicle"
    case Skills.SkillCategory.NA        => "None"
  };
  val skillCategoryOptionsShort = keys.skillCategoryOptionsShort <~ {
    case Skills.SkillCategory.Combat    => "C"
    case Skills.SkillCategory.Mental    => "M"
    case Skills.SkillCategory.Physical  => "P"
    case Skills.SkillCategory.Psi       => "\u03A8&M"
    case Skills.SkillCategory.Social    => "S"
    case Skills.SkillCategory.Technical => "T"
    case Skills.SkillCategory.Vehicle   => "V"
    case Skills.SkillCategory.NA        => " "
  };
  val skillClassOptions = keys.skillClassOptions <~ {
    case Skills.SkillClass.Active    => "Active"
    case Skills.SkillClass.Knowledge => "Knowledge"
  };
  val skillSortOptions = keys.skillSortOptions <~ {
    case Skills.SortBy.None     => " - "
    case Skills.SortBy.Name     => "Name"
    case Skills.SortBy.Category => "Category"
    case Skills.SortBy.Aptitude => "Aptitude"
  };
  val dmgType = keys.dmgType <~ {
    case DamageType.Kinetic => "Kinetic"
    case DamageType.Energy  => "Energy"
    case DamageType.Untyped => "Untyped"
  };
  val dmgTypeShort = keys.dmgTypeShort <~ {
    case DamageType.Kinetic => "K"
    case DamageType.Energy  => "E"
    case DamageType.Untyped => ""
  };

  val psiType = keys.psiType <~ {
    case PsiType.Active  => "Active"
    case PsiType.Passive => "Passive"
  }
  val psiTypeShort = keys.psiTypeShort <~ {
    case PsiType.Active  => "A"
    case PsiType.Passive => "P"
  }
  val derangementSeverityOptions = keys.derangementSeverityOptions <~ {
    case DerangementSeverity.Minor    => "Minor"
    case DerangementSeverity.Moderate => "Moderate"
    case DerangementSeverity.Major    => "Major"
  }
  val chatOutputOptions = keys.chatOutputOptions <~ {
    case ChatOutput.Public       => "Public"
    case ChatOutput.GM           => "Whisper to GM"
    case ChatOutput.PublicScript => "Public via API"
    case ChatOutput.GMScript     => "Whisper to GM via API"
  }
  val usingAPIScript = keys.usingAPIScript <~ "Use API Script?";
  val apiLookup = keys.apiLookup <~ "Lookup";
  // ****************
  // ADD OPTIONS HERE
  // ****************
  lazy val allFullOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptions,
    Skills.SkillClass -> skillClassOptions,
    Skills.SortBy -> skillSortOptions,
    DamageType -> dmgType,
    PsiType -> psiType,
    DerangementSeverity -> derangementSeverityOptions,
    TraitType -> traitTypeOptions,
    ChatOutput -> chatOutputOptions);

  lazy val allShortOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptionsShort,
    DamageType -> dmgTypeShort,
    PsiType -> psiTypeShort,
    TraitType -> traitTypeOptionsShort);

}
