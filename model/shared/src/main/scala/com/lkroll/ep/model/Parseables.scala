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

package com.lkroll.ep.model

object Parseables {}

object Aptitude extends Enumeration {

  type Aptitude = Value

  val COG, COO, INT, REF, SAV, SOM, WIL = Value

  def valuesFrom(m: Map[Aptitude, Int]): AptitudeValues = {
    AptitudeValues(m.get(COG), m.get(COO), m.get(INT), m.get(REF), m.get(SAV), m.get(SOM), m.get(WIL))
  }
  def defaultValues: AptitudeValues = AptitudeValues(None, None, None, None, None, None, None)
}

case class AptitudeValues(cog: Option[Int],
                          coo: Option[Int],
                          int: Option[Int],
                          ref: Option[Int],
                          sav: Option[Int],
                          som: Option[Int],
                          wil: Option[Int]);

case class SkillMod(skill: String, field: Option[String], mod: Int);
