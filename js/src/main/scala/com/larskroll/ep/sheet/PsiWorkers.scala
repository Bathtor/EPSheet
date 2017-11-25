package com.larskroll.ep.sheet

import com.larskroll.roll20.facade.Roll20;
import com.larskroll.roll20.facade.Roll20.EventInfo;
import com.larskroll.roll20.sheet._
import SheetWorkerTypeShorthands._
import util.{ Success, Failure }
import concurrent.{ Future, Promise, ExecutionContext }
import scala.scalajs.js

object PsiWorkers extends SheetWorker {
  import EPCharModel._

  val psiSustainedCalc = bind(op(psiCurrentSustained)) update {
    case (sustained) => {
      Seq(
        psiSustainedMod <<= -sustained * 10)
    }
  }

  val psiChiTypeCalc = bind(op(psiChi.psiType)) update {
    case (ptName) => {
      import PsiType._

      val pt = withName(ptName);
      val ptLabel = dynamicLabelShort(pt);
      Seq(psiChi.psiTypeShort <<= ptLabel)
    }
  }

  val psiGammaTypeCalc = bind(op(psiGamma.psiType)) update {
    case (ptName) => {
      import PsiType._

      val pt = withName(ptName);
      val ptLabel = dynamicLabelShort(pt);
      Seq(psiGamma.psiTypeShort <<= ptLabel)
    }
  }
}
