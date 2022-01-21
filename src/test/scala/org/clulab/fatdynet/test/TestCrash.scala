package org.clulab.fatdynet.test

import java.io.File
import edu.cmu.dynet._
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer

import scala.io.Source

class TestCrash extends FatdynetTest {
  Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L, Initializer.DYNET_MEM -> "2048"))

  def asString(lookupParameter: LookupParameter, name: String): Unit = {
    val tmpFile = File.createTempFile("model-", ".fatdynet")
    val filename = tmpFile.getCanonicalPath

    val modelSaver = new ModelSaver(filename)
    modelSaver.addLookupParameter(lookupParameter, name)
    modelSaver.done()

    val string = Source.fromFile(filename).mkString
    tmpFile.delete()
  }

  def makeParameter = {
    val oldParameterCollection = new ParameterCollection()
    val oldParameter = oldParameterCollection.addParameters(Dim(51))
    val oldParametersList = oldParameterCollection.parametersList()
  }

  def makeLookupParameter = {
    val oldParameterCollection = new ParameterCollection()
    val oldLookupParameters = oldParameterCollection.addLookupParameters(1234, Dim(300))
    // Must do this twice
    asString(oldLookupParameters, "/name")
    asString(oldLookupParameters, "/name")
  }

  it should "do something" in {
    // This activates beforeAll and afterAll.
  }

  // Must be in this order
  makeParameter
  // Only works if these are lookup parameters
  makeLookupParameter
}
