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
package com.lkroll.ep.api.compendium

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.roll20.api.conf._
import com.lkroll.ep.compendium._
import com.lkroll.ep.api.{ asInfoTemplate, ScallopUtils, EPScripts, SpecialRollsCommand }
import util.{ Try, Success, Failure }
import org.rogach.scallop.singleArgConverter

class EPCompendiumDataConf(_args: Seq[String]) extends ScallopAPIConf(_args) {
  version(s"${EPCompendiumDataCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Search and view data loaded into the Eclipse Phase Compendium.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");
  val search = opt[String]("search", descr = "Search for items with similar names to &lt;param&gt;.")(ScallopUtils.singleArgSpacedConverter(identity));
  val multiSearch = opt[Boolean]("multi-search", descr = "Search for a comma-separated list of items (after --), showing best matches only.");
  val nameOnly = opt[Boolean]("name-only", descr = "Only show names, not statblocks.");
  val rank = opt[Boolean]("rank", descr = "Rank all significant results, instead of showing highest one only.");
  val rankMax = opt[Int]("rank-max", descr = "Rank the top &lt;param&gt; significant results, instead of showing highest one only.");
  // items
  val weapon = opt[String]("weapon", descr = "Search for matches with &lt;param&gt; in weapons.")(ScallopUtils.singleArgSpacedConverter(identity));
  val ammo = opt[String]("ammo", descr = "Search for matches with &lt;param&gt; in ammo.")(ScallopUtils.singleArgSpacedConverter(identity));
  val withAmmo = opt[String]("with-ammo", descr = "Must be used together with --weapon. Modifies weapon to use the specified ammo. Ammo name must be exact!")(ScallopUtils.singleArgSpacedConverter(identity));
  val morphModel = opt[String]("morph-model", descr = "Search for matches with &lt;param&gt; in morph models.")(ScallopUtils.singleArgSpacedConverter(identity));
  val morph = opt[String]("morph", descr = "Search for matches with &lt;param&gt; in custom morphs.")(ScallopUtils.singleArgSpacedConverter(identity));
  val epTrait = opt[String]("trait", descr = "Search for matches with &lt;param&gt; in traits.")(ScallopUtils.singleArgSpacedConverter(identity));
  val derangement = opt[String]("derangement", descr = "Search for matches with &lt;param&gt; in derangements.")(ScallopUtils.singleArgSpacedConverter(identity));
  val disorder = opt[String]("disorder", descr = "Search for matches with &lt;param&gt; in disorders.")(ScallopUtils.singleArgSpacedConverter(identity));
  val armour = opt[String]("armour", descr = "Search for matches with &lt;param&gt; in armour.")(ScallopUtils.singleArgSpacedConverter(identity));
  val gear = opt[String]("gear", descr = "Search for matches with &lt;param&gt; in gear.")(ScallopUtils.singleArgSpacedConverter(identity));
  val software = opt[String]("software", descr = "Search for matches with &lt;param&gt; in software.")(ScallopUtils.singleArgSpacedConverter(identity));
  val substance = opt[String]("substance", descr = "Search for matches with &lt;param&gt; in substances.")(ScallopUtils.singleArgSpacedConverter(identity));
  val augmentation = opt[String]("augmentation", descr = "Search for matches with &lt;param&gt; in augmentations.")(ScallopUtils.singleArgSpacedConverter(identity));
  val armourMod = opt[String]("armour-mod", descr = "Search for matches with &lt;param&gt; in armour mods.")(ScallopUtils.singleArgSpacedConverter(identity));
  val weaponAccessory = opt[String]("weapon-accessory", descr = "Search for matches with &lt;param&gt; in weapon accessories.")(ScallopUtils.singleArgSpacedConverter(identity));
  val psiSleight = opt[String]("psi-sleight", descr = "Search for matches with &lt;param&gt; in psi sleights.")(ScallopUtils.singleArgSpacedConverter(identity));
  val skill = opt[String]("skill", descr = "Search for matches with &lt;param&gt; in skills.")(ScallopUtils.singleArgSpacedConverter(identity));

  val trailing = trailArg[List[String]]("trailing", hidden = true, required = false);

