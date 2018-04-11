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

import com.lkroll.ep.compendium._
import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.ep.model.ActiveSkillSection

trait Importable {
  def updateLabel: String;
  def importInto(char: Character, idPool: RowIdPool, cache: ImportCache): Either[String, String];
  def children: List[Importable] = Nil;
}

object Importable {
  implicit def weapon2Import(w: Weapon): WeaponImport = WeaponImport(w);
  implicit def morph2Import(m: Morph): MorphImport = MorphImport(m);
}

class ImportCache(val char: Character) {

  private var activeSkillNameToId: Option[Map[String, String]] = None;

  private def loadActiveSkillMap(): Map[String, String] = {
    val fields = char.repeating(ActiveSkillSection.skillName);
    val m = fields.map(attr => (attr() -> attr.getRowId.get)).toMap;
    activeSkillNameToId = Some(m);
    m
  }

  def activeSkillId(name: String): Option[String] = {
    val data = activeSkillNameToId match {
      case Some(m) => m
      case None    => loadActiveSkillMap()
    };
    data.get(name)
  }
}
object ImportCache {
  def apply(char: Character): ImportCache = new ImportCache(char);
}
