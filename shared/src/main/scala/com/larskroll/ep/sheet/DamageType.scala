package com.larskroll.ep.sheet

object DamageType extends Enumeration {
  type DamageType = Value;

  val Kinetic, Energy = Value;

  val labelPrefix = "damage-typeopt";

  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
}
