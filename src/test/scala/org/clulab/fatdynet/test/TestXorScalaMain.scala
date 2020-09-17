package org.clulab.fatdynet.test

import org.clulab.dynet.Test
import org.clulab.fatdynet.examples.XorScala

class TestXorScalaMain extends Test {

  behavior of "XorScala"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        XorScala.main(Array.empty[String])
      }
      catch {
        case exception: Throwable =>
          exception.printStackTrace()
          throw exception
      }
   }
  }
}
