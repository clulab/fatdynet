package org.clulab.fatdynet.test

import java.util.zip.ZipFile

import org.clulab.dynet.Test
import org.clulab.fatdynet.utils.BaseTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser

import scala.io.Source

class TestResource extends Test {

  behavior of "Resource"

  it should "be extractable from a jar file" in {
    // sbt does not create a jar file for testing.  The resource is never zipped.
    // For manual testing, use a file that is part of a dependency like scalatest.

    // This file is part of scalatest
//    val resourceName = "org/scalatest/ScalaTestBundle.properties"
    val resourceName = "resource.txt"
    val resourceInfo = BaseTextModelLoader.getResourceInfo(resourceName, this)
    println(resourceInfo.resourceFilename)
    val source =
        if (resourceInfo.isZipped) {
          val zipFile = new ZipFile(resourceInfo.resourceFilename)
          val entry = zipFile.getEntry(resourceName)
          val inputStream = zipFile.getInputStream(entry)

          Source.fromInputStream(inputStream)
        }
        else
          Source.fromFile(resourceInfo.resourceFilename)
    val contents = source.autoClose(_.mkString)

//    println(contents)
    contents should not be empty
  }
}
