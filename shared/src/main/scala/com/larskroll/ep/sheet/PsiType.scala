package com.larskroll.ep.sheet

object PsiType extends Enumeration {
  type PsiType = Value;

  val Active, Passive = Value;

  val labelPrefix = "psi-typeopt";
  val labelShortPrefix = "psi-typeopt-short";

  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
  def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
}
