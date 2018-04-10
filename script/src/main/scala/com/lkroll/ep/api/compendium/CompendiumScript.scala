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
import com.lkroll.ep.api.{ asInfoTemplate, ScallopUtils }

object CompendiumScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPCompendiumImportCommand, EPCompendiumDataCommand);
}

class EPCompendiumImportConf(_args: Seq[String]) extends ScallopAPIConf(_args) {
  footer(s"\nAll names must be exact. Use '!${EPCompendiumDataCommand.command} --search' to find available names.");

  val weapon = opt[List[String]]("weapon", descr = "Import a weapon with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  requireOne(weapon);
  verify();
}

object EPCompendiumImportCommand extends APICommand[EPCompendiumImportConf] {
  import APIImplicits._;
  import Importable._;
  override def command = "epcompendium-import";
  override def options = (args) => new EPCompendiumImportConf(args);
  override def apply(config: EPCompendiumImportConf, ctx: ChatContext): Unit = {
    val graphicTokens = ctx.selected;
    if (graphicTokens.isEmpty) {
      ctx.reply("No tokens selected. Nothing to do...");
    } else {
      val tokens = graphicTokens.flatMap {
        case t: Token => Some(t)
        case c        => debug(s"Ignoring non-Token $c"); None
      };
      var toImport = List.empty[Importable];
      if (config.weapon.isSupplied) {
        config.weapon().foreach { s =>
          EPCompendium.getWeapon(s) match {
            case Some(w) => toImport ::= w
            case None    => ctx.reply(s"No weapon found for name ${s}")
          }
        }
      }
      val updatedCharacters = tokens.flatMap { token =>
        debug(s"Working on token: ${token.name} (${token.id})");
        token.represents match {
          case Some(char) => {
            var updates = List.empty[String];
            debug(s"Token represents $char");

            val idPool = RowIdPool();
            val importCache = ImportCache(char);

            toImport.foreach { i =>
              i.importInto(char, idPool, importCache) match {
                case Left(msg)  => updates ::= s"Imported ${i.updateLabel} ($msg)"
                case Right(msg) => updates ::= s"Failed to import ${i.updateLabel} (correctly): $msg"
              }
            }

            Some(char.name -> updates)
          }
          case None => ctx.reply(s"Token ${token.name}(${token.id}) does not represent any character!"); None
        }

      };
      val updates = updatedCharacters.map(_ match {
        case (char, ups) => char + ups.mkString("<ul><li>", "</li><li>", "</li></ul>")
      }).mkString("<ul><li>", "</li><li>", "</li></ul>");
      debug(s"Updates: $updates")
      ctx.reply(s"Updated Characters $updates");
    }
  }
}

class EPCompendiumDataConf(_args: Seq[String]) extends ScallopAPIConf(_args) {
  val search = opt[String]("search", descr = "Search for items with similar names to <param>.");
  val nameOnly = opt[Boolean]("name-only", descr = "Only show names, not statblocks.");
  val rank = opt[Boolean]("rank", descr = "Rank all significant results, instead of showing highest one only.");
  val weapon = opt[String]("weapon", descr = "Search for exact matches with <param> in weapons.");
  val morph = opt[String]("morph", descr = "Search for exact matches with <param> in morphs.");
  dependsOnAny(nameOnly, List(search, weapon, morph));
  dependsOnAll(rank, List(search));
  requireOne(search, weapon, morph);
  verify();
}

object EPCompendiumDataCommand extends APICommand[EPCompendiumDataConf] {
  import APIImplicits._;
  override def command = "epcompendium-data";
  override def options = (args) => new EPCompendiumDataConf(args);
  override def apply(config: EPCompendiumDataConf, ctx: ChatContext): Unit = {
    if (config.search.isSupplied) {
      val needle = config.search();
      val results = EPCompendium.findAnything(needle);
      if (results.isEmpty) {
        ctx.reply("No results found");
        return ;
      }
      if (config.rank()) {
        if (config.nameOnly()) {
          val pretty = results.map(r => r.templateTitle).mkString("<ul><li>", "</li><li>", "</li><ul>");
          ctx.reply(pretty);
        } else {
          results.foreach { r =>
            val pretty = asInfoTemplate(r);
            ctx.reply(pretty);
          }
        }
      } else { // head only
        val top = results.head;
        val pretty = if (config.nameOnly()) {
          top.templateTitle
        } else {
          asInfoTemplate(top)
        };
        ctx.reply(pretty);
      }
    } else if (config.weapon.isSupplied) {
      // TODO
    } else if (config.morph.isSupplied) {
      // TODO
    }
  }
}
