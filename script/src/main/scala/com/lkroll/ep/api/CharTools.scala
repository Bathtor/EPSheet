package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api.{ Character => Roll20Char, _ }
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import scalajs.js
import scalajs.js.JSON
import util.{ Try, Success, Failure }
import com.lkroll.ep.model.{ EPCharModel => epmodel, DamageType, MorphType }

object CharTools extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(CharToolsCommand);
}

class CharToolsConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val damage = opt[Int]("damage", descr = "Add damage to characters, calculating wounds as well.");
  val stress = opt[Int]("stress", descr = "Add stress to characters, calculating traumas as well.");

  val armourPenetration = opt[Int]("ap", descr = "Subtract Armour Penetration from selected armour.", default = Some(0));
  val armour = opt[String](
    "armour",
    descr = s"Reduce damage by character's armor value of the given type. Options are ${DamageType.values.mkString(", ")} or None").
    map(s => if (s.equalsIgnoreCase("None")) None else Some(DamageType.withName(s)));

  val characterName = opt[String](
    "char-name",
    descr = "Use character of this name instead of the selected token.")(
      ScallopUtils.singleArgSpacedConverter(identity));

  val output = opt[String]("output", descr = "Who should receive the final output. Default is public.");

  dependsOnAll(armour, List(damage));
  dependsOnAll(armourPenetration, List(damage, armour));
  requireOne(damage, stress);
  verify();
}

object CharToolsCommand extends APICommand[CharToolsConf] {
  import APIImplicits._;
  override def command = "epchar";
  override def options = (args) => new CharToolsConf(args);
  override def apply(config: CharToolsConf, ctx: ChatContext): Unit = {
    if (config.characterName.isSupplied) {
      val charName = config.characterName();
      val chars = Roll20Char.find(charName);
      if (chars.isEmpty) {
        ctx.reply(s"No character found for name $charName");
      } else if (chars.size > 1) {
        ctx.reply(s"Multiple characters found for name $charName. Picking first.");
        applyToChar(config, ctx, chars.head);
      } else {
        applyToChar(config, ctx, chars.head);
      }
    } else {
      ctx.forChar(applyToChar(config, ctx, _));
    }
  }

  def applyToChar(config: CharToolsConf, ctx: ChatContext, char: Roll20Char): Unit = {
    val chatTarget = if (config.output.isSupplied) {
      Chat.Whisper(config.output())
    } else {
      Chat.Default
    };
    var msg = s"<h3>${char.name}</h3>";
    if (config.stress.isSupplied) {
      msg += applyStress(config, ctx, char);
    }
    if (config.damage.isSupplied) {
      msg += applyDamage(config, ctx, char);
    }

    sendChat(ctx.player, chatTarget.message(msg));
  }

  def applyStress(config: CharToolsConf, ctx: ChatContext, char: Roll20Char): String = {
    var msg = "";
    val stressField = char.attribute(epmodel.stress);
    val stress = config.stress();
    val curStress = stressField();
    val luc = char.attribute(epmodel.lucidity)();
    val ir = char.attribute(epmodel.insanityRating)();
    val tt = char.attribute(epmodel.traumaThreshold)();
    val stressTotal = stress + curStress;
    stressField <<= stressTotal;
    msg += s"<p>Took <em>$stress</em>SV.<p/>";

    val (traumas, newTraumas) = if (stress >= tt) {
      val newTraumas = stress / tt;
      val traumaField = char.attribute(epmodel.trauma);
      val curTraumas = traumaField();
      val newCurTraumas = curTraumas + newTraumas;
      traumaField.setWithWorker(newCurTraumas);
      msg += s"<p>Took <em>$newTraumas</em> traumas.</p>";
      (newCurTraumas, newTraumas)
    } else (0, 0);

    if (stressTotal >= ir) {
      msg += s"<p>Is now permanently insane.</p>";
    } else if (stressTotal >= luc) {
      msg += s"<p>Is now catatonic until stress is reduced below <em>${luc}</em>.</p>";
    } else {
      if (newTraumas >= 1) {
        msg += s"<p>Must ${char.rollButton("Resist Disorientation", "willx3-roll")} or be forced to expend a Complex Action to regain their wits.</p>";
        if (traumas >= 4) {
          msg += s"<p>Acquires <em>${newTraumas}</em> new minor derangements or upgrades an equivalent number of existing derangements, potentially to a disorder.</p>";
        } else {
          msg += s"<p>Acquires <em>${newTraumas}</em> new minor derangements or upgrades an equivalent number of existing derangements.</p>";
        }
      }
    }
    msg
  }

  def applyDamage(config: CharToolsConf, ctx: ChatContext, char: Roll20Char): String = {
    var msg = "";
    val damageField = char.attribute(epmodel.damage);
    val incomingDamage = config.damage();
    val curDamage = damageField();
    val dur = char.attribute(epmodel.durability)();
    val dr = char.attribute(epmodel.deathRating)();
    val wt = char.attribute(epmodel.woundThreshold)();
    val damageApplied: Int = if (config.armour.isSupplied) {
      val ap = config.armourPenetration();
      config.armour() match {
        case Some(DamageType.Kinetic) => {
          val kineticArmour = char.attribute(epmodel.armourKineticTotal)();
          val armourApplied = Math.max(kineticArmour - ap, 0);
          Math.max(0, incomingDamage - armourApplied)
        }
        case Some(DamageType.Energy) => {
          val energyArmour = char.attribute(epmodel.armourEnergyTotal)();
          val armourApplied = Math.max(energyArmour - ap, 0);
          Math.max(0, incomingDamage - armourApplied)
        }
        case Some(DamageType.Untyped) | None => incomingDamage
        case _                               => ??? // should be exhaustive
      }
    } else {
      incomingDamage
    };
    val damageTotal = curDamage + damageApplied;
    damageField <<= damageTotal;
    msg += s"<p>Took <em>$damageApplied</em>DV.<p/>";
    val (wounds, newWounds) = if (damageApplied >= wt) {
      val newWounds = damageApplied / wt;
      val woundsField = char.attribute(epmodel.wounds);
      val curWounds = woundsField();
      val newCurWounds = curWounds + newWounds;
      woundsField.setWithWorker(newCurWounds);
      msg += s"<p>Took <em>$newWounds</em> wounds.</p>";
      (newCurWounds, newWounds)
    } else (0, 0);
    val isBiomorph = MorphType.withName(char.attribute(epmodel.morphType)()) match {
      case MorphType.Biomorph | MorphType.Pod => true
      case _                                  => false
    };
    if (damageTotal >= dr) {
      if (isBiomorph) {
        msg += s"<p>Morph is now beyond healing.</p>";
      } else {
        msg += s"<p>Morph is now beyond repair.</p>";
      }
    } else if (damageTotal >= dur && wounds > 0 && isBiomorph) {
      msg += s"<p>Is now unconscious and bleeding out (1DV per Action Turn).</p>";
    } else if (damageTotal >= dur) {
      if (isBiomorph) {
        msg += s"<p>Is now unconscious.</p>";
      } else {
        msg += s"<p>Is now incapacitated.</p>";
      }
    } else {
      if (newWounds == 1) {
        msg += s"<p>Must ${char.rollButton("Resist Knockdown", "somx3-roll")} or fall prone.</p>";
      } else if (newWounds >= 2) {
        msg += s"<p>Must ${char.rollButton("Resist Unconsciousness", "somx3-roll")} or pass out until awoken or healed.</p>";
      }
    }
    msg
  }
}
