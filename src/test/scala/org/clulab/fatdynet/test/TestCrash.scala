package org.clulab.fatdynet.test

import java.io.File

import edu.cmu.dynet._

import org.scalatest.TestSuite

import scala.io.Source

class TestCrash extends TestSuite {
  Initialize.initialize(Map("random-seed" -> 2522620396L, "dynet-mem" -> "2048"))

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
    val oldParametersList = oldParameterCollection.parametersList
  }

  def makeLookupParameter = {
    val oldParameterCollection = new ParameterCollection()
    val oldLookupParameters = oldParameterCollection.addLookupParameters(1234, Dim(300))
    // Must do this twice
    asString(oldLookupParameters, "/name")
    asString(oldLookupParameters, "/name")
  }

  // Must be in this order
  makeParameter
  // Only works if these are lookup parameters
  makeLookupParameter
}
