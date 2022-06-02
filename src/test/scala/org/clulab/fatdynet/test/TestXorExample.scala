package org.clulab.fatdynet.test

import java.io.File
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.apps.XorExampleApp
import org.clulab.fatdynet.synchronizers.Synchronizer

class TestXorExample extends FatdynetTest {

  behavior of "XorExample"

  it should "not throw an exception" in {
    if (Synchronizer.canTrain) {
      noException should be thrownBy {
        try {
          // Do not run main, because it cleans up.
          XorExampleApp.run(Array[String]())
          new File("XorModel.dat").delete
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
