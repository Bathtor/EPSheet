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
import com.lkroll.ep.compendium._
import com.lkroll.ep.compendium.utils.OptionPickler._
import com.lkroll.ep.model.{
  EPCharModel => epmodel,
  ActiveSkillSection,
  KnowledgeSkillSection,
  Skills,
  Aptitude => ModelAptitude
}
import APIImplicits._;
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Try

object SkillConversions {
  def compendiumCat2modelCat(c: SkillCategory): Skills.SkillCategory.SkillCategory = {
    c match {
      case SkillCategory.Combat    => Skills.SkillCategory.Combat
      case SkillCategory.Mental    => Skills.SkillCategory.Mental
      case SkillCategory.Physical  => Skills.SkillCategory.Physical
      case SkillCategory.Psi       => Skills.SkillCategory.Psi
      case SkillCategory.Social    => Skills.SkillCategory.Social
      case SkillCategory.Technical => Skills.SkillCategory.Technical
      case SkillCategory.Vehicle   => Skills.SkillCategory.Vehicle
      case SkillCategory.NA        => Skills.SkillCategory.NA
    }
  }
  def modsForSkillCategory(c: Skills.SkillCategory.SkillCategory): ArithmeticExpression[Int] = {
    import Skills.SkillCategory._;
    c match {
      case Combat | Physical => epmodel.globalPhysicalMods
      case _                 => epmodel.globalMods
    }
  }
  def compendiumApt2ModelApt(a: Aptitude): ModelAptitude.Aptitude = {
    a match {
      case Aptitude.COG => ModelAptitude.COG
      case Aptitude.COO => ModelAptitude.COO
      case Aptitude.INT => ModelAptitude.INT
      case Aptitude.REF => ModelAptitude.REF
      case Aptitude.SAV => ModelAptitude.SAV
      case Aptitude.SOM => ModelAptitude.SOM
      case Aptitude.WIL => ModelAptitude.WIL
    }
  }
  def aptField(a: Aptitude): FieldLike[Int] = {
    a match {
      case Aptitude.COG => epmodel.cogTotal
      case Aptitude.COO => epmodel.cooTotal
      case Aptitude.INT => epmodel.intTotal
      case Aptitude.REF => epmodel.refTotal
      case Aptitude.SAV => epmodel.savTotal
      case Aptitude.SOM => epmodel.somTotal
      case Aptitude.WIL => epmodel.wilTotal
    }
  }
}

case class SkillImport(s: CharacterSkill) extends Importable {

  private var assignedRowId: Option[String] = None;

  def updateLabel: String = s.name;
  def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    val rowId = Some(idPool.generateRowId());
    this.assignedRowId = rowId;

    s.cls match {
      case SkillClass.Active => {
        char.createRepeating(ActiveSkillSection.rowId, rowId) <<= rowId.value;
        char.createRepeating(ActiveSkillSection.skillName, rowId) <<= s.name;
        s.field match {
          case Some(f) => char.createRepeating(ActiveSkillSection.field, rowId) <<= f;
          case None    => () // ignore
        }
        // categories (duplicating some code from the sheet worker)
        val cat = SkillConversions.compendiumCat2modelCat(s.category);
        char.createRepeating(ActiveSkillSection.category, rowId).setWithWorker(cat.toString);
        // val catLabel = Skills.SkillCategory.dynamicLabelShort(cat);
        // char.createRepeating(ActiveSkillSection.categoryShort, rowId) <<= catLabel;
        val globalModsExpression = SkillConversions.modsForSkillCategory(cat);
        char.createRepeating(ActiveSkillSection.globalMods, rowId) <<= ActiveSkillSection.globalMods.valueFrom(
          globalModsExpression
        );

        char.createRepeating(ActiveSkillSection.specialisations, rowId) <<= s.specs.mkString(", ");
        val apt = SkillConversions.compendiumApt2ModelApt(s.apt);
        char.createRepeating(ActiveSkillSection.linkedAptitude, rowId) <<= apt.toString();
        char.createRepeating(ActiveSkillSection.noDefaulting, rowId) <<= s.noDefaulting;
        char.createRepeating(ActiveSkillSection.ranks, rowId) <<= s.ranks;

        if (s.noDefaulting && s.ranks == 0) {
          char.createRepeating(ActiveSkillSection.total, rowId) <<= 0;
        } else {
          val aptValue = char.attribute(SkillConversions.aptField(s.apt)).getOrDefault;
          char.createRepeating(ActiveSkillSection.total, rowId) <<= s.ranks + aptValue;
        }

        Ok("Ok")
      }
      case SkillClass.Knowledge => {
        char.createRepeating(KnowledgeSkillSection.rowId, rowId) <<= rowId.value;
        char.createRepeating(KnowledgeSkillSection.skillName, rowId) <<= s.name;
        s.field match {
          case Some(f) => char.createRepeating(KnowledgeSkillSection.field, rowId) <<= f;
          case None    => () // ignore
        }
        char.createRepeating(KnowledgeSkillSection.specialisations, rowId) <<= s.specs.mkString(", ");
        val apt = SkillConversions.compendiumApt2ModelApt(s.apt);
        char.createRepeating(KnowledgeSkillSection.linkedAptitude, rowId) <<= apt.toString();
        char.createRepeating(KnowledgeSkillSection.noDefaulting, rowId) <<= s.noDefaulting;
        char.createRepeating(KnowledgeSkillSection.ranks, rowId) <<= s.ranks;

        if (s.noDefaulting && s.ranks == 0) {
          char.createRepeating(KnowledgeSkillSection.total, rowId) <<= 0;
        } else {
          val aptValue = char.attribute(SkillConversions.aptField(s.apt)).getOrDefault;
          char.createRepeating(KnowledgeSkillSection.total, rowId) <<= s.ranks + aptValue;
        }

        Ok("Ok")
      }
    }
  }

  override def triggerWorkers(char: Character)(implicit ec: ExecutionContext): Future[Unit] = {
    val r = Try(this.assignedRowId.get).map(rowId => {
      s.cls match {
        case SkillClass.Active => {
          char
            .createRepeating(ActiveSkillSection.skillName, Some(rowId))
            .setWithWorker(s.name); // force trigger of Fray/2 worker
        }
        case _ => Future.successful(())
      }
    });
    Future.fromTry(r).flatten
  }
}

