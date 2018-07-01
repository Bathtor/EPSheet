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
  val nameOnly = opt[Boolean]("name-only", descr = "Only show names, not statblocks.");
  val rank = opt[Boolean]("rank", descr = "Rank all significant results, instead of showing highest one only.");
  val rankMax = opt[Int]("rank-max", descr = "Rank the top &lt;param&gt; significant results, instead of showing highest one only.");
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

  dependsOnAny(nameOnly, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software));
  dependsOnAny(rank, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software));
  dependsOnAny(rankMax, List(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software));
  dependsOnAll(withAmmo, List(weapon));
  requireOne(search, weapon, ammo, morph, morphModel, epTrait, derangement, disorder, armour, gear, software);
  verify();
}

object EPCompendiumDataCommand extends APICommand[EPCompendiumDataConf] {
  import APIImplicits._;
  override def command = "epcompendium-data";
  override def options = (args) => new EPCompendiumDataConf(args);
  override def apply(config: EPCompendiumDataConf, ctx: ChatContext): Unit = {
    if (config.search.isSupplied) {
      val needle = config.search();
      ctx.reply(s"Searching for '$needle' in whole Compendium...");
      val results = EPCompendium.findAnything(needle);
      handleResults(results, config, ctx);
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
        val wname = wwa.weapon.lookupName.replace(")", "&#41;");
        val aname = wwa.ammo.lookupName.replace(")", "&#41;");
        List(config.weapon <<= wname, config.withAmmo <<= aname)
      }
      case _ => List.empty
    }
  }

  private def argumentFrom(r: ChatRenderable, config: EPCompendiumDataConf): List[OptionApplication] = {
    val name = buttonSafeText(r.lookupName);
    r match {
      case _: Ammo          => List(config.ammo <<= name)
      case _: Armour        => List(config.armour <<= name)
      case _: Derangement   => List(config.derangement <<= name)
      case _: Disorder      => List(config.disorder <<= name)
      case _: EPTrait       => List(config.epTrait <<= name)
      case _: Gear          => List(config.gear <<= name)
      case _: MorphModel    => List(config.morphModel <<= name)
      case _: MorphInstance => List(config.morph <<= name)
      case _: Software      => List(config.software <<= name)
      case _: Weapon        => List(config.weapon <<= name)
      case _                => List.empty
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
          c.damageDice <<= w.dmgD10,
          c.damageDiv <<= w.dmgDiv,
          c.damageConst <<= w.dmgConst,
          c.damageType <<= w.dmgType.label,
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
      case w: WeaponWithAmmo => {
        val c = SpecialRollsCommand.minConf;
        val dmg = w.templateKV("Damage");
        val dmgButton = SpecialRollsCommand.invoke(dmg, List(
          c.damage <<= true,
          c.damageDice <<= w.dmgD10,
          c.damageDiv <<= w.dmgDiv,
          c.damageConst <<= w.dmgConst,
          c.damageType <<= w.dmgType.label,
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
      case _ => List.empty
    }
  }
}
