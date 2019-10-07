package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api.{Character => Roll20Char, _}
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import scalajs.js
import scalajs.js.JSON
import util.{Failure, Success, Try}
import com.lkroll.ep.model.{EPCharModel => epmodel, DamageType, MorphType}

object CharTools extends EPScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(CharToolsCommand);
}

class CharToolsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  version(s"${CharToolsCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Apply DV and SV easily.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");

  val damage = opt[Int]("damage", descr = "Add damage to characters, calculating wounds as well.");
  val stress = opt[Int]("stress", descr = "Add stress to characters, calculating traumas as well.");

  val armourPenetration =
    opt[Int]("ap",
             descr =
               "Subtract Armour Penetration from selected armour. Will always be interpreted as a negative number.",
             default = Some(0)).map(i => Math.abs(i));
  val armour = opt[String](
    "armour",
    descr =
      s"Reduce damage by character's armor value of the given type. Options are ${DamageType.values.mkString(", ")} or None"
  ).map(s => if (s.equalsIgnoreCase("None")) None else Some(DamageType.withName(s)));

  val characterName = opt[String]("char-name", descr = "Use character of this name instead of the selected token.")(
    ScallopUtils.singleArgSpacedConverter(identity)
  );

  val output = opt[String]("output", descr = "Who should receive the final output. Default is public.");

  dependsOnAll(armour, List(damage));
  dependsOnAll(armourPenetration, List(damage, armour));
  requireOne(damage, stress);
  verify();
}

object CharToolsCommand extends EPCommand[CharToolsConf] {
  import APIImplicits._;
  import scalatags.Text.all._;

  lazy val minConf = new CharToolsConf(Seq("--damage", "0"));

  override def command = "epchar";
  override def options = (args) => new CharToolsConf(args);
  override def apply(config: CharToolsConf, ctx: ChatContext): Unit = {
    if (config.characterName.isSupplied) {
      val charName = config.characterName();
      val chars = Roll20Char.find(charName);
      if (chars.isEmpty) {
        ctx.replyWarn(s"No character found for name $charName");
      } else if (chars.size > 1) {
        ctx.replyWarn(s"Multiple characters found for name $charName. Picking first.");
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
    val msg: Tag = if (config.stress.isSupplied) {
      div(applyStress(config, ctx, char).reverse)
    } else if (config.damage.isSupplied) {
      div(applyDamage(config, ctx, char).reverse)
    } else {
      p(cls := "sheet-inline-error", "Invalid invocation type!")
    };

    sendChat(ctx.player, title = Some(char.name), chatTarget.htmlMessage(msg));
  }

  def applyStress(config: CharToolsConf, ctx: ChatContext, char: Roll20Char): Seq[Tag] = {
    var msg: List[Tag] = Nil;
    try {
      val stressField = char.attribute(epmodel.stress);
      val stress = config.stress();
      val curStress = stressField.getOrDefault;
      val luc = char.attribute(epmodel.lucidity).getOrDefault;
      val ir = char.attribute(epmodel.insanityRating).getOrDefault;
      val tt = char.attribute(epmodel.traumaThreshold).getOrDefault;
      val stressTotal = stress + curStress;
      stressField <<= stressTotal;
      msg ::= p("Took ", em(stress), "SV.");

      val (traumas, newTraumas) = if (stress >= tt) {
        val newTraumas = stress / tt;
        val traumaField = char.attribute(epmodel.trauma);
        val curTraumas = traumaField.getOrDefault;
        val newCurTraumas = curTraumas + newTraumas;
        traumaField.setWithWorker(newCurTraumas);
        msg ::= p("Took ", em(newTraumas), " traumas.");
        (newCurTraumas, newTraumas)
      } else (0, 0);

      if (stressTotal >= ir) {
        msg ::= p("Is now permanently insane.");
      } else if (stressTotal >= luc) {
        msg ::= p("Is now catatonic until stress is reduced below ", em(luc), ".");
      } else {
        if (newTraumas >= 1) {
          msg ::= p("Must ",
                    char.rollButton("Resist Disorientation", "willx3-roll"),
                    " or be forced to expend a Complex Action to regain their wits.");
          if (traumas >= 4) {
            msg ::= p(
              "Acquires ",
              em(newTraumas),
              " new minor derangements or upgrades an equivalent number of existing derangements, potentially to a disorder."
            );
          } else {
            msg ::= p("Acquires ",
                      em(newTraumas),
                      " new minor derangements or upgrades an equivalent number of existing derangements.");
          }
        }
      }
    } catch {
      case e: java.util.NoSuchElementException => {
        error(e);
        msg ::= p(cls := "sheet-inline-error", "An error occurred accessing a field!")
      }
    }
    msg
  }

  def applyDamage(config: CharToolsConf, ctx: ChatContext, char: Roll20Char): Seq[Tag] = {
    var msg: List[Tag] = Nil;
    try {
      val damageField = char.attribute(epmodel.damage);
      val incomingDamage = config.damage();
      val curDamage = damageField.getOrDefault;
      val dur = char.attribute(epmodel.durability).getOrDefault;
      val dr = char.attribute(epmodel.deathRating).getOrDefault;
      val wt = char.attribute(epmodel.woundThreshold).getOrDefault;
      val damageApplied: Int = if (config.armour.isSupplied) {
        val ap = config.armourPenetration();
        config.armour() match {
          case Some(DamageType.Kinetic) => {
            val kineticArmour = char.attribute(epmodel.armourKineticTotal).getOrDefault;
            val armourApplied = Math.max(kineticArmour - ap, 0);
            Math.max(0, incomingDamage - armourApplied)
          }
          case Some(DamageType.Energy) => {
            val energyArmour = char.attribute(epmodel.armourEnergyTotal).getOrDefault;
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
      msg ::= p("Took ", em(damageApplied), "DV.");
      val (wounds, newWounds) = if (damageApplied >= wt) {
        val newWounds = damageApplied / wt;
        val woundsField = char.attribute(epmodel.wounds);
        val curWounds = woundsField();
        val newCurWounds = curWounds + newWounds;
        woundsField.setWithWorker(newCurWounds);
        msg ::= p("Took ", em(newWounds), " wounds.");
        (newCurWounds, newWounds)
      } else (0, 0);
      val isBiomorph = MorphType.withName(char.attribute(epmodel.morphType)()) match {
        case MorphType.Biomorph | MorphType.Pod => true
        case _                                  => false
      };
      if (damageTotal >= dr) {
        if (isBiomorph) {
          msg ::= p("Morph is now beyond healing.");
        } else {
          msg ::= p("Morph is now beyond repair.");
        }
      } else if (damageTotal >= dur && wounds > 0 && isBiomorph) {
        msg ::= p("Is now unconscious and bleeding out (1DV per Action Turn).");
      } else if (damageTotal >= dur) {
        if (isBiomorph) {
          msg ::= p("Is now unconscious.");
        } else {
          msg ::= p("Is now incapacitated.");
        }
      } else {
        if (newWounds == 1) {
          msg ::= p("Must ", char.rollButton("Resist Knockdown", "somx3-roll"), " or fall prone.");
        } else if (newWounds >= 2) {
          msg ::= p("Must ",
                    char.rollButton("Resist Unconsciousness", "somx3-roll"),
                    " or pass out until awoken or healed.");
        }
      }
    } catch {
      case e: java.util.NoSuchElementException => {
        error(e);
        msg ::= p(cls := "sheet-inline-error", "An error occurred accessing a field!")
      }
    }
    msg
  }
}