  dependsOnAny(nameOnly, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software, substance, augmentation, armourMod, weaponAccessory, psiSleight, skill));
  dependsOnAny(rank, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software, substance, augmentation, armourMod, weaponAccessory, psiSleight, skill));
  dependsOnAny(rankMax, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software, substance, augmentation, armourMod, weaponAccessory, psiSleight, skill));
  dependsOnAll(withAmmo, List(weapon));
  dependsOnAll(multiSearch, List(trailing));
  requireOne(search, multiSearch, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software, substance, augmentation, armourMod, weaponAccessory, psiSleight, skill);
  verify();

  def forDataType(dataType: String): Option[org.rogach.scallop.ScallopOption[String]] = {
    dataType match {
      case Augmentation.dataType    => Some(augmentation)
      case Armour.dataType          => Some(armour)
      case ArmourMod.dataType       => Some(armourMod)
      case Ammo.dataType            => Some(ammo)
      case Derangement.dataType     => Some(derangement)
      case Disorder.dataType        => Some(disorder)
      case EPTrait.dataType         => Some(epTrait)
      case Gear.dataType            => Some(gear)
      case MorphModel.dataType      => Some(morphModel)
      case MorphInstance.dataType   => Some(morph)
      case PsiSleight.dataType      => Some(psiSleight)
      case SkillDef.dataType        => Some(skill)
      case Software.dataType        => Some(software)
      case Substance.dataType       => Some(substance)
      case Weapon.dataType          => Some(weapon)
      case WeaponAccessory.dataType => Some(weaponAccessory)
      case _                        => None
    }
  }
}

object EPCompendiumDataCommand extends APICommand[EPCompendiumDataConf] {
  import APIImplicits._;
  val minConf = new EPCompendiumDataConf(Seq("--search", "nothing"));
  override def command = "epcompendium-data";
  override def options = (args) => new EPCompendiumDataConf(args);
  override def apply(config: EPCompendiumDataConf, ctx: ChatContext): Unit = {
    if (config.search.isSupplied) {
      val needle = config.search();
      ctx.reply(s"Searching for '$needle' in whole Compendium...");
      val results = EPCompendium.findAnything(needle);
      handleResults(results, config, ctx);
    } else if (config.multiSearch()) {
      val s = config.trailing().mkString(" ");
      if (s.isEmpty()) {
        ctx.reply(s"Ignoring empty search.");
      } else {
        ctx.reply(s"Searching for multiple items in whole Compendium...");
        //val cleanedS = s.split("""\R""").map(_.trim).filterNot(s => s.startsWith("=") || s.startsWith("#"));
        // val needles = cleanedS.map(_.split(",")).flatten.map(_.trim);
        val needles = s.split(",").map(_.trim);
        val results = needles.map { needle =>
          val r = EPCompendium.findAnything(needle.trim).headOption.map { bestResult =>
            val infoButton = this.invoke("?", argumentFrom(bestResult, config)).render;
            s"${bestResult.templateTitle} ${infoButton}"
          };
          (needle, r)
        };
        val pretty = results.map {
          case (needle, Some(r)) => s"<b>${needle}</b> &rarr; $r"
          case (needle, None)    => s"<b>${needle}</b> &rarr; 404 Not Found"
        }.mkString("<ul><li>", "</li><li>", "</li><ul>");
        debug(s"About to send '$pretty'");
        ctx.reply(pretty);
      }
    } else if (config.weapon.isSupplied && !config.withAmmo.isSupplied) {
      val needle = config.weapon();
      ctx.reply(s"Searching for '$needle' in weapons...");
      val results = EPCompendium.findWeapons(needle);
      handleResults(results, config, ctx);
    } else if (config.ammo.isSupplied) {
      val needle = config.ammo();
      ctx.reply(s"Searching for '$needle' in ammunitions...");
      val results = EPCompendium.findAmmos(needle);
      handleResults(results, config, ctx);
    } else if (config.weapon.isSupplied && config.withAmmo.isSupplied) {
      val needle = config.weapon();
      ctx.reply(s"Searching for '$needle' in weapons with ammo ${config.withAmmo()}...");
      val ammoO = EPCompendium.getAmmo(config.withAmmo());
      ammoO match {
        case Some(ammo) => {
          val weapons = EPCompendium.findWeapon(needle).toList;
          val results = weapons.flatMap(w => w.load(ammo) match {
            case Success(wwa) => Some(wwa)
            case Failure(e)   => ctx.reply(s"Error loading ${ammo.name} into ${w.name}: ${e.getMessage}"); None
          });
          handleResults(results, config, ctx);
        }
        case None => ctx.reply(s"No ammo found for name ${config.withAmmo()}")
      }
    } else if (config.morph.isSupplied) {
      val needle = config.morph();
      ctx.reply(s"Searching for '$needle' in morphs...");
      val results = EPCompendium.findMorphInstances(needle);
      handleResults(results, config, ctx);
    } else if (config.morphModel.isSupplied) {
      val needle = config.morphModel();
      ctx.reply(s"Searching for '$needle' in morph models...");
      val results = EPCompendium.findMorphModels(needle);
      handleResults(results, config, ctx);
    } else if (config.epTrait.isSupplied) {
      val needle = config.epTrait();
      ctx.reply(s"Searching for '$needle' in traits...");
      val results = EPCompendium.findTraits(needle);
      handleResults(results, config, ctx);
    } else if (config.derangement.isSupplied) {
      val needle = config.derangement();
      ctx.reply(s"Searching for '$needle' in derangements...");
      val results = EPCompendium.findDerangements(needle);
      handleResults(results, config, ctx);
    } else if (config.disorder.isSupplied) {
      val needle = config.disorder();
      ctx.reply(s"Searching for '$needle' in disorders...");
      val results = EPCompendium.findDisorders(needle);
      handleResults(results, config, ctx);
    } else if (config.armour.isSupplied) {
      val needle = config.armour();
      ctx.reply(s"Searching for '$needle' in armour...");
      val results = EPCompendium.findArmourItems(needle);
      handleResults(results, config, ctx);
    } else if (config.gear.isSupplied) {
      val needle = config.gear();
      ctx.reply(s"Searching for '$needle' in gear...");
      val results = EPCompendium.findGearItems(needle);
      handleResults(results, config, ctx);
    } else if (config.software.isSupplied) {
      val needle = config.software();
      ctx.reply(s"Searching for '$needle' in software...");
      val results = EPCompendium.findSoftwarePrograms(needle);
      handleResults(results, config, ctx);
    } else if (config.substance.isSupplied) {
      val needle = config.substance();
      ctx.reply(s"Searching for '$needle' in substances...");
      val results = EPCompendium.findSubstances(needle);
      handleResults(results, config, ctx);
    } else if (config.augmentation.isSupplied) {
      val needle = config.augmentation();
      ctx.reply(s"Searching for '$needle' in augmentations...");
      val results = EPCompendium.findAugmentations(needle);
      handleResults(results, config, ctx);
    } else if (config.armourMod.isSupplied) {
      val needle = config.armourMod();
      ctx.reply(s"Searching for '$needle' in armour mods...");
      val results = EPCompendium.findArmourMods(needle);
      handleResults(results, config, ctx);
    } else if (config.weaponAccessory.isSupplied) {
      val needle = config.weaponAccessory();
      ctx.reply(s"Searching for '$needle' in weapon accessories...");
      val results = EPCompendium.findWeaponAccessories(needle);
      handleResults(results, config, ctx);
    } else if (config.psiSleight.isSupplied) {
      val needle = config.psiSleight();
      ctx.reply(s"Searching for '$needle' in psi sleights...");
      val results = EPCompendium.findPsiSleights(needle);
      handleResults(results, config, ctx);
    } else if (config.skill.isSupplied) {
      val needle = config.skill();
      ctx.reply(s"Searching for '$needle' in skills...");
      val results = EPCompendium.findSkillDefs(needle);
      handleResults(results, config, ctx);
    } else {
      error(s"Unsupported options supplied: ${config.args}");
    }
  }

