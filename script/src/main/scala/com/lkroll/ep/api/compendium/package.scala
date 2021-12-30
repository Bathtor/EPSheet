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
package com.lkroll.ep.api

import scala.util.{Failure, Success, Try}
import scala.collection
import com.lkroll.ep.model.{AptitudeValues => ModelAptitudes, SkillMod => ModelSkillMod, MorphType => ModelMorphType};
import com.lkroll.ep.compendium.{AptitudeValues => CompendiumAptitudes, MorphType => CompendiumMorphType, Effect}

package object compendium {

  /*
   * Re-exports
   */
  type Result[T] = com.lkroll.common.result.Result[T, String];
  val Result = com.lkroll.common.result.Result;
  val Ok = com.lkroll.common.result.Ok;
  val Err = com.lkroll.common.result.Err;

  /*
   * Implicits
   */

  implicit class OptionOps[A](opt: Option[A]) {

    def toTry(msg: String): Try[A] = {
      opt.map(Success(_)).getOrElse(Failure(new NoSuchElementException(msg)))
    }

    def toTryOr(alt: => A): Try[A] = {
      opt.map(Success(_)).getOrElse(Success(alt))
    }
  }

  def toCompendiumApts(apts: ModelAptitudes): CompendiumAptitudes = {
    apts match {
      case ModelAptitudes(cog, coo, int, ref, sav, som, wil) =>
        CompendiumAptitudes(cog, coo, int, ref, sav, som, wil)
    }
  }

  def toCompendiumSkills(apts: collection.Seq[ModelSkillMod]): collection.Seq[Effect.SkillMod] = {
    apts.map {
      case ModelSkillMod(skill, field, mod) =>
        Effect.SkillMod(skill, field, mod)
    }
  }

  def toCompendiumMorphType(mt: ModelMorphType.MorphType): Option[CompendiumMorphType] = {
    import ModelMorphType._;
    mt match {
      case None       => Option.empty // since None is overloaded here
      case Synthmorph => Some(CompendiumMorphType.Synthmorph)
      case Biomorph   => Some(CompendiumMorphType.Biomorph)
      case Pod        => Some(CompendiumMorphType.Pod)
      case Infomorph  => Some(CompendiumMorphType.Infomorph)
    }
  }
}
