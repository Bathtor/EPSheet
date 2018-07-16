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

package com.lkroll.ep.sheet

import com.lkroll.roll20.sheet._
import com.lkroll.roll20.sheet.model._
import com.lkroll.roll20.core._
import com.lkroll.ep.model._

object EPUpdates extends MinorVersionUpdateManager {
  val model = EPCharModel;
  val old = OldModels;

  override def updateUnversioned(version: String): List[SheetWorkerOp] = List(
    nop update { _ => Seq(model.versionField <<= version, model.characterSheet <<= s"${model.sheetName} v$version") });

  override def onEveryVersionUpdate(newVersion: String): Seq[(FieldLike[Any], Any)] = {
    log(s"Updated to version $newVersion");
    Seq(model.versionField <<= newVersion, model.characterSheet <<= s"${model.sheetName} v$newVersion");
  }

  forVersion("1.4.0") {
    List(
      nameChange(old.V4.async, model.async))
  }
  forVersion("1.5.0") {
    List(
      nameChangeRepeating(model.morphs, old.V5.MorphSection.aptitudeMax, model.morphs.aptitudeMax))
  }
  forVersion("1.6.0") {
    val assignDMax = op(model.durability).update(dur => Seq(model.damageMax <<= dur));
    List(assignDMax)
  }
  forVersion("1.7.0") {
    val calc = nop { _: Option[Unit] => EPWorkers.chatOutputCalc().map(_ => ()) }; // workaround for initialisation timing
    List(calc)
  }
  forVersion("1.8.0") {
    val speedUpdate = op(model.speed).update(oldSpeed => {
      val extraSpeed = if (oldSpeed <= 1) { // if it's < 1 something is weird, but we shouldn't touch it
        0 // just leave everything as it is...morphSpeed will be 1 and speedExtra 0 so it'd sum to 1 anyway
      } else {
        oldSpeed - 1; // since 1 is coming from morphSpeed
      };
      Seq(model.speedExtra <<= extraSpeed)
    });
    val moaUpdate = op(model.mentalOnlyActions).update(oldMOA => {
      val extraMOA = if (oldMOA == 0) { // can't be < 0 since the input field doesn't allow that
        0 // just leave everything as it is...morphMOA will be 0 and moaExtra 0 so it'd sum to 0 anyway
      } else {
        oldMOA // since 0 is coming from morphMOA
      };
      Seq(model.mentalOnlyActionsExtra <<= oldMOA)
    });
    val assignSMax = op(model.lucidity).update(luc => Seq(model.stressMax <<= luc));
    List(speedUpdate, moaUpdate, assignSMax)
  }
}
