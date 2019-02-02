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

import scalajs.js
import util.{ Try, Success, Failure }
import com.lkroll.ep.model._
import scala.collection.Seq
import scala.scalajs.js.Any.jsArrayOps
import scala.reflect.ClassTag

object ValueParsers {
  implicit class Sequenceable[V: ClassTag](arr: Array[Try[V]]) {
    def sequence: Try[Array[V]] = {
      arr.foldLeft(Try(Array.empty[V])) { (acc, tv) =>
        acc match {
          case Success(a) => tv match {
            case Success(v) => Success(a :+ v)
            case Failure(e) => Failure(e)
          }
          case f: Failure[Array[V]] => f
        }
      }
    }
  }

  def aptitudesFrom(sraw: String): Try[AptitudeValues] = {
    val s = sraw.trim();
    val r = Try {
      if (s.isEmpty()) {
        Success(Aptitude.defaultValues)
      } else if (s.startsWith("{")) {
        //EPWorkers.log(s"Trying to parse $s as json.");
        val res = js.JSON.parse(s);
        //EPWorkers.log(s"Got dynamic: $res.");
        //        val value: Any = PicklerRegistry.unpickle(res);
        //        EPWorkers.log(s"Got value: $res.");

        val cog = dynamicToOption[Int](res.cog).map(_.toInt);
        val coo = dynamicToOption[Int](res.coo).map(_.toInt);
        val int = dynamicToOption[Int](res.int).map(_.toInt);
        val ref = dynamicToOption[Int](res.ref).map(_.toInt);
        val sav = dynamicToOption[Int](res.sav).map(_.toInt);
        val som = dynamicToOption[Int](res.som).map(_.toInt);
        val wil = dynamicToOption[Int](res.wil).map(_.toInt);
        Success(AptitudeValues(cog, coo, int, ref, sav, som, wil))
      } else {
        //EPWorkers.log(s"Trying to parse $s as comma separated string.");
        val commaSplit = s.split(',').map(_.trim());
        if (commaSplit.length == 1) {
          //EPWorkers.log(s"Split is of length 1: ${commaSplit(0)}");
          val iT = Try(commaSplit(0).toInt);
          val aT = iT.map { i =>
            //EPWorkers.log(s"Single parsing successful: $i");
            val aptsMap = Aptitude.values.map(a => (a -> i)).toMap;
            val apts = Aptitude.valuesFrom(aptsMap);
            //EPWorkers.log(s"Aptitudes: ${aptsMap.mkString(",")} -> $apts");
            apts;
          };
          aT
        }
        val parsed = commaSplit.zipWithIndex.map {
          case (in, index) =>
            //EPWorkers.log(s"Mapping $index -> ${in}");
            val oT = Try((Aptitude(index) -> in.toInt)).recoverWith {
              case _ =>
                val spaceSplit = in.split(" ");
                //EPWorkers.log(s"Wasn't just a number: ${spaceSplit.mkString(",")}");
                if (spaceSplit.length == 2) {
                  for {
                    i <- Try(spaceSplit(0).toInt);
                    a <- Try(aptFrom(spaceSplit(1).toUpperCase()))
                  } yield (a -> i)
                } else {
                  Failure(new IllegalArgumentException(in))
                }
            }
            oT
        };
        parsed.sequence.map(p => Aptitude.valuesFrom(p.toMap))
      }
    };
    r.flatten
  }

  def aptFrom(s: String): Aptitude.Aptitude = Aptitude.withName(s);

  private lazy val skillPattern = {
    val mod = """([+-]\d+)""";
    val words = """(\w(?:(?:\w| )*\w)?)""";
    val whitespace = """\s""";
    val wordsInParenMaybe = """(?:\s\(""" + words + """\))?""";
    val skillMaybe = """(?:\sskill)?""";

    val expr = mod + whitespace + words + wordsInParenMaybe + skillMaybe;
    expr.r
  }

  def skillsFrom(sraw: String): Try[Seq[SkillMod]] = {
    val s = sraw.trim;
    val r = Try {
      if (s.isEmpty()) {
        Success(Seq.empty)
      } else if (s.startsWith("{")) {
        //EPWorkers.log(s"Trying to parse $s as single json object.");
        val res = js.JSON.parse(s);
        //EPWorkers.log(s"Got dynamic: $res.");
        Success(skillFromJson(res).toSeq)
      } else if (s.startsWith("[")) {
        //EPWorkers.log(s"Trying to parse $s as json list.");
        val res = js.JSON.parse(s);
        //EPWorkers.log(s"Got dynamic: $res.");
        dynamicToOption[js.Array[js.Dynamic]](res) match {
          case Some(data) => {
            Success(data.flatMap(skillFromJson).toSeq)
          }
          case None => Failure(new ParsingException("Object was not an array")) //EPWorkers.error("Object was not an array!"); Seq.empty
        }
      } else {
        val commaSplit = s.split(",");
        val res = commaSplit.flatMap { group =>
          group.trim match {
            case skillPattern(mod, skill, null)  => Some(SkillMod(removeFinalSkill(skill), None, mod.toInt))
            case skillPattern(mod, skill, field) => Some(SkillMod(removeFinalSkill(skill), Some(field), mod.toInt))
            case _                               => println(s"Extracted nothing from: $group"); None
          }
        };
        Success(res.toSeq)
      }
    };
    r.flatten
  }

  private lazy val finalSkillPattern = """(.*)\sskill""".r;

  private def removeFinalSkill(s: String): String = {
    s match {
      case finalSkillPattern(x) => x
      case x                    => x
    }
  }

  def skillFromJson(res: js.Dynamic): Option[SkillMod] = {
    for {
      skill <- dynamicToOption[String](res.skill);
      field <- Some(dynamicToOption[String](res.field));
      mod <- dynamicToOption[Int](res.mod)
    } yield SkillMod(skill, field, mod)
  }

  private def dynamicToOption[T](d: Dynamic): Option[T] = if (js.isUndefined(d)) { None } else { Some(d.asInstanceOf[T]) }
}

class ParsingException(message: String) extends Exception(message) {

  def this(message: String, cause: Throwable) {
    this(message)
    initCause(cause)
  }

  def this(cause: Throwable) {
    this(Option(cause).map(_.toString).orNull, cause)
  }

  def this() {
    this(null: String)
  }
}
