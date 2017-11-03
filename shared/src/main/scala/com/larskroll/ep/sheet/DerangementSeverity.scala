package com.larskroll.ep.sheet

object DerangementSeverity extends Enumeration {
  type DerangementSeverity = Value;

  val Minor, Moderate, Major = Value;

  val labelPrefix = "derangement-severity-opt";
  val labelShortPrefix = "derangement-severity-opt-short";

  def dynamicLabel(v: Value): String = s"${labelPrefix}-${v.toString()}";
  def dynamicLabelShort(v: Value): String = s"${labelShortPrefix}-${v.toString()}";
}
