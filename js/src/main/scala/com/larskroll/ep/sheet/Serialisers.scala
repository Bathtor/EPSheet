package com.larskroll.ep.sheet

import scalajs.js
import com.larskroll.roll20.sheet._

object ReporderSer extends Serialiser[Array[String]] {
  override def serialise(o: Array[String]): js.Any = o.mkString(",");
}
