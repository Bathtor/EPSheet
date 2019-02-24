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
import com.lkroll.ep.model.{ EPCharModel => epmodel, GearSection }
import APIImplicits._;

case class GearImport(g: Gear) extends Importable {
  override def updateLabel: String = g.name;
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val rowId = Some(idPool.generateRowId());
    char.createRepeating(GearSection.itemName, rowId) <<= g.name;
    char.createRepeating(GearSection.amount, rowId) <<= 1;
    char.createRepeating(GearSection.description, rowId) <<= g.descr;
    Left("Ok")
  }
}

case class GearEntryImport(e: GearEntry) extends Importable {
  override def updateLabel: String = e.item.name;
  override def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String] = {
    val g = e.item;
    val rowId = Some(idPool.generateRowId());
    char.createRepeating(GearSection.itemName, rowId) <<= g.name;
    char.createRepeating(GearSection.amount, rowId) <<= e.count;
    char.createRepeating(GearSection.description, rowId) <<= g.descr;
    Left("Ok")
  }
}
