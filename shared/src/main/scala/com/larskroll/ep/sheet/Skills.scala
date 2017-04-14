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

package com.larskroll.ep.sheet

object Skills {
  object SkillClass extends Enumeration {
    type SkillClass = Value;

    val Active, Knowledge = Value;

    val labelPrefix = "skill-classopt";
    //val labelShortPrefix = "skill-classopt-short";

    def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
    //def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
  }

  object SkillCategory extends Enumeration {
    type SkillCategory = Value;

    val Combat, Mental, Physical, Psi, Social, Technical, Vehicle, NA = Value;

    val labelPrefix = "skill-catopt";
    val labelShortPrefix = "skill-catopt-short";

    def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
    def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
  }

  case class ActiveSkillTuple(id: String, rowId: Option[String], name: Option[String], category: Option[String], aptitude: Option[String], field: Option[String]) {
    val sortId: String = rowId match {
      case Some("?") => id
      case Some(x)   => x
      case None      => id
    }
  }
  case class KnowledgeSkillTuple(id: String, rowId: Option[String], name: Option[String], aptitude: Option[String], field: Option[String]) {
    val sortId: String = rowId match {
      case Some("?") => id
      case Some(x)   => x
      case None      => id
    }
  }

  object SortBy extends Enumeration {

    type SortBy = Value;

    val None, Name, Category, Aptitude = Value;

    val labelPrefix = "skill-sortopt";

    def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";

    val activeIdOrd = Ordering[String].on((x: ActiveSkillTuple) => x.sortId);
    val knowledgeIdOrd = Ordering[String].on((x: KnowledgeSkillTuple) => x.sortId);
    val activeNameOrd = Ordering[(Option[String], Option[String], String)].on((x: ActiveSkillTuple) => (x.name, x.field, x.sortId));
    val knowledgeNameOrd, knowledgeCategoryOrd = Ordering[(Option[String], Option[String], String)].on((x: KnowledgeSkillTuple) => (x.name, x.field, x.sortId));
    val activeCategoryOrd = Ordering[(Option[String], Option[String], Option[String], String)].on((x: ActiveSkillTuple) => (x.category, x.name, x.field, x.sortId));
    val activeAptOrd = Ordering[(Option[String], Option[String], Option[String], String)].on((x: ActiveSkillTuple) => (x.aptitude, x.name, x.field, x.sortId));
    val knowledgeAptOrd = Ordering[(Option[String], Option[String], Option[String], String)].on((x: KnowledgeSkillTuple) => (x.aptitude, x.name, x.field, x.sortId));

    def activeOrdering(sortBy: SortBy): Ordering[ActiveSkillTuple] = {
      sortBy match {
        case None     => activeIdOrd
        case Name     => activeNameOrd
        case Category => activeCategoryOrd
        case Aptitude => activeAptOrd
      }
    }

    def knowledgeOrdering(sortBy: SortBy): Ordering[KnowledgeSkillTuple] = {
      sortBy match {
        case None     => knowledgeIdOrd
        case Name     => knowledgeNameOrd
        case Category => knowledgeCategoryOrd
        case Aptitude => knowledgeAptOrd
      }
    }

  }

  import SkillClass._
  import SkillCategory._
  import Aptitude._

  val pregen = Seq(
    Skill("Academics", Some("???"), Knowledge, NA, COG),
    Skill("Animal Handling", None, Active, Social, SAV),
    Skill("Art", Some("???"), Knowledge, NA, INT),
    Skill("Beam Weapons", None, Active, Combat, COO),
    Skill("Blades", None, Active, Combat, SOM),
    Skill("Climbing", None, Active, Physical, SOM),
    Skill("Clubs", None, Active, Combat, SOM),
    Skill("Control", None, Active, Psi, WIL, true),
    Skill("Deception", None, Active, Social, SAV),
    Skill("Demolitions", None, Active, Technical, COG, true),
    Skill("Disguise", None, Active, Physical, INT),
    Skill("Exotic Melee Weapon", Some("???"), Active, Combat, SOM),
    Skill("Exotic Ranged Weapon", Some("???"), Active, Combat, COO),
    Skill("Flight", None, Active, Physical, SOM),
    Skill("Fray", None, Active, Combat, REF),
    Skill("Free Fall", None, Active, Physical, REF),
    Skill("Freerunning", None, Active, Physical, SOM),
    Skill("Gunnery", None, Active, Combat, INT),
    Skill("Hardware", Some("???"), Active, Technical, COG),
    Skill("Impersonation", None, Active, Social, SAV),
    Skill("Infiltration", None, Active, Physical, COO),
    Skill("Infosec", None, Active, Technical, COG, true),
    Skill("Interest", Some("???"), Knowledge, NA, COG),
    Skill("Interfacing", None, Active, Technical, COG),
    Skill("Intimidation", None, Active, Social, SAV),
    Skill("Investigation", None, Active, Mental, INT),
    Skill("Kinesics", None, Active, Social, SAV),
    Skill("Kinetic Weapons", None, Active, Combat, COO),
    Skill("Language", Some("???"), Knowledge, NA, INT),
    Skill("Medicine", Some("???"), Active, Technical, COG),
    Skill("Navigation", None, Active, Mental, INT),
    Skill("Networking", Some("Autonomists"), Active, Social, SAV),
    Skill("Networking", Some("Criminals"), Active, Social, SAV),
    Skill("Networking", Some("Ecologists"), Active, Social, SAV),
    Skill("Networking", Some("Firewall"), Active, Social, SAV),
    Skill("Networking", Some("Hypercorps"), Active, Social, SAV),
    Skill("Networking", Some("Media"), Active, Social, SAV),
    Skill("Networking", Some("Scientists"), Active, Social, SAV),
    Skill("Palming", None, Active, Physical, COO),
    Skill("Perception", None, Active, Mental, INT),
    Skill("Persuasion", None, Active, Social, SAV),
    Skill("Pilot", Some("???"), Active, Vehicle, REF),
    Skill("Profession", Some("???"), Knowledge, NA, COG),
    Skill("Programming", None, Active, Technical, COG, true),
    Skill("Protocol", None, Active, Social, SAV),
    Skill("Psi Assault", None, Active, Psi, WIL, true),
    Skill("Psychosurgery", None, Active, Technical, INT),
    Skill("Research", None, Active, Technical, COG),
    Skill("Scrounging", None, Active, Mental, INT),
    Skill("Seeker Weapons", None, Active, Combat, COO),
    Skill("Sense", None, Active, Psi, INT, true),
    Skill("Spray Weapons", None, Active, Combat, COO),
    Skill("Swimming", None, Active, Physical, SOM),
    Skill("Throwing Weapons", None, Active, Combat, COO),
    Skill("Unarmed Combat", None, Active, Combat, SOM));
}

import Skills._
import SkillClass._
import SkillCategory._

case class Skill(name: String, field: Option[String], cls: SkillClass, category: SkillCategory, apt: Aptitude.Aptitude, noDefaulting: Boolean = false)
