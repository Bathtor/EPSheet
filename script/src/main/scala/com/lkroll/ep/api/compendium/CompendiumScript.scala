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
import com.lkroll.roll20.api.conf._
import java.io.{ ByteArrayOutputStream, ByteArrayInputStream }
import java.util.Base64;
//import java.util.zip.{ GZIPOutputStream, GZIPInputStream }

object CompendiumScript extends APIScript {
  override def apiCommands: Seq[APICommand[_]] = Seq(EPCompendiumDataCommand);
}

class EPCompoendiumDataConf(args: Seq[String]) extends ScallopAPIConf(args) {

  val needle = opt[String]("needle");
  verify();
}

object EPCompendiumDataCommand extends APICommand[EPCompoendiumDataConf] {
  import APIImplicits._;
  override def command = "epcompendium-data";
  override def options = (args) => new EPCompoendiumDataConf(args);
  override def apply(config: EPCompoendiumDataConf, ctx: ChatContext): Unit = {
    if (config.needle.isSupplied) {
      val needle = config.needle();
      val needleB = needle.getBytes("UTF-8");
      val bos = new ByteArrayOutputStream(needleB.length);
      //val gzip = new GZIPOutputStream(bos)
      //gzip.write(needleB);
      //gzip.close();
      ctx.reply("Meh fuck this")
    } else {
      ctx.reply("No needle supplied :(");
    }
  }
}
