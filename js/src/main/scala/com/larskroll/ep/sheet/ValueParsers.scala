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

package com.larskroll.ep.sheet

import scalajs.js
import util.{ Try, Success, Failure }

object ValueParsers {
  def aptitudesFrom(s: String): AptitudeValues = {
    try {
      if (s.startsWith("{")) {
        EPWorkers.log(s"Trying to parse $s as json.");
        val res = js.JSON.parse(s);
        EPWorkers.log(s"Got dynamic: $res.");
        //        val value: Any = PicklerRegistry.unpickle(res);
        //        EPWorkers.log(s"Got value: $res.");

        val cog = dynamicToOption[Int](res.cog);
        val coo = dynamicToOption[Int](res.coo);
        val int = dynamicToOption[Int](res.int);
        val ref = dynamicToOption[Int](res.ref);
        val sav = dynamicToOption[Int](res.sav);
        val som = dynamicToOption[Int](res.som);
        val wil = dynamicToOption[Int](res.wil);
        return AptitudeValues(cog, coo, int, ref, sav, som, wil)
      } else {
        EPWorkers.log(s"Trying to parse $s as comma separated string.");
        val commaSplit = s.split(',').map(_.trim());
        if (commaSplit.length == 1) {
          EPWorkers.log(s"Split is of length 1: ${commaSplit(0)}");
          val iT = Try(commaSplit(0).toInt);
          val aT = iT.map { i =>
            EPWorkers.log(s"Single parsing successful: $i");
            val aptsMap = Aptitude.values.map(a => (a -> i)).toMap;
            val apts = Aptitude.valuesFrom(aptsMap);
            EPWorkers.log(s"Aptitudes: ${aptsMap.mkString(",")} -> $apts");
            apts;
          };
          if (aT.isSuccess) {
            return aT.get
          }
        }
        val parsed = commaSplit.zipWithIndex.map {
          case (in, index) =>
            EPWorkers.log(s"Mapping $index -> ${in}");
            val oT = Try((Aptitude(index) -> in.toInt)).recoverWith {
              case _ =>
                val spaceSplit = in.split(" ");
                EPWorkers.log(s"Wasn't just a number: ${spaceSplit.mkString(",")}");
                if (spaceSplit.length == 2) {
                  for {
                    i <- Try(spaceSplit(0).toInt);
                    a <- Try(aptFrom(spaceSplit(1).toUpperCase()))
                  } yield (a -> i)
                } else {
                  Failure(new IllegalArgumentException(in))
                }
            }
            if (oT.isFailure) {
              EPWorkers.error(oT.toString)
            }
            oT.toOption
        };
        return Aptitude.valuesFrom(parsed.flatten.toMap)
      }
    } catch {
      case e: Throwable => EPWorkers.error(e);
    }
    return AptitudeValues(None, None, None, None, None, None, None);
  }

  private def aptFrom(s: String): Aptitude.Aptitude = Aptitude.withName(s);

  private def dynamicToOption[T](d: Dynamic): Option[T] = if (js.isUndefined(d)) { None } else { Some(d.asInstanceOf[T]) }
}
