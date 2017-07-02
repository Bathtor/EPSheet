package com.larskroll.ep.sheet

object DamageType extends Enumeration {
  type DamageType = Value;

  val Kinetic, Energy = Value;

  val labelPrefix = "damage-typeopt";
  val labelShortPrefix = "damage-typeopt-short";

  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
  def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
}
