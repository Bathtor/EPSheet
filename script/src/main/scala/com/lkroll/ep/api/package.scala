package com.lkroll.ep

import com.lkroll.ep.compendium.ChatRenderable

package object api {
  def asInfoTemplate(title: String, subtitle: String, keys: List[(String, String)], description: String): String = {
    val temp = "&{template:ep-info}";
    val t = s"{{title=$title}}";
    val st = s"{{subtitle=$subtitle}}";
    val d = s"{{description=$description}}";
    val other = keys.map {
      case (key, value) => s"{{$key=$value}}"
    };
    temp ++ t ++ st ++ other.mkString(" ") ++ d
  }

  def asInfoTemplate(r: ChatRenderable): String = {
    asInfoTemplate(r.templateTitle, r.templateSubTitle, r.templateKV.toList.sortBy(_._1), r.templateDescr)
  }
}
