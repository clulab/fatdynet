package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.scalatest._

import java.util.zip.ZipFile
import org.clulab.fatdynet.utils.BaseTextModelLoader

import scala.io.Source
import scala.util.Using

class TestResource extends FatdynetTest {

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
    val contents = Using.resource(source)(_.mkString)

//    println(contents)
    contents should not be empty
  }
}
