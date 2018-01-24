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
package com.lkroll.ep.model

import com.lkroll.roll20.sheet.model._;
import com.lkroll.roll20.core._;

object ActiveSkillSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "activeskills";

  val rowId = text("rowid").editable(false).default("?");
  val skillName = text("name");
  val field = "field".default("");
  val category = "category".options(Skills.SkillCategory);
  val categoryShort = text("category_short").editable(false);
  val specialisations = "specialisations".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val noDefaulting = "no_defaulting".default(false);
  val ranks = "ranks".default(0);
  val morphBonus = "morph_bonus".editable(false).default(0);
  val total = number[Int]("total").editable(false);
  val globalMods = "global_mods".editable(false).expression[Int].default(EPCharModel.globalMods);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total + globalMods.altArith);
  val rollSpecTarget = roll("spec_target", EPCharModel.modQuery.arith + total + globalMods.altArith + 10);
}

object KnowledgeSkillSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "knowledgeskills";

  val rowId = text("rowid").editable(false).default("?");
  val skillName = text("name");
  val field = "field".default("");
  val specialisations = "specialisations".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val noDefaulting = "no_defaulting".default(false);
  val ranks = "ranks".default(0);
  val morphBonus = "morph_bonus".editable(false).default(0);
  val total = number[Int]("total").editable(false);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total + EPCharModel.globalMods);
  val rollSpecTarget = roll("spec_target", EPCharModel.modQuery.arith + total + EPCharModel.globalMods + 10);

}

object MuseSkillSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "museskills";

  val skillName = text("name");
  val field = "field".default("");
  val linkedAptitude = "linked_aptitude".options(Aptitude);
  val ranks = "ranks".default(0);
  val total = number[Int]("total").editable(false);
  val rollTarget = roll("target", EPCharModel.modQuery.arith + total - EPCharModel.museTraumaMod);
}
