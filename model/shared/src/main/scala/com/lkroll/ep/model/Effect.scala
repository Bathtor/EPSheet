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

sealed trait Effect {
  def text: String;
}

object Effect {
  def modToString(i: Int): String = if (i > 0) s"+$i" else i.toString;
}
import Effect._

case class SpeedMod(mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} SPD";
}
case class MOAMod(mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} MOA";
}
case class IniMod(mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} INI";
}
case class AptitudeMod(apt: Aptitude.Aptitude, mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} $apt";
}
case class SkillMod(skill: String, field: Option[String] = None, mod: Int) extends Effect {
  def text: String = field match {
    case Some(f) => s"${modToString(mod)} $skill ($f) skill"
    case None    => s"${modToString(mod)} $skill skill"
  }
}
case class DurMod(mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} DUR";
}
case class IgnoreWounds(n: Int) extends Effect {
  override def text: String = s"Ignore modifiers from $n wounds";
}
case class IgnoreTraumas(n: Int) extends Effect {
  override def text: String = s"Ignore modifiers from $n traumas";
}
case class LucMod(mod: Int) extends Effect {
  override def text: String = s"${modToString(mod)} LUC";
}
case class FreeForm(text: String) extends Effect
