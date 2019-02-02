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

import com.lkroll.roll20.sheet.model._;
import com.lkroll.roll20.core._;

object IdentitiesSection extends RepeatingSection {
  import FieldImplicits._;

  implicit val ctx = this.renderingContext;

  def name = "identities";

  val identity = text("identity");
  val description = text("description");
  val credits = "credits".default(0).validIn(-999999999, 999999999, 1);
  val notes = text("notes");

  val atNameShort = "at_rep_name_short".default(EPTranslation.atRep.abbrKey);
  val atNameLong = "at_rep_name_long".default(EPTranslation.atRep.fullKey);
  val atRepScore = "at_rep_score".default(0).validIn(0, 99, 1);
  val atRepFavour1 = "at_rep_favour1".default(false);
  val atRepFavour2 = "at_rep_favour2".default(false);
  val atRepFavour3 = "at_rep_favour3".default(false);
  val atRepFavour4 = "at_rep_favour4".default(false);
  val atRepFavour5 = "at_rep_favour5".default(false);

  val cNameShort = "c_rep_name_short".default(EPTranslation.cRep.abbrKey);
  val cNameLong = "c_rep_name_long".default(EPTranslation.cRep.fullKey);
  val cRepScore = "c_rep_score".default(0).validIn(0, 99, 1);
  val cRepFavour1 = "c_rep_favour1".default(false);
  val cRepFavour2 = "c_rep_favour2".default(false);
  val cRepFavour3 = "c_rep_favour3".default(false);
  val cRepFavour4 = "c_rep_favour4".default(false);
  val cRepFavour5 = "c_rep_favour5".default(false);

  val eNameShort = "e_rep_name_short".default(EPTranslation.eRep.abbrKey);
  val eNameLong = "e_rep_name_long".default(EPTranslation.eRep.fullKey);
  val eRepScore = "e_rep_score".default(0).validIn(0, 99, 1);
  val eRepFavour1 = "e_rep_favour1".default(false);
  val eRepFavour2 = "e_rep_favour2".default(false);
  val eRepFavour3 = "e_rep_favour3".default(false);
  val eRepFavour4 = "e_rep_favour4".default(false);
  val eRepFavour5 = "e_rep_favour5".default(false);

  val fNameShort = "f_rep_name_short".default(EPTranslation.fRep.abbrKey);
  val fNameLong = "f_rep_name_long".default(EPTranslation.fRep.fullKey);
  val fRepScore = "f_rep_score".default(0).validIn(0, 99, 1);
  val fRepFavour1 = "f_rep_favour1".default(false);
  val fRepFavour2 = "f_rep_favour2".default(false);
  val fRepFavour3 = "f_rep_favour3".default(false);
  val fRepFavour4 = "f_rep_favour4".default(false);
  val fRepFavour5 = "f_rep_favour5".default(false);

  val gNameShort = "g_rep_name_short".default(EPTranslation.gRep.abbrKey);
  val gNameLong = "g_rep_name_long".default(EPTranslation.gRep.fullKey);
  val gRepScore = "g_rep_score".default(0).validIn(0, 99, 1);
  val gRepFavour1 = "g_rep_favour1".default(false);
  val gRepFavour2 = "g_rep_favour2".default(false);
  val gRepFavour3 = "g_rep_favour3".default(false);
  val gRepFavour4 = "g_rep_favour4".default(false);
  val gRepFavour5 = "g_rep_favour5".default(false);

  val iNameShort = "i_rep_name_short".default(EPTranslation.iRep.abbrKey);
  val iNameLong = "i_rep_name_long".default(EPTranslation.iRep.fullKey);
  val iRepScore = "i_rep_score".default(0).validIn(0, 99, 1);
  val iRepFavour1 = "i_rep_favour1".default(false);
  val iRepFavour2 = "i_rep_favour2".default(false);
  val iRepFavour3 = "i_rep_favour3".default(false);
  val iRepFavour4 = "i_rep_favour4".default(false);
  val iRepFavour5 = "i_rep_favour5".default(false);

  val rNameShort = "r_rep_name_short".default(EPTranslation.rRep.abbrKey);
  val rNameLong = "r_rep_name_long".default(EPTranslation.rRep.fullKey);
  val rRepScore = "r_rep_score".default(0).validIn(0, 99, 1);
  val rRepFavour1 = "r_rep_favour1".default(false);
  val rRepFavour2 = "r_rep_favour2".default(false);
  val rRepFavour3 = "r_rep_favour3".default(false);
  val rRepFavour4 = "r_rep_favour4".default(false);
  val rRepFavour5 = "r_rep_favour5".default(false);

  val uNameShort = "u_rep_name_short".default(EPTranslation.uRep.abbrKey);
  val uNameLong = "u_rep_name_long".default(EPTranslation.uRep.fullKey);
  val uRepScore = "u_rep_score".default(0).validIn(0, 99, 1);
  val uRepFavour1 = "u_rep_favour1".default(false);
  val uRepFavour2 = "u_rep_favour2".default(false);
  val uRepFavour3 = "u_rep_favour3".default(false);
  val uRepFavour4 = "u_rep_favour4".default(false);
  val uRepFavour5 = "u_rep_favour5".default(false);

  val xNameShort = "x_rep_name_short".default(EPTranslation.xRep.abbrKey);
  val xNameLong = "x_rep_name_long".default(EPTranslation.xRep.fullKey);
  val xRepScore = "x_rep_score".default(0).validIn(0, 99, 1);
  val xRepFavour1 = "x_rep_favour1".default(false);
  val xRepFavour2 = "x_rep_favour2".default(false);
  val xRepFavour3 = "x_rep_favour3".default(false);
  val xRepFavour4 = "x_rep_favour4".default(false);
  val xRepFavour5 = "x_rep_favour5".default(false);
}
