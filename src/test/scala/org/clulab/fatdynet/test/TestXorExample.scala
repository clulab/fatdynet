package org.clulab.fatdynet.test

import org.clulab.fatdynet.apps.XorExampleApp
import org.scalatest._

class TestXorExample extends FlatSpec with Matchers {

  behavior of "XorExample"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        XorExampleApp.main(Array[String]())
      }
      catch {
        case exception: Throwable => exception.printStackTrace; throw exception
      }
   }
  }
}