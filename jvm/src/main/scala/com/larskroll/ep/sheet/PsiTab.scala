package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._

object PsiTab extends FieldGroup {
  import SheetImplicits._
  //import Roll20Predef._
  import Blocks._

  val char = EPCharModel;
  val t = EPTranslation;
  val sty = EPStyle;

  val ptRenderer: GroupRenderer.FieldDualRenderer = (f, mode) => {
    sup(span(EPStyle.`cat-tag-field`, name := f.name, SheetI18N.datai18nDynamic))
  }

  val psiChi = block(t.psiChi,
    char.psiChi {
      TightRepRow(
        presOnly(flowpar(
          char.psiChi.sleight.like(GearTab.rowItemName),
          char.psiChi.psiTypeShort.like(ptRenderer),
          span(raw(" [")),
          char.psiChi.strainMod,
          span(raw("] ")),
          char.psiChi.range.like(CoreTabRenderer.italic),
          span(raw(" ~ ")),
          char.psiChi.action,
          span(raw(" / ")),
          char.psiChi.duration,
          char.psiChi.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(sty.halfRemRowSeparator,
          char.psiChi.sleight.like(CoreTabRenderer.textWithPlaceholder(t.sleightName.placeholder)),
          char.psiChi.psiType,
          char.psiChi.psiTypeShort.hidden,
          (t.psiRange -> char.psiChi.range),
          (t.psiAction -> char.psiChi.action),
          (t.psiDuration -> char.psiChi.duration),
          (t.strainMod -> char.psiChi.strainMod),
          span(EPStyle.inlineLabel, t.sleightDescription),
          MarkupElement(char.psiChi.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });
  val psiGamma = block(t.psiGamma,
    char.psiGamma {
      TightRepRow(
        presOnly(flowpar(
          char.psiGamma.sleight.like(GearTab.rowItemName),
          char.psiGamma.psiTypeShort.like(ptRenderer),
          span(raw(" (")),
          char.psiGamma.skill,
          span(raw(") ")),
          span(raw("[")),
          char.psiGamma.strainMod,
          span(raw("] ")),
          char.psiGamma.range.like(CoreTabRenderer.italic),
          span(raw(" ~ ")),
          char.psiGamma.action,
          span(raw(" / ")),
          char.psiGamma.duration,
          char.psiGamma.description.like(CoreTabRenderer.description),
          flexFill)),
        editOnly(tightfrow(sty.halfRemRowSeparator,
          char.psiGamma.sleight.like(CoreTabRenderer.textWithPlaceholder(t.sleightName.placeholder)),
          char.psiGamma.psiType,
          char.psiGamma.psiTypeShort.hidden,
          char.psiGamma.skill.like(CoreTabRenderer.textWithPlaceholder(t.psiSkill.placeholder)),
          (t.psiRange -> char.psiGamma.range),
          (t.psiAction -> char.psiGamma.action),
          (t.psiDuration -> char.psiGamma.duration),
          (t.strainMod -> char.psiGamma.strainMod),
          span(EPStyle.inlineLabel, t.sleightDescription),
          MarkupElement(char.psiGamma.description.like(CoreTabRenderer.textareaFieldGrow)))))
    });

  val members: Seq[SheetElement] = Seq(
    eprow(frow(sty.`flex-centre`,
      flexFillNarrow,
      sblock(t.async, sty.max20rem,
        char.async, span(raw("&nbsp;")), span(t.asyncTrait)),
      flexFillNarrow,
      sblock(t.psiDuration, sty.max20rem,
        (t.psiTempTime -> char.psiTempTime), span(t.psiTempUnits)),
      flexFillNarrow,
      sblock(t.psiSustained, sty.max20rem,
        (t.psiCurrent -> char.psiCurrentSustained),
        (t.psiSustainedMod -> char.psiSustainedMod)),
      flexFillNarrow)),
    frow(sty.`flex-start`,
      fcol(Seq(sty.`flex-grow`, sty.exactly20rem, sty.marginr1rem),
        psiChi),
      fcol(Seq(EPStyle.`flex-grow`, sty.exactly20rem),
        psiGamma)));

  override def renderer = CoreTabRenderer;
}
