package org.clulab.fatdynet.test

import java.io.File
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Zipper

class TestZipper extends FatdynetTest {

  behavior of "Zipper"

  it should "both create and delete files rapidly" in {
    val filename1 = "README.md"
    val filename2 = "build.sbt"
    val zipname = "zipper.zip"

    0.until(10).foreach { i =>
      Zipper.zip(filename1, zipname)
      Zipper.zip(filename2, zipname)
      new File(zipname).delete should be (true)
    }
  }
}
