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
import com.lkroll.ep.api.{ asInfoTemplate, ScallopUtils, EPCommand, EPScripts }
import com.lkroll.ep.model.{ EPCharModel => epmodel }
import util.{ Try, Success, Failure }
import org.rogach.scallop.singleArgConverter

class EPCompendiumExportConf(_args: Seq[String]) extends ScallopAPIConf(_args) {
  version(s"${EPCompendiumImportCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner(s"""Export data from in the Eclipse Phase Compendium format.<br/>
All exports appear in the API Text Exchange field on the sheet represented by the selected token.
""");
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");

  val morph = opt[Boolean]("morph", descr = "Export the currently active morph instance.");

  requireOne(morph);
  verify();
}

object EPCompendiumExportCommand extends EPCommand[EPCompendiumExportConf] {
  import APIImplicits._;
  import Importable._;
  override def command = "epcompendium-export";
  override def options = (args) => new EPCompendiumExportConf(args);
  override def apply(config: EPCompendiumExportConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.reply("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      val updatedCharacters: List[(Character, List[String])] = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            val cached = char.cached();
            if (config.morph()) {
              val ex = MorphInstanceExport;
              Export.export(cached, ex) match {
                case Left(msg)  => Some(char -> List(s"Exported ${ex.updateLabel} to Sheet ($msg)"))
                case Right(msg) => Some(char -> List(s"Failed to export ${ex.updateLabel} to Sheet ($msg)"))
              }
            } else {
              None
            }
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }
      };
      val updates = updatedCharacters.map(_ match {
        case (char, ups) => char.name + ups.mkString("<ul><li>", "</li><li>", "</li></ul>")
      }).mkString("<ul><li>", "</li><li>", "</li></ul>");
      debug(s"Updates: $updates");
      ctx.reply(s"Updated Characters $updates");
    }
  }
}
