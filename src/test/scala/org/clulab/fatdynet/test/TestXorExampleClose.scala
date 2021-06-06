package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.apps.XorExampleCloseApp

import java.io.File

class TestXorExampleClose extends FatdynetTest {

  behavior of "XorExampleClose"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        // Do not run main, because it cleans up.
        XorExampleCloseApp.run(Array[String]())
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