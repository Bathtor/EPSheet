package com.lkroll.ep

import com.lkroll.ep.compendium.ChatRenderable
import com.lkroll.roll20.core.APIButton

package object api {
  def asInfoTemplate(title: String, subtitle: String, keys: List[(String, String)], description: String, button: Option[APIButton] = None): String = {
    val temp = "&{template:ep-info}";
    val t = s"{{title=$title}}";
    val st = s"{{subtitle=$subtitle}}";
    val d = s"{{description=$description}}";
    val other = keys.map {
      case (key, value) => s"{{$key=$value}}"
    };
    val bO = button.map(btn => s"{{Import=${btn.render}}}");
    bO match {
      case Some(b) => temp ++ t ++ st ++ other.mkString(" ") ++ b ++ d
      case None    => temp ++ t ++ st ++ other.mkString(" ") ++ d
    }

  }

  def asInfoTemplate(r: ChatRenderable): String = {
    asInfoTemplate(r.templateTitle, r.templateSubTitle, r.templateKV.toList.sortBy(_._1), r.templateDescr)
  }

  def asInfoTemplate(r: ChatRenderable, button: APIButton): String = {
    asInfoTemplate(r.templateTitle, r.templateSubTitle, r.templateKV.toList.sortBy(_._1), r.templateDescr, Some(button))
  }
}
