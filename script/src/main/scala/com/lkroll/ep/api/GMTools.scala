package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api.{ Character => Roll20Char, _ }
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import scalajs.js
import scalajs.js.JSON
import util.{ Try, Success, Failure }
import com.lkroll.ep.model.{ EPCharModel => epmodel, ActiveSkillSection, KnowledgeSkillSection }

object GMTools extends EPScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(GMToolsCommand);
}

class GMToolsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  version(s"${GMToolsCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner("Get an overview of the party's skills quickly.")
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");
  val bestMod = opt[Boolean]("best-mod", descr = "List selected characters sorted by best modifier.");

  val skillName = opt[String](
    "skill-name",
    descr = "Select skills by name for selected characters.")(
      ScallopUtils.singleArgSpacedConverter(identity));

  val characterNames = opt[List[String]](
    "char-name",
    descr = "Use character of this name instead of selected tokens. (Can be specified multiple times.)")(
      ScallopUtils.singleListArgConverter(identity));

  val charIds = opt[Boolean]("char-ids", descr = "Read character ids from trailing args.");

  val trailing = trailArg[List[String]]("trailing", hidden = true, required = false);

  dependsOnAll(bestMod, List(skillName));
  requireOne(bestMod);
  verify();
}

object GMToolsCommand extends EPCommand[GMToolsConf] {
  import CoreImplicits._;
  import scalatags.Text.all._;

  override def command = "epgmtools";
  override def options = (args) => new GMToolsConf(args);
  override def apply(config: GMToolsConf, ctx: ChatContext): Unit = {
    val skill = config.skillName();
    val res = if (config.characterNames.isSupplied || config.charIds()) {
      var chars = List.empty[Roll20Char];
      if (config.characterNames.isSupplied) {
        val charNames = config.characterNames();
        chars ++= charNames.flatMap { charName =>
          val r = Roll20Char.find(charName);
          if (r.isEmpty) {
            ctx.replyWarn(s"No character found for name $charName");
          }
          r
        }
      }
      if (config.charIds()) {
        val charIds = config.trailing();
        chars ++= charIds.flatMap { charId =>
          val r = Roll20Char.get(charId);
          if (r.isEmpty) {
            ctx.replyWarn(s"No character found for ID $charId");
          }
          r
        }
      }
      chars.map(skillTotals(_, skill))
    } else ctx.forChar(skillTotals(_, skill));
    if (config.bestMod()) {
      val bySkillName = res.flatMap {
        case (char, mods) => {
          mods.map(t => (char.name, t._1, t._2))
        }
      }.groupBy(_._2).map {
        case (skillName, l) => {
          val l2 = l.map(t => (t._1, t._3));
          (skillName -> l2)
        }
      };
      val organised = bySkillName.map(t => (t._1 -> t._2.sortBy(_._2)));
      val partials: Seq[Tag] = for ((skill, mods) <- organised.toSeq) yield div(
        h4(skill),
        p(ul(for ((char, total) <- mods) yield li(b(char), " has ", s"[[$total]]"))));
      val results: Tag = div(
        p("Best Modifiers (incl. wounds and traumas):"),
        partials);
      debug(s"Results: $results");
      ctx.reply("GM Tools", results);
    }
  }

  private def skillTotals(char: Roll20Char, skill: String): (Roll20Char, List[(String, Int)]) = {
    val woundMod = char.attribute(epmodel.woundMod)();
    val traumaMod = char.attribute(epmodel.traumaMod)();
    val aSkills = char.repeating(ActiveSkillSection.skillName).filter(_.current.equalsIgnoreCase(skill));
    val aSkillMods: List[(String, Int)] = aSkills.flatMap { skillNameAttr =>
      val Some(rowId) = skillNameAttr.getRowId;
      val field = char.repeatingAt(rowId)(ActiveSkillSection.field).map(_.apply).
        map(f => if (f.isEmpty) "" else s" ($f)").getOrElse("");
      val name = s"${skillNameAttr()}$field";
      val totalO = char.repeatingAt(rowId)(ActiveSkillSection.total).map(_.apply);
      totalO.map { total =>
        val totalModded = total - woundMod - traumaMod;
        (name -> totalModded)
      }
    };
    val kSkills = char.repeating(KnowledgeSkillSection.skillName).filter(_.current.equalsIgnoreCase(skill));
    val kSkillMods: List[(String, Int)] = kSkills.flatMap { skillNameAttr =>
      val Some(rowId) = skillNameAttr.getRowId;
      val field = char.repeatingAt(rowId)(KnowledgeSkillSection.field).map(_.apply).
        map(f => if (f.isEmpty) "" else s" ($f)").getOrElse("");
      val name = s"${skillNameAttr()}$field";
      val totalO = char.repeatingAt(rowId)(KnowledgeSkillSection.total).map(_.apply);
      totalO.map { total =>
        val totalModded = total - woundMod - traumaMod;
        (name -> totalModded)
      }
    };
    val skillMods: List[(String, Int)] = aSkillMods ++ kSkillMods;
    (char -> skillMods)
  }
}