  private def handleResults(results: List[ChatRenderable], config: EPCompendiumDataConf, ctx: ChatContext): Unit = {
    if (results.isEmpty) {
      ctx.reply("No results found");
    } else {
      val displayResults = if (config.rank()) {
        results
      } else if (config.rankMax.isSupplied) {
        val n = Math.min(results.size, config.rankMax());
        results.take(n)
      } else { // head only
        List(results.head)
      };
      if (config.nameOnly()) {
        val pretty = displayResults.map(r => {
          val infoButton = this.invoke("?", argumentFrom(r, config)).render;
          val importButton = EPCompendiumImportCommand.invoke("⤺", importArgumentFrom(r)).render;
          s"${r.templateTitle} ${infoButton}&nbsp;${importButton}"
        }).
          mkString("<ul><li>", "</li><li>", "</li><ul>");
        debug(s"About to send '$pretty'");
        ctx.reply(pretty);
      } else {
        displayResults.foreach { r =>
          val importButton = EPCompendiumImportCommand.invoke("⤺", importArgumentFrom(r));
          val extras = extraButtons(r);
          val pretty = asInfoTemplate(r, importButton, extras);
          debug(s"About to send '$pretty'");
          ctx.reply(pretty);
        }
      }
    }
  }

  private def importArgumentFrom(r: ChatRenderable): List[OptionApplication] = {
    val config: EPCompendiumImportConf = new EPCompendiumImportConf(List.empty);
    val name = buttonSafeText(r.lookupName);
    r match {
      case _: Armour        => List(config.armour <<= name)
      case _: Derangement   => List(config.derangement <<= name)
      case _: Disorder      => List(config.disorder <<= name)
      case _: EPTrait       => List(config.egoTrait <<= name)
      case _: Gear          => List(config.gear <<= name)
      case _: MorphModel    => List(config.morphModel <<= name)
      case _: MorphInstance => List(config.morph <<= name)
      case _: Software      => List(config.software <<= name)
      case _: Weapon        => List(config.weapon <<= name)
      case wwa: WeaponWithAmmo => {
        val wname = buttonSafeText(wwa.weapon.lookupName);
        val aname = buttonSafeText(wwa.ammo.lookupName);
        List(config.weapon <<= wname, config.withAmmo <<= aname)
      }
      case _: Substance  => List(config.substance <<= name)
      case _: SkillDef   => List(config.skill <<= name)
      case _: PsiSleight => List(config.psiSleight <<= name)
      case _             => List.empty
    }
  }

