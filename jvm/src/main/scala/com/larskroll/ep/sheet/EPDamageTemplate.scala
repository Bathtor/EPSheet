package com.larskroll.ep.sheet

import com.larskroll.roll20.sheet._
import scalatags.Text.all._
import SheetImplicits._

object EPDamageTemplate extends RollTemplate {
  override def name: String = "ep-damage";

  val t = EPTranslation;
  val sty = EPStyle;

  def apply(cf: Field[String], af: Field[String], rf: RollField[Int], apf: Field[Int]): TemplateApplication =
    apply(character <<= cf, attributeField <<= af, damageRoll <<= rf, armourPenetration <<= apf);

  // **** Fields ****
  val character = attribute[String]("character");
  val attributeLabel = labeli18n("attribute-label");
  val attributeField = attribute[String]("attribute-field");
  val damageRoll = rollable[Int]("damage-roll");
  val armourPenetration = attribute[Int]("armour-penetration");

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
    p(span(t.damageInflicts), span(raw(" ")), span(fontWeight.bold, t.damageValue), span(": "), damageRoll),
    p(span(fontWeight.bold, t.ap), span(": "), span(sty.fieldvalue, armourPenetration), span(" "), span(fontStyle.italic, t.orTotalAP)));
  //    switchExists(attackCritical, {
  //      switchExists(attackMoS, {
  //        damageResult(damageRoll)
  //      }, {
  //
  //      })
  //    }, {
  //      switchExists(attackMoS, {
  //
  //      }, {
  //        damageResult(damageRoll)
  //      })
  //    }));
}
