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
package com.lkroll.ep.sheet

import com.lkroll.roll20.sheet._
import com.lkroll.roll20.sheet.model._
import com.lkroll.ep.model.{EPTranslation => TranslationKeys, _}
import scalatags.Text.all._
import SheetImplicits._

object MuseTab extends FieldGroup {
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val aptitudes = MuseAptitudes(
    Seq(
      AptitudeRow(t.aptTotal,
                  Seq(char.museCog, char.museCoo, char.museInt, char.museRef, char.museSav, char.museSom, char.museWil))
    )
  );

  val infoBlock = fblock(
    t.museInfo,
    EPStyle.min5rem,
    (t.museName -> dualMode(char.museName)),
    (t.tt -> char.museTraumaThreshold),
    (t.luc -> char.museLucidity),
    (t.ir -> char.museInsanityRating),
    (t.museNotes -> dualMode(char.museNotes.like(CoreTabRenderer.textareaField)))
  );

  val topRow = eprow(
    frow(
      sty.`flex-centre`,
      flexFillNarrow,
      sblock(t.mentalHealth,
             sty.max15rem,
             char.museTraumaMod.hidden,
             (t.stress -> char.museStress),
             (t.trauma -> char.museTrauma)),
      flexFillNarrow
    )
  );

  val leftCol = fcol(Seq(EPStyle.`flex-grow`, EPStyle.exactly15rem, EPStyle.marginr1rem), infoBlock);
  val rightCol = fcol(
    Seq(EPStyle.exactly23rem),
    block(t.aptitudes, aptitudes),
    block(
      t.museSkills,
      char.museSkills {
        TightRepRow(
          presOnly(
            tightfrow(
              char.museSkills.total like { total =>
                span(sty.skillTotal, name := total.name)
              },
              roll(
                char.museSkills,
                "muse_skill_roll",
                char.chatOutputEPRolls,
                EPDefaultTemplate(char.museName,
                                  char.museSkills.skillName,
                                  char.museSkills.field,
                                  char.epRoll,
                                  char.museSkills.rollTarget),
                char.museSkills.skillName like { sname =>
                  span(sty.skillName, name := sname.name)
                }
              ),
              char.museSkills.field like { sfield =>
                span(sty.skillField, name := sfield.name)
              },
              char.museSkills.linkedAptitude like { apt =>
                span(sty.skillApt, "(", span(name := apt.name), ")")
              },
              flexFill
            )
          ),
          editOnly(
            tightfrow(
              sty.halfRemRowSeparator,
              char.museSkills.skillName.like(CoreTabRenderer.textWithPlaceholder(t.skillName.placeholder)),
              char.museSkills.field.like(CoreTabRenderer.textWithPlaceholder(t.skillField.placeholder)),
              char.museSkills.linkedAptitude,
              (t.skillRanks -> char.museSkills.ranks),
              (t.skillTotal -> char.museSkills.total),
              flexFill
            )
          )
        )
      }
    ),
    block(
      t.skillCommands,
      char.generateMuseSkillsLabel like { rid =>
        input(`type` := "hidden", name := rid.name, value := rid.initialValue)
      },
      tightfrow(pseudoButton(char.generateMuseSkills, char.generateMuseSkillsLabel), flexFill)
    )
  );

  val members: Seq[SheetElement] = Seq(topRow, frow(sty.`flex-start`, leftCol, rightCol));

  override def renderer = CoreTabRenderer;
}
