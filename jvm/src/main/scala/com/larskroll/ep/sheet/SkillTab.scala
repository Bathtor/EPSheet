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

import com.larskroll.roll20.sheet._
import scalatags.Text.all._
import SheetImplicits._

object SkillTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val members: Seq[SheetElement] = Seq(frow(sty.`flex-start`,
    fcol(Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem),
      block(t.activeSkills,
        char.activeSkills(
          SkillRow(
            char.activeSkills.rowId like { rid => input(`type` := "hidden", name := rid.name, value := rid.initialValue) },
            presOnly(tightfrow(
              char.activeSkills.total like { total => span(sty.skillTotal, name := total.name) },
              roll("active_skill_roll", Chat.Default, EPDefaultTemplate(char.characterName, char.activeSkills.skillName, char.activeSkills.skillField, char.epRoll, char.activeSkills.rollTarget), char.activeSkills.skillName like { sname => span(sty.skillName, name := sname.name) }),
              char.activeSkills.skillCategoryShort.like(SkillRenderers.categoryRenderer),
              char.activeSkills.skillField like { sfield => span(sty.skillField, name := sfield.name) },
              char.activeSkills.linkedAptitude like { apt => span(sty.skillApt, "(", span(name := apt.name), ")") },
              //span(raw(" &mdash; ")),
              roll("active_spec_roll", Chat.Default, EPDefaultTemplate(char.characterName, char.activeSkills.skillName, char.activeSkills.specialisations, char.epRoll, char.activeSkills.rollSpecTarget), char.activeSkills.specialisations like { sspec => span(sty.skillSpec, name := sspec.name) }),
              flexFill)),
            editOnly(tightfrow(
              char.activeSkills.skillName like { sname => div(sty.inlineLabelGroup, input(`type` := "text", name := sname.name, value := sname.initialValue, t.skillName.placeholder)) },
              (t.skillNoDefaulting -> char.activeSkills.noDefaulting),
              char.activeSkills.skillField like { sfield => div(sty.inlineLabelGroup, input(`type` := "text", name := sfield.name, value := sfield.initialValue, t.skillField.placeholder)) },
              char.activeSkills.skillCategory,
              char.activeSkills.linkedAptitude,
              (t.skillRanks -> char.activeSkills.ranks),
              (t.skillMorphBonus -> char.activeSkills.morphBonus),
              (t.skillTotal -> char.activeSkills.total),
              char.activeSkills.specialisations like { sspec => div(sty.inlineLabelGroup, input(`type` := "text", name := sspec.name, value := sspec.initialValue, t.skillSpecialisations.placeholder)) },
              flexFill)))))),
    fcol(Seq(EPStyle.`flex-grow`, EPStyle.exactly20rem),
      block(t.knowledgeSkills,
        char.knowledgeSkills(
          SkillRow(
            char.knowledgeSkills.rowId like { rid => input(`type` := "hidden", name := rid.name, value := rid.initialValue) },
            presOnly(tightfrow(
              char.knowledgeSkills.total like { total => span(sty.skillTotal, name := total.name) },
              roll("knowledge_skill_roll", Chat.Default, EPDefaultTemplate(char.characterName, char.knowledgeSkills.skillName, char.knowledgeSkills.skillField, char.epRoll, char.knowledgeSkills.rollTarget), char.knowledgeSkills.skillName like { sname => span(sty.skillName, name := sname.name) }),
              char.knowledgeSkills.skillField like { sfield => span(sty.skillField, name := sfield.name) },
              char.knowledgeSkills.linkedAptitude like { apt => span(sty.skillApt, "(", span(name := apt.name), ")") },
              //span(raw(" &mdash; ")),
              roll("knowledge_spec_roll", Chat.Default, EPDefaultTemplate(char.characterName, char.knowledgeSkills.skillName, char.knowledgeSkills.specialisations, char.epRoll, char.knowledgeSkills.rollSpecTarget), char.knowledgeSkills.specialisations like { sspec => span(sty.skillSpec, name := sspec.name) }),
              flexFill)),
            editOnly(tightfrow(
              char.knowledgeSkills.skillName like { sname => div(sty.inlineLabelGroup, input(`type` := "text", name := sname.name, value := sname.initialValue, t.skillName.placeholder)) },
              (t.skillNoDefaulting -> char.knowledgeSkills.noDefaulting),
              char.knowledgeSkills.skillField like { sfield => div(sty.inlineLabelGroup, input(`type` := "text", name := sfield.name, value := sfield.initialValue, t.skillField.placeholder)) },
              char.knowledgeSkills.linkedAptitude,
              (t.skillRanks -> char.knowledgeSkills.ranks),
              (t.skillMorphBonus -> char.knowledgeSkills.morphBonus),
              (t.skillTotal -> char.knowledgeSkills.total),
              char.knowledgeSkills.specialisations like { sspec => div(sty.inlineLabelGroup, input(`type` := "text", name := sspec.name, value := sspec.initialValue, t.skillSpecialisations.placeholder)) },
              flexFill))))),
      block(t.skillCommands,
        char.generateSkillsLabel like { rid => input(`type` := "hidden", name := rid.name, value := rid.initialValue) },
        tightfrow(
          pseudoButton(char.generateSkills, char.generateSkillsLabel),
          pseudoButton(char.sortSkills, t.skillsSort),
          flexFill),
        tightfrow(
          (t.skillsSortBy -> char.sortSkillsBy),
          flexFill),
        note(t.skillReloadPage),
        note(t.skillNoSortManual)))));

  override def renderer = CoreTabRenderer;

}

object SkillRenderers {
  import RenderMode._;

  //  val nameRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
  //    case (name: Field[_], Presentation) => b(span(name.initialValue))
  //  }

  val categoryRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
    sup(span(EPStyle.`cat-tag-field`, name := f.name, SheetI18N.datai18nDynamic))
  }
}

object SkillRow extends GroupRenderer {
  import GroupRenderer._
  import RenderMode._

  override def fieldCombiner = { tags =>
    div(EPStyle.`flex-container`, tags)
  };

  override def renderEditWrapper(e: Tag): Tag = {
    div(TabbedStyle.edit, width := "100%", e)
  }

  override def renderPresentationWrapper(e: Tag): Tag = {
    div(TabbedStyle.presentation, width := "100%", e)
  }

  override def fieldRenderers: FieldRenderer = CoreTabRenderer.fieldRenderers;
}
