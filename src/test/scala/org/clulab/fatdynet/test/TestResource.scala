package org.clulab.fatdynet.test

import org.clulab.fatdynet.utils.ZipTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.scalatest._

import scala.io.Source

class TestResource extends FlatSpec with Matchers {

  behavior of "Resource"

  it should "be extractable from a jar file" in {
    // sbt does not create a jar file for testing.  The resource is never zipped.
    val (jarFileName, zipped) = ZipTextModelLoader.getResourceFileName("resource.txt", this)
    val source =
        if (zipped)
          Source.fromResource(jarFileName) // How to read the zip file?
        else
          Source.fromFile(jarFileName)
    val text = source.autoClose(_.mkString)

    text.trim should be ("This is a test resource.")
  }
}
