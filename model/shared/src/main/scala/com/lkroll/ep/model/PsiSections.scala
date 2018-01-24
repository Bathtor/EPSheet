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

object PsiChiSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "psichi";

  val sleight = text("sleight");
  val psiType = "psi_type".options(PsiType).default(PsiType.Passive);
  val psiTypeShort = text("psi_type_short").editable(false).default(PsiType.dynamicLabelShort(PsiType.Passive));
  val range = "range".default("Self");
  val action = "action".default("Automatic");
  val duration = "duration".default("Constant");
  val strainMod = "strain_mod".default(0);
  val strainDamage = roll("strain_damage", ceil(Dice.d10.arith / 2) + strainMod); // Chi never targets others, and the user can hardly be below full sentience, thus no targetStrainQuery
  val description = text("description");
}

object PsiGammaSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "psigamma";

  val sleight = text("sleight");
  val psiType = "psi_type".options(PsiType).default(PsiType.Active);
  val psiTypeShort = text("psi_type_short").editable(false).default(PsiType.dynamicLabelShort(PsiType.Active));
  val range = "range".default("Touch");
  val action = "action".default("Complex");
  val duration = "duration".default("Temp (Action Turns)");
  val strainMod = "strain_mod".default(0);
  val strainDamage = roll("strain_damage", ceil(Dice.d10.arith / 2) + strainMod + EPCharModel.targetStrainQuery.arith);
  val skillSearch = "skill_search".options("Control", "Psi Assault", "Sense");
  val skillName = "skill_name".editable(false).default("None");
  val skillTotal = "skill_total".ref(EPCharModel.activeSkills.total);
  val attackTarget = roll("attack_target", EPCharModel.targetQuery.arith + EPCharModel.modQuery.arith + skillTotal.altArith + EPCharModel.globalMods);
  val description = text("description");
}
