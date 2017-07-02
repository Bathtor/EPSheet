package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._
import SheetImplicits._

object EPDamageTemplate extends RollTemplate {
  import FieldImplicits._
  override def name: String = "ep-damage";

  val t = EPTranslation;
  val sty = EPStyle;

  def apply(cf: Field[String], al: LabelI18N, rf: RollField[Int]): TemplateApplication =
    apply(character <<= cf, attributeLabel <<= al, damageRoll <<= rf)

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], dtf: Field[String], apf: Field[Int]): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageType <<= dtf, armourPenetration <<= apf);

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], dtf: Field[String], apf: Field[Int], xdbf: CommandButton, xdfa: CommandButton): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, damageType <<= dtf, armourPenetration <<= apf, concentrateBF <<= xdbf, concentrateFA <<= xdfa)

  // **** Fields ****
  val character = attribute[String]("character");
  val attributeLabel = labeli18n("attribute-label");
  val attributeField = attribute[String]("attribute-field");
  val damageRoll = rollable[Int]("damage-roll");
  val damageType = attribute[String]("damage-type");
  val armourPenetration = attribute[Int]("armour-penetration");
  val concentrateBF = button("concentrate-damage-bf");
  val concentrateFA = button("concentrate-damage-fa");

  def damageResult(r: TemplateFields.RollableField[Int]) =
    p(span(t.damageInflicts), span(raw(" ")), span(fontWeight.bold, t.damageValue), span(": "), r);

  // **** Layout ****
  override def content: Tag = div(sty.`template-wrapper`,
    h3(character),
    exists(attributeLabel) {
      h4(attributeLabel)
    },
    exists(attributeField) {
      h4(attributeField)
    },
    p(span(t.damageInflicts), span(raw(" ")), span(fontWeight.bold, t.damageValue), span(": "), damageRoll,
      exists(damageType) {
        Seq(span(raw(" ")), span(damageType))
      }),
    exists(armourPenetration) {
      p(span(fontWeight.bold, t.ap), span(": "), span(sty.fieldvalue, armourPenetration), span(" "), span(fontStyle.italic, t.orTotalAP))
    },
    exists(concentrateBF) {
      exists(concentrateFA) {
        Seq(
          h4(span(sty.`sub-header`, t.concentrateFire)),
          p(span(fontWeight.bold, t.burstFire), span(": "), concentrateBF),
          p(span(fontWeight.bold, t.fullAutomatic), span(": "), concentrateFA))
      }
    });
}
