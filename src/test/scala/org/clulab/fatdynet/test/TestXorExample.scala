package org.clulab.fatdynet.test

import java.io.File
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.apps.XorExampleApp

class TestXorExample extends FatdynetTest {

  behavior of "XorExample"

  it should "not throw an exception" in {
    noException should be thrownBy {
      try {
        XorExampleApp.main(Array[String]())
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
