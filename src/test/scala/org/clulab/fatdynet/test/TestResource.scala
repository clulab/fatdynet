package org.clulab.fatdynet.test

import org.clulab.fatdynet.utils.ZipTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.scalatest._

import java.util.zip.ZipFile
import scala.io.Source

class TestResource extends FlatSpec with Matchers {

  behavior of "Resource"

  it should "be extractable from a jar file" in {
    // sbt does not create a jar file for testing.  The resource is never zipped.
    // For manual testing, use a file that is part of a dependency like scalatest.

    // This file is part of scalatest
//    val resourceName = "org/scalatest/ScalaTestBundle.properties"
    val resourceName = "resource.txt"
    val (jarFileName, zipped) = ZipTextModelLoader.getResourceFileNameAndZipped(resourceName, this)
    println(jarFileName)
    val source =
        if (zipped) {
          val zipFile = new ZipFile(jarFileName)
          val entry = zipFile.getEntry(resourceName)
          val inputStream = zipFile.getInputStream(entry)

          Source.fromInputStream(inputStream)
        }
        else
          Source.fromFile(jarFileName)
    val contents = source.autoClose(_.mkString)

//    println(contents)
    contents should not be empty
  }
}
