package org.clulab.fatdynet.test

import java.io.File

import org.clulab.fatdynet.apps.PairExampleApp
import org.scalatest._

class TestPairExample extends FlatSpec with Matchers {

  behavior of "PairExample"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        PairExampleApp.main(Array[String]())
        new File("PairModel.dat").delete
      }
      catch {
        case exception: Throwable =>
          exception.printStackTrace()
          throw exception
      }
   }
  }
}
