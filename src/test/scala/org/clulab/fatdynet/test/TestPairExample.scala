package org.clulab.fatdynet.test


import java.io.File
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.apps.PairExampleApp
import org.clulab.fatdynet.synchronizers.Synchronizer

class TestPairExample extends FatdynetTest {

  behavior of "PairExample"

  it should "not throw an exception" in {
    if (Synchronizer.canTrain) {
      noException should be thrownBy {
        try {
          PairExampleApp.run(Array[String]())
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
}
