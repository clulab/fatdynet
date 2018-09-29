package org.clulab.fatdynet.test

import org.clulab.fatdynet.apps.XorScalaApp
import org.scalatest._

class TestXorScala extends FlatSpec with Matchers {

  behavior of "XorScala"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        XorScalaApp.main(Array[String]())
      }
      catch {
        case exception: Throwable => exception.printStackTrace; throw exception
      }
   }
  }
}