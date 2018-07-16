package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api.{ Character => Roll20Char, _ }
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import scalajs.js
import scalajs.js.JSON
import util.{ Try, Success, Failure }
import com.lkroll.ep.model.{ EPCharModel => epmodel, DamageType }

object CharTools extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(CharToolsCommand);
}

class CharToolsConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val damage = opt[Int]("damage", descr = "Add damage to characters, calculating wounds as well.");
  val stress = opt[Int]("stress", descr = "Add stress to characters, calculating traumas as well.");

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
      val stressField = char.attribute(epmodel.stress);
      val stress = config.stress();
      val curStress = stressField();
      val luc = char.attribute(epmodel.lucidity)();
      val ir = char.attribute(epmodel.insanityRating)();
      val tt = char.attribute(epmodel.traumaThreshold)();
      val stressTotal = stress + curStress;
      stressField <<= stressTotal;
      msg += s"<p>Took <em>$stress</em>SV.<p/>";
      if (stress >= tt) {
        val newTraumas = stress / tt;
        val traumaField = char.attribute(epmodel.trauma);
        val curTraumas = traumaField();
        traumaField <<= curTraumas + newTraumas;
        msg += s"<p>Took <em>$newTraumas</em> traumas. Add or upgrade an equivalent amount of derangements.</p>";
      }
      if (stressTotal >= ir) {
        msg += s"<p>Character is now permanently insane.</p>";
      } else if (stressTotal >= luc) {
        msg += s"<p>Character is now catatonic until stress is reduced below <em>${luc}</em>.</p>";
      }
    }
    if (config.damage.isSupplied) {
      val damageField = char.attribute(epmodel.damage);
      val incomingDamage = config.damage();
      val curDamage = damageField();
      val dur = char.attribute(epmodel.durability)();
      val dr = char.attribute(epmodel.deathRating)();
      val wt = char.attribute(epmodel.woundThreshold)();
      val damageApplied: Int = if (config.armour.isSupplied) {
        config.armour() match {
          case Some(DamageType.Kinetic) => {
            val kineticArmour = char.attribute(epmodel.armourKineticTotal)();
            Math.max(0, incomingDamage - kineticArmour)
          }
          case Some(DamageType.Energy) => {
            val energyArmour = char.attribute(epmodel.armourEnergyTotal)();
            Math.max(0, incomingDamage - energyArmour)
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
      if (damageApplied >= wt) {
        val newWounds = damageApplied / wt;
        val woundsField = char.attribute(epmodel.wounds);
        val curWounds = woundsField();
        woundsField <<= curWounds + newWounds;
        msg += s"<p>Took <em>$newWounds</em> wounds.</p>";
      }
      if (damageTotal >= dr) {
        msg += s"<p>The morph is now beyond repair/healing.</p>";
      } else if (damageTotal >= dur) {
        msg += s"<p>Character is now incapacitated/unconscious.</p>";
      }
    }

    sendChat(ctx.player, chatTarget.message(msg));
  }
}
