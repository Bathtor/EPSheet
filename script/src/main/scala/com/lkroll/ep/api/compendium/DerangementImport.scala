/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lars Kroll <bathtor@googlemail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.lkroll.ep.api.compendium

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.ep.compendium._
import com.lkroll.ep.compendium.utils.OptionPickler._
import com.lkroll.ep.model.{ EPCharModel => epmodel, DerangementSection, DerangementSeverity => ModelSeverity }
import APIImplicits._;

case class DerangementImport(d: Derangement, duration: Float) extends Importable {

  implicit def s2mds(s: Severity): ModelSeverity.DerangementSeverity = s match {
    case Severity.Minor    => ModelSeverity.Minor
    case Severity.Moderate => ModelSeverity.Moderate
    case Severity.Major    => ModelSeverity.Major
  };

  override def updateLabel: String = s"${d.name} derangement";
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());
    char.createRepeating(DerangementSection.conditionName, rowId) <<= d.name;
    char.createRepeating(DerangementSection.severity, rowId) <<= s2mds(d.severity).toString();
    char.createRepeating(DerangementSection.description, rowId) <<= d.descr;
    char.createRepeating(DerangementSection.duration, rowId) <<= duration;
    Left("Ok")
  }
}
