package com.larskroll.ep.sheet

object TraitType extends Enumeration {
  type TraitType = Value;

  val Positive, Negative, Neutral = Value;

  val labelPrefix = "trait-type-opt";
  val labelShortPrefix = "trait-type-opt-short";

  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
  def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
}
