package com.lkroll.ep.api

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import util.Try

object EPScripts extends APIScriptRoot {
  override def children: Seq[APIScript] = Seq(EPRollsScript);

  onReady {
    info(s"EPScripts v${BuildInfo.version} loaded!");
  }
}

object EPRollsScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPRollsCommand);
}

class EPRollsConf(args: Seq[String]) extends ScallopAPIConf(args) {
  import org.rogach.scallop.singleArgConverter;

  val output = opt[String]("output", descr = "who should receive the final output");
  val variables = trailArg[TemplateVars]("variables")(TemplateVars);
  verify();
}

object EPRollsCommand extends APICommand[EPRollsConf] {
  override def command = "eproll";
  override def options = (args) => new EPRollsConf(args);
  override def apply(config: EPRollsConf, ctx: ChatContext): Unit = {
    val target = if (config.output.isSupplied) {
      Chat.Whisper(config.output())
    } else {
      Chat.Default
    };
    sendChat(ctx.player, target.message("This is how I roll"));
    if (config.variables.isSupplied) {
      val vars = config.variables();
      ctx.rollTemplate match {
        case Some("ep-default") => {
          val replacedVars = vars.replaceInlineRolls(ctx.inlineRolls);
          val mofO = for {
            target <- replacedVars.lookup("test-target");
            roll <- replacedVars.lookup("test-roll");
            targetValueS <- target.stripValue;
            targetValue <- Try(targetValueS.toInt).toOption;
            rollValueS <- roll.stripValue;
            rollValue <- Try(rollValueS.toInt).toOption
          } yield {
            val diff = rollValue - targetValue;
            TemplateVar("test-mof", Some(s"[[${diff.toString()}]]"))
          };
          val augmentedVars = mofO match {
            case Some(mof) => mof :: replacedVars;
            case None      => replacedVars
          };
          val msg = s"&{template:ep-default} ${augmentedVars.render}";
          log(s"About to send: $msg");
          sendChat(ctx.player, target.message(msg));
        }
        case Some(t) => warn(s"template $t is not supported")
        case None    => warn("roll must use a roll template")
      }
    } else {
      warn("No variables supplied!");
    }
  }
}
