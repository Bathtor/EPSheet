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
import com.lkroll.ep.api.{ asInfoTemplate, ScallopUtils, EPScripts }
import com.lkroll.ep.model.{ EPCharModel => epmodel }
import util.{ Try, Success, Failure }
import org.rogach.scallop.singleArgConverter

class EPCompendiumImportConf(_args: Seq[String]) extends ScallopAPIConf(_args) {
  version(s"${EPCompendiumImportCommand.command} ${EPScripts.version} by ${EPScripts.author} ${EPScripts.emailTag}");
  banner(s"""Import data from the Eclipse Phase Compendium.<br/>
All names must be exact. Use '!${EPCompendiumDataCommand.command} --search' to find available names.
""");
  footer(s"<br/>Source code can be found on ${EPScripts.repoLink}");

  val weapon = opt[List[String]]("weapon", descr = "Import a weapon with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val withAmmo = opt[String]("with-ammo", descr = "Must be used together with --weapon. Modifies the weapon to use the specified ammo.")(ScallopUtils.singleArgSpacedConverter(identity));
  val withAccessory = opt[String]("with-accessory", descr = "Must be used together with --weapon. Modifies the weapon to have the specified weapon accessory.")(ScallopUtils.singleArgSpacedConverter(identity));
  val morphModel = opt[List[String]]("morph-model", descr = "Import a generic morph model name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val morph = opt[List[String]]("morph", descr = "Import a custom morph instance with the given label. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val egoTrait = opt[List[String]]("trait", descr = "Import an ego trait with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val derangement = opt[List[String]]("derangement", descr = "Import a derangement with the given name and default duration. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val withDuration = opt[FloatOrInline]("duration", descr = "Must be used together with --derangement. Import derangement with given duration.")(singleArgConverter(FloatOrInline.fromString(_)));
  val disorder = opt[List[String]]("disorder", descr = "Import a disorder with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val armour = opt[List[String]]("armour", descr = "Import an armour with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val withMod = opt[String]("with-mod", descr = "Must be used together with --armour. Modifies the amour to have the specified armour mod.")(ScallopUtils.singleArgSpacedConverter(identity));
  val gear = opt[List[String]]("gear", descr = "Import a gear item with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val software = opt[List[String]]("software", descr = "Import a program with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val substance = opt[List[String]]("substance", descr = "Import a substance with the given name. (Can be specified multiple times)")(ScallopUtils.singleListArgConverter(identity));
  val fromSheet = opt[Boolean]("from-sheet", descr = "Import a JSON formatted item from the API Text Exchange field on the character sheet linked with a selected token");

  dependsOnAll(withAmmo, List(weapon));
  dependsOnAll(withAccessory, List(weapon));
  dependsOnAll(withMod, List(armour));
  codependent(withDuration, derangement);
  //requireOne(weapon, morph);
  verify();
}

sealed trait FloatOrInline {
  def toFloat(ctx: ChatContext): Try[Float];
}
object FloatOrInline {
  import fastparse.all._

  lazy val parser: P[FloatOrInline] = P(inline | raw);
  lazy val raw: P[AFloat] = P((CharIn('0' to '9').rep(1) ~ ("." ~ CharIn('0' to '9').rep(1)).?).!).map(s => AFloat(s.toFloat));
  lazy val inline: P[Inline] = P("$[[" ~/ ws ~ CharIn('0' to '9').rep(1).! ~ ws ~ "]]").map(s => Inline(s.toInt));
  lazy val ws = P(" ".rep);

  def fromString(s: String): FloatOrInline = {
    parser.parse(s) match {
      case Parsed.Success(r, _) => r
      case _: Parsed.Failure    => throw new RuntimeException(s"Could not parse '$s' as Float or Inline Roll Ref!")
    }
  }

  case class AFloat(v: Float) extends FloatOrInline {
    override def toFloat(ctx: ChatContext): Try[Float] = Success(v);
  }

  case class Inline(index: Int) extends FloatOrInline {
    override def toFloat(ctx: ChatContext): Try[Float] = {
      val irs = ctx.inlineRolls;
      Try {
        val ir = irs(index);
        ir.results.total.toFloat
      }
    }
  }
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
        val weapons = config.weapon().flatMap { s =>
          EPCompendium.getWeapon(s) match {
            case Some(w) => Some(w)
            case None => {
              ctx.reply(s"No weapon found for name ${s}");
              None
            }
          }
        };
        val moddedWeaponsO = if (config.withAccessory.isSupplied) {
          val s = config.withAccessory();
          if (s.equalsIgnoreCase("None")) {
            Some(weapons)
          } else {
            EPCompendium.getWeaponAccessory(s) match {
              case Some(accessory) => Some(weapons.map(w => accessory.mod(w)))
              case None => {
                ctx.reply(s"No Weapon Accessory found for name ${s}");
                None
              }
            }
          }
        } else {
          Some(weapons)
        };
        moddedWeaponsO match {
          case Some(moddedWeapons) if config.withAmmo.isSupplied => {
            val s = config.withAmmo();
            if (s.equalsIgnoreCase("None")) {
              moddedWeapons.foreach(w => toImport ::= w)
            } else {
              EPCompendium.getAmmo(s) match {
                case Some(ammo) => {
                  moddedWeapons.foreach { w =>
                    w.load(ammo) match {
                      case Success(w) => toImport ::= w
                      case Failure(e) => ctx.reply(s"Error loading ${ammo.name} into ${w.name}: ${e.getMessage}")
                    }
                  }
                }
                case None => ctx.reply(s"No ammo found for name ${s}}")
              }
            }
          }
          case Some(moddedWeapons) if !config.withAmmo.isSupplied => {
            moddedWeapons.foreach(w => toImport ::= w)
          }
          case None => () // nothing more to do
        }
      }
      if (config.morph.isSupplied) {
        config.morph().foreach { s =>
          EPCompendium.getMorphCustom(s) match {
            case Some(m) => {
              val mi: MorphInstanceImport = m;
              toImport ::= mi;
              toImport ++= mi.children;
            }
            case None => ctx.reply(s"No morph found for name ${s}")
          }
        }
      }
      if (config.morphModel.isSupplied) {
        config.morphModel().foreach { s =>
          EPCompendium.getMorphModel(s) match {
            case Some(m) => {
              val mi: MorphModelImport = m;
              toImport ::= mi;
              toImport ++= mi.children;
            }
            case None => ctx.reply(s"No morph found for name ${s}")
          }
        }
      }
      if (config.egoTrait.isSupplied) {
        config.egoTrait().foreach { s =>
          EPCompendium.getTrait(s) match {
            case Some(t) => {
              toImport ::= t;
            }
            case None => ctx.reply(s"No trait found for name ${s}")
          }
        }
      }
      if (config.derangement.isSupplied && config.withDuration.isSupplied) {
        config.withDuration().toFloat(ctx) match {
          case Success(dur) => {
            config.derangement().foreach { s =>
              EPCompendium.getDerangement(s) match {
                case Some(d) => {
                  toImport ::= DerangementImport(d, dur);
                }
                case None => ctx.reply(s"No derangement found for name ${s}")
              }
            }
          }
          case Failure(e) => ctx.reply(e.getMessage)
        }

      }
      if (config.disorder.isSupplied) {
        config.disorder().foreach { s =>
          EPCompendium.getDisorder(s) match {
            case Some(d) => {
              toImport ::= d;
            }
            case None => ctx.reply(s"No disorder found for name ${s}")
          }
        }
      }
      if (config.armour.isSupplied) {
        val armours = config.armour().flatMap { s =>
          EPCompendium.getArmour(s) match {
            case Some(a) => Some(a)
            case None    => ctx.reply(s"No armour found for name ${s}"); None
          }
        };
        if (config.withMod.isSupplied) {
          val s = config.withMod();
          if (s.equalsIgnoreCase("None")) {
            armours.foreach(a => toImport ::= a)
          } else {
            EPCompendium.getArmourMod(s) match {
              case Some(mod) => armours.foreach(a => toImport ::= a.withMod(mod))
              case None      => ctx.reply(s"No armour mod found for name ${s}")
            }
          }
        } else {
          armours.foreach(a => toImport ::= a)
        }
      }
      if (config.gear.isSupplied) {
        config.gear().foreach { s =>
          EPCompendium.getGear(s) match {
            case Some(g) => {
              toImport ::= g;
            }
            case None => ctx.reply(s"No gear found for name ${s}")
          }
        }
      }
      if (config.software.isSupplied) {
        config.software().foreach { s =>
          EPCompendium.getSoftware(s) match {
            case Some(s) => {
              toImport ::= s;
            }
            case None => ctx.reply(s"No software found for name ${s}")
          }
        }
      }
      if (config.substance.isSupplied) {
        config.substance().foreach { s =>
          EPCompendium.getSubstance(s) match {
            case Some(s) => {
              toImport ::= s;
            }
            case None => ctx.reply(s"No substance found for name ${s}")
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

            if (config.fromSheet()) {
              val text = char.attribute(epmodel.apiText)();
              EPCompendium.readData(text) match {
                case Success(d) => {
                  Importable.fromData(d) match {
                    case Some(i) => i.importInto(char, idPool, importCache) match {
                      case Left(msg)  => updates ::= s"Imported ${i.updateLabel} ($msg)"
                      case Right(msg) => updates ::= s"Failed to import ${i.updateLabel} (correctly): $msg"
                    }
                    case None => updates ::= s"Data from sheet (${d.getClass}) is not importable."
                  }
                }
                case Failure(e) => updates ::= s"Failed to parse data from sheet: ${e.getMessage}"
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
