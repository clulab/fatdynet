package org.clulab.fatdynet.test

import java.io.File

import edu.cmu.dynet._
import org.scalatest._

import scala.io.Source

class TestCrash extends FlatSpec with Matchers {
  Initialize.initialize(Map("random-seed" -> 2522620396L, "dynet-mem" -> "2048"))

  def asString(lookupParameter: LookupParameter, name: String): Unit = {
    val tmpFile = File.createTempFile("model-", ".fatdynet")
    val filename = tmpFile.getCanonicalPath

    val modelSaver = new ModelSaver(filename)
    modelSaver.addLookupParameter(lookupParameter, name)
    modelSaver.done()

    val string = Source.fromFile(filename).mkString // This seems critical
    tmpFile.delete()
  }

  def testNamedParameter = {
    val oldParameterCollection = new ParameterCollection()
    val oldParameter = oldParameterCollection.addParameters(Dim(51))
    val oldParametersList = oldParameterCollection.parametersList
  }

  def testNamedLookupParameter = {
    val oldParameterCollection = new ParameterCollection()
    val oldLookupParameters = oldParameterCollection.addLookupParameters(1234, Dim(300))
    asString(oldLookupParameters, "/name")
    asString(oldLookupParameters, "/name")
  }

  // Must be in this order
  testNamedParameter
  // Only works if these are lookup parameters
  testNamedLookupParameter
}