  private def argumentFrom(r: ChatRenderable, config: EPCompendiumDataConf): List[OptionApplication] = {
    val name = buttonSafeText(r.lookupName);
    r match {
      case _: Augmentation    => List(config.augmentation <<= name)
      case _: Ammo            => List(config.ammo <<= name)
      case _: Armour          => List(config.armour <<= name)
      case _: ArmourMod       => List(config.armourMod <<= name)
      case _: Derangement     => List(config.derangement <<= name)
      case _: Disorder        => List(config.disorder <<= name)
      case _: EPTrait         => List(config.epTrait <<= name)
      case _: Gear            => List(config.gear <<= name)
      case _: MorphModel      => List(config.morphModel <<= name)
      case _: MorphInstance   => List(config.morph <<= name)
      case _: PsiSleight      => List(config.psiSleight <<= name)
      case _: SkillDef        => List(config.skill <<= name)
      case _: Software        => List(config.software <<= name)
      case _: Substance       => List(config.substance <<= name)
      case _: Weapon          => List(config.weapon <<= name)
      case _: WeaponAccessory => List(config.weaponAccessory <<= name)
      case _                  => List.empty
    }
  }

  private def buttonSafeText(s: String): String = s.replaceAll("\\)", "&#41;");

  private def extraButtons(r: ChatRenderable): List[(String, APIButton)] = {
    r match {
      case w: Weapon => {
        val c = SpecialRollsCommand.minConf;
        val dmg = w.templateKV("Damage");
        val dmgButton = SpecialRollsCommand.invoke(dmg, List(
          c.damage <<= true,
          c.damageDice <<= w.damage.dmgD10,
          c.damageDiv <<= w.damage.dmgDiv,
          c.damageConst <<= w.damage.dmgConst,
          c.damageType <<= w.damage.dmgType.label,
          c.ap <<= w.ap,
          c.label <<= buttonSafeText(w.templateTitle)));
        val skill = w.templateKV("Skill");
        val skillButton = SpecialRollsCommand.invoke(skill, List(
          c.success <<= true,
          c.target.name <<= s"?{$skill}",
          c.label <<= buttonSafeText(w.templateTitle),
          c.sublabel <<= buttonSafeText(skill)));
        List("Damage" -> dmgButton, "Skill" -> skillButton)
      }
      case a: Augmentation => {
        val conf = this.minConf;
        val prefix = "Related Item ";
        a.related.zipWithIndex.map {
          case (ref, i) => {
            conf.forDataType(ref.dataType).map { opt =>
              val button = EPCompendiumDataCommand.invoke(
                ref.name, List(opt <<= buttonSafeText(ref.name)));
              ((prefix + i) -> button)
            }
          }
        }.flatten
      }
      case w: WeaponWithAmmo => {
        val c = SpecialRollsCommand.minConf;
        val dmg = w.templateKV("Damage");
        val dmgButton = SpecialRollsCommand.invoke(dmg, List(
          c.damage <<= true,
          c.damageDice <<= w.damage.dmgD10,
          c.damageDiv <<= w.damage.dmgDiv,
          c.damageConst <<= w.damage.dmgConst,
          c.damageType <<= w.damage.dmgType.label,
          c.ap <<= w.ap,
          c.label <<= buttonSafeText(w.templateTitle)));
        val skill = w.templateKV("Skill");
        val skillButton = SpecialRollsCommand.invoke(skill, List(
          c.success <<= true,
          c.target.name <<= s"?{$skill}",
          c.label <<= buttonSafeText(w.templateTitle),
          c.sublabel <<= buttonSafeText(skill)));
        List("Damage" -> dmgButton, "Skill" -> skillButton)
      }
      case s: SkillDef => {
        val c = SpecialRollsCommand.minConf;
        val skill = s.name;
        val skillButton = SpecialRollsCommand.invoke("Success Roll", List(
          c.success <<= true,
          c.target.name <<= s"?{$skill}",
          c.label <<= buttonSafeText(s.templateTitle),
          c.sublabel <<= buttonSafeText(s.templateSubTitle)));
        List("Commands" -> skillButton)
      }
      case _ => List.empty
    }
  }
}
