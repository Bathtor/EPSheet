package com.lkroll.ep

import com.lkroll.ep.compendium.ChatRenderable
import com.lkroll.roll20.core.{ Rolls, APIButton, RollExpression }
import com.lkroll.ep.model.{ EPCharModel => epmodel }
import com.lkroll.roll20.api.templates._

package object api {
  import TemplateImplicits._;

  def asInfoTemplate(title: String, subtitle: String, importButton: Option[APIButton], keys: List[(String, String)], description: String): String = {
    val t = templateV("title" -> title);
    val st = templateV("subtitle" -> subtitle);
    val ib = templateV("import" -> importButton);
    val d = templateV("description" -> description);
    val other = keys.map(templateV(_));
    val vars = t :: st :: ib :: d :: other;

    templateApplication("ep-info", vars)
  }

  def asInfoTemplate(r: ChatRenderable): String = {
    asInfoTemplate(r.templateTitle, r.templateSubTitle, None, r.templateKV.toList.sortBy(_._1), r.templateDescr)
  }

  def asInfoTemplate(r: ChatRenderable, importButton: APIButton, buttons: (String, APIButton)*): String = asInfoTemplate(r, importButton, buttons);

  def asInfoTemplate(r: ChatRenderable, importButton: APIButton, buttons: Iterable[(String, APIButton)]): String = {
    val btns = buttons.map({
      case (k, v) => (k -> v.render)
    }).toMap;
    val rkv = r.templateKV;
    val kv = (rkv ++ btns).toList.sortBy(_._1); // override original fields with same name buttons
    asInfoTemplate(r.templateTitle, r.templateSubTitle, Some(importButton), kv, r.templateDescr)
  }

  def asDefaultTemplate(character: String, attributeField: String, attributeSubField: Option[String] = None,
                        testRoll: Rolls.InlineRoll[Int], testTarget: Rolls.InlineRoll[Int],
                        testMoF: Option[Rolls.InlineRoll[Int]] = None): String = {
    val char = templateV("character" -> character);
    val field = templateV("attribute-field" -> attributeField);
    val subField = templateV("attribute-subfield" -> attributeSubField);
    val roll = templateV("test-roll" -> testRoll);
    val target = templateV("test-target" -> testTarget);
    val mof = templateV("test-mof" -> testMoF);
    templateApplication("ep-default", char, field, subField, roll, target, mof)
  }

  def asDamageTemplate(character: String, attributeField: String,
                       damageRoll: Rolls.InlineRoll[Int], damageType: String,
                       armourPenetration: Int): String = {
    val char = templateV("character" -> character);
    val field = templateV("attribute-field" -> attributeField);
    val roll = templateV("damage-roll" -> damageRoll);
    val dType = templateV("damage-type" -> damageType);
    val ap = templateV("armour-penetration" -> armourPenetration);
    templateApplication("ep-damage", char, field, roll, dType, ap)
  }

  //  private def insertRolls(data: List[(String, String)]): List[(String, String)] = data.map {
  //    case ("Damage", rollS)    => {
  //      val c = SpecialRollsCommand.minConf;
  //      SpecialRollsCommand.invoke(rollS, args)
  //    }
  //    case ("Skill", skillName) => {
  //      ("Skill" -> API)
  //    }
  //    case t                    => t
  //  }
}