case class SkillDefImport(s: SkillDef) extends Importable {

  private var assignedRowId: Option[String] = None;

  def updateLabel: String = s.name;
  def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Result[String] = {
    val rowId = Some(idPool.generateRowId());
    this.assignedRowId = rowId;

    s.cls match {
      case SkillClass.Active => {
        char.createRepeating(ActiveSkillSection.rowId, rowId) <<= rowId.value;
        char.createRepeating(ActiveSkillSection.skillName, rowId) <<= s.name;
        s.field match {
          case Some(f) => char.createRepeating(ActiveSkillSection.field, rowId) <<= f;
          case None    => () // ignore
        }
        // categories (duplicating some code from the sheet worker)
        val cat = SkillConversions.compendiumCat2modelCat(s.category);
        char.createRepeating(ActiveSkillSection.category, rowId).setWithWorker(cat.toString);
        // val catLabel = Skills.SkillCategory.dynamicLabelShort(cat);
        // char.createRepeating(ActiveSkillSection.categoryShort, rowId) <<= catLabel;
        val globalModsExpression = SkillConversions.modsForSkillCategory(cat);
        char.createRepeating(ActiveSkillSection.globalMods, rowId) <<= ActiveSkillSection.globalMods.valueFrom(
          globalModsExpression
        );

        char.createRepeating(ActiveSkillSection.specialisations, rowId) <<= ActiveSkillSection.specialisations.resetValue;
        val apt = SkillConversions.compendiumApt2ModelApt(s.apt);
        char.createRepeating(ActiveSkillSection.linkedAptitude, rowId) <<= apt.toString();
        char.createRepeating(ActiveSkillSection.noDefaulting, rowId) <<= s.noDefaulting;
        char.createRepeating(ActiveSkillSection.ranks, rowId) <<= 0;

        if (s.noDefaulting) {
          char.createRepeating(ActiveSkillSection.total, rowId) <<= 0;
        } else {
          val aptValue = char.attribute(SkillConversions.aptField(s.apt)).getOrDefault;
          char.createRepeating(ActiveSkillSection.total, rowId) <<= aptValue;
        }

        Ok("Ok")
      }
      case SkillClass.Knowledge => {
        char.createRepeating(KnowledgeSkillSection.rowId, rowId) <<= rowId.value;
        char.createRepeating(KnowledgeSkillSection.skillName, rowId) <<= s.name;
        s.field match {
          case Some(f) => char.createRepeating(KnowledgeSkillSection.field, rowId) <<= f;
          case None    => () // ignore
        }
        char.createRepeating(KnowledgeSkillSection.specialisations, rowId) <<= KnowledgeSkillSection.specialisations.resetValue;
        val apt = SkillConversions.compendiumApt2ModelApt(s.apt);
        char.createRepeating(KnowledgeSkillSection.linkedAptitude, rowId) <<= apt.toString();
        char.createRepeating(KnowledgeSkillSection.noDefaulting, rowId) <<= s.noDefaulting;
        char.createRepeating(KnowledgeSkillSection.ranks, rowId) <<= 0;

        if (s.noDefaulting) {
          char.createRepeating(KnowledgeSkillSection.total, rowId) <<= 0;
        } else {
          val aptValue = char.attribute(SkillConversions.aptField(s.apt)).getOrDefault;
          char.createRepeating(KnowledgeSkillSection.total, rowId) <<= aptValue;
        }

        Ok("Ok")
      }
    }
  }

  override def triggerWorkers(char: Character)(implicit ec: ExecutionContext): Future[Unit] = {
    val r = Try(this.assignedRowId.get).map(rowId => {
      s.cls match {
        case SkillClass.Active => {
          char
            .createRepeating(ActiveSkillSection.skillName, Some(rowId))
            .setWithWorker(s.name); // force trigger of Fray/2 worker
        }
        case _ => Future.successful(())
      }
    });
    Future.fromTry(r).flatten
  }
}
