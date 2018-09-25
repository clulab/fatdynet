package org.clulab.fatdynet

import edu.cmu.dynet.examples.XorScala
import org.scalatest._

class TestXorScala extends FlatSpec with Matchers {

  behavior of "XorScala"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        XorScala.main(Array[String]())
      }
      catch {
        case exception: Throwable => exception.printStackTrace; throw exception
      }
   }
  }
}
