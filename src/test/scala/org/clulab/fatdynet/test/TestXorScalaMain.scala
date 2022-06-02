package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.examples.XorScala
import org.clulab.fatdynet.synchronizers.Synchronizer

class TestXorScalaMain extends FatdynetTest {

  behavior of "XorScala"

  it should "not throw an exception" in {
    if (Synchronizer.canTrain) {
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
}
