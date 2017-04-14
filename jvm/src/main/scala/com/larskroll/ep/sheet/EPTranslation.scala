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

  val allFullOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptions,
    Skills.SkillClass -> skillClassOptions,
    Skills.SortBy -> skillSortOptions);

  val allShortOptions = Map[Enumeration, OptionLabel](
    Skills.SkillCategory -> skillCategoryOptionsShort);
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
  val morphDescription = text("morph-description", "Description");
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
