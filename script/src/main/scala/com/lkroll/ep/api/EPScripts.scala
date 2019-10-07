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

import com.lkroll.roll20.core._
import com.lkroll.roll20.api._
import com.lkroll.roll20.api.conf._
import com.lkroll.roll20.api.templates._
import com.lkroll.roll20.api.facade.Roll20API
import com.lkroll.ep.model.{EPCharModel => epmodel}
import scalajs.js
import scalajs.js.JSON
import fastparse.all._
import util.{Failure, Success, Try}

object EPScripts extends APIScriptRoot {

  override lazy val outputTemplate: Option[TemplateRef] = epmodel.outputTemplate.map(_.ref);

  override def children: Seq[APIScript] =
    Seq(RollsScript,
        TokensScript,
        GroupRollsScript,
        GMTools,
        CharTools,
        compendium.CompendiumScript,
        BattleManagerScript,
        CharCleanerScript);

  val version = BuildInfo.version;
  val author = "Lars Kroll";
  val email = "bathtor@googlemail.com";
  val emailTag = s"&lt;$email&gt;";
  val repository = "https://github.com/Bathtor/EPSheet/tree/master/script";
  val repoLink = s"[Github]($repository)";

  onReady {
    info(s"EPScripts v${version} loaded!");
  }

  def checkVersion(char: Character): Result[Unit] = {
    char.attributeValue(epmodel.versionField) match {
      case Some(version) =>
        if (version == epmodel.version()) {
          Ok(())
        } else {
          Err(
            s"The character sheet for ${char.name} does not have a matching model version (${version} vs ${epmodel.version()})!"
          )
        }
      case None =>
        Err(s"Can't verify that character sheet for ${char.name} has matching model version! Skipping token.")
    }
  }
}
