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

import scala.concurrent.Future

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.ep.compendium._
import com.lkroll.ep.compendium.utils.OptionPickler._
import com.lkroll.ep.model.{ EPCharModel => epmodel, IdentitiesSection, MorphSection }
import APIImplicits._;

class CharacterImport(val c: EPCharacter) extends Importable {
  private var morphId: Option[String] = None;

  override def updateLabel: String = s"Character <c.name>";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    char.name = c.name;
    char.attribute(epmodel.genderId) <<= c.gender.entryName;
    char.attribute(epmodel.actualAge) <<= c.age;
    char.attribute(epmodel.motivations) <<= c.motivations.map(_.text).mkString(", ");
    char.attribute(epmodel.faction) <<= c.faction;
    char.attribute(epmodel.background) <<= c.background;
    val moxie = char.createAttribute(epmodel.moxie);
    moxie <<= c.moxie;
    moxie.max = c.moxie.toString;
    char.attribute(epmodel.miscNotes) <<= c.history.mkString("\n");
    char.attribute(epmodel.async) <<= c.isAsync;

    // Aptitudes
    char.attribute(epmodel.cogBase) <<= c.aptitudes.base.cog.getOrElse(0);
    char.attribute(epmodel.cooBase) <<= c.aptitudes.base.coo.getOrElse(0);
    char.attribute(epmodel.intBase) <<= c.aptitudes.base.int.getOrElse(0);
    char.attribute(epmodel.refBase) <<= c.aptitudes.base.ref.getOrElse(0);
    char.attribute(epmodel.savBase) <<= c.aptitudes.base.sav.getOrElse(0);
    char.attribute(epmodel.somBase) <<= c.aptitudes.base.som.getOrElse(0);
    char.attribute(epmodel.wilBase) <<= c.aptitudes.base.wil.getOrElse(0);

    //  Skills
    val skillRes = c.skills.foldLeft(Left("Ok").asInstanceOf[Either[String, String]]) { (acc, skill) =>
      val res = SkillImport(skill).importInto(char, idPool, cache);
      res match {
        case Left(_) => acc // ignore lefts
        case Right(err) => acc match {
          case Left(_)       => res // replace lefts with rights
          case Right(errAcc) => Right(s"${errAcc}, ${err}")
        }
      }
    };
    var res = skillRes match {
      case Left(_)    => Left("Ok")
      case Right(err) => Right(s"Some skills failed to import correctly:\n$err")
    };
    cache.reset(); // we changed all the skills here

    // Identities
    val identityRowId = Some(idPool.generateRowId());
    char.createRepeating(IdentitiesSection.identity, identityRowId) <<= c.name;
    char.createRepeating(IdentitiesSection.description, identityRowId) <<= "Primary ID";
    char.createRepeating(IdentitiesSection.credits, identityRowId) <<= c.startingCredit;
    char.createRepeating(IdentitiesSection.notes, identityRowId) <<= "Imported from JSON";
    c.rep.foreach {
      case (network, rep) =>
        network.name match {
          case "@-Rep" => {
            char.createRepeating(IdentitiesSection.atRepScore, identityRowId) <<= rep;
          }
          case "c-Rep" => {
            char.createRepeating(IdentitiesSection.cRepScore, identityRowId) <<= rep;
          }
          case "e-Rep" => {
            char.createRepeating(IdentitiesSection.eRepScore, identityRowId) <<= rep;
          }
          case "f-Rep" => {
            char.createRepeating(IdentitiesSection.fRepScore, identityRowId) <<= rep;
          }
          case "g-Rep" => {
            char.createRepeating(IdentitiesSection.gRepScore, identityRowId) <<= rep;
          }
          case "i-Rep" => {
            char.createRepeating(IdentitiesSection.iRepScore, identityRowId) <<= rep;
          }
          case "r-Rep" => {
            char.createRepeating(IdentitiesSection.rRepScore, identityRowId) <<= rep;
          }
          case "u-Rep" => {
            char.createRepeating(IdentitiesSection.uRepScore, identityRowId) <<= rep;
          }
          case "x-Rep" => {
            char.createRepeating(IdentitiesSection.xRepScore, identityRowId) <<= rep;
          }
          case n => {
            res = res match {
              case Left(_)    => Right(s"Unkown rep network $n!")
              case Right(err) => Right(s"${err}\nUnkown rep network ${n}!")
            }
          }
        }
    }
    val morphRowId = idPool.generateRowId();
    val morphRes = MorphInstanceImport(c.activeMorph).importInto(char, morphRowId, cache);
    res = (res, morphRes) match {
      case (Left(_), Left(_)) => {
        this.morphId = Some(morphRowId);
        Left("Ok")
      }
      case (Left(_), x) => x
      case (x, Left(_)) => {
        this.morphId = Some(morphRowId);
        x
      }
      case (Right(err), Right(errMorph)) => {
        Right(s"${err}\nError during morph import: ${errMorph}")
      }
    };
    res
  }
  override def children: List[Importable] = {
    import Importable._;

    var l: List[Importable] = Nil;
    l ++= c.traits.map(trait2Import);
    l ++= c.psiChiSleights.map(psiSleight2Import);
    l ++= c.psiGammaSleights.map(psiSleight2Import);
    l ++= c.gear.map(gearEntry2Import);
    l ++= c.weapons.map {
      case Left(w)    => weapon2Import(w)
      case Right(wwa) => wwa2Import(wwa)
    };
    l ++= c.armour.map {
      case Left(a)   => armour2Import(a)
      case Right(ma) => moddedarmour2Import(ma)
    };
    l ++= c.software.map(software2Import);
    l
  }

  override def triggerWorkers(char: Character): Future[Unit] = {
    this.morphId match {
      case rowId @ Some(_) => {
        // just setting a morph active, should cause almost all dynamic values on the sheet to be recalculated
        val f = char.createRepeating(MorphSection.active, rowId).setWithWorker(true);
        this.morphId = None; // reset to not activate twice
        f
      }
      case None => Future.failed(new RuntimeException("You must import a character, before triggering workers!"))
    }
  }
}
