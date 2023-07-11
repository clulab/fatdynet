package org.clulab.fatdynet.test


import java.io.File
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.apps.PairExampleApp

class TestPairExample extends FatdynetTest {

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
