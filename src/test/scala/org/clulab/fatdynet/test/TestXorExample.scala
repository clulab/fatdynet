package org.clulab.fatdynet.test

import java.io.File

import org.clulab.dynet.Test
import org.clulab.fatdynet.apps.XorExampleApp

class TestXorExample extends Test {

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
