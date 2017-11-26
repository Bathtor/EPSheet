package com.larskroll.ep.sheet

object ChatOutput extends Enumeration {

  type ChatOutput = Value;

  val Public, GM = Value;

  val labelPrefix = "damage-typeopt";
  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
}
