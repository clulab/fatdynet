package org.clulab.fatdynet.test

import java.io.File

import edu.cmu.dynet.Dim
import edu.cmu.dynet.Initialize
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ModelLoader
import edu.cmu.dynet.ModelSaver
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.ZipModelLoader
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.Zipper
import org.scalatest._

class TestUnicode extends FlatSpec with Matchers {

  def newModel(): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

    lookupParameter
  }

  def save(filename: String, lookupParameter: LookupParameter, key: String): Unit = {
    val saver = new ModelSaver(filename)

    saver.addLookupParameter(lookupParameter, key)
    saver.done()
  }

  def loadRaw(filename: String, key: String): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))
    val loader = new ModelLoader(filename)

    loader.populateLookupParameter(lookupParameter, key)
    loader.done()
    lookupParameter
  }

  def loadZip(filename: String, zipname: String, key: String): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))
    val loader = new ZipModelLoader(filename, zipname)

    loader.populateLookupParameter(lookupParameter, key)
    loader.done()
    lookupParameter
  }

  Initialize.initialize(Map("random-seed" -> 2522620396L))

  behavior of "model with Unicode name"

  it should "be found in files" in {
    val filename = "model.rnn"
    val zipname = "model.jar"
    val key = "/\u03b1\u03b2\u03b3"
    val model = newModel()

    save(filename, model, key)
    Zipper.zip(filename, zipname)

    val rawModel = loadRaw(filename, key)
    val zipModel = loadZip(filename, zipname, key)

    {
      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val exists = designs.exists { design =>
        design.name == key
      }

      exists should be (true)
    }

    {
      val repo = Repo(filename, zipname)
      val designs = repo.getDesigns()
      val exists = designs.exists { design =>
        design.name == key
      }

      exists should be (true)
    }

    new File(filename).delete
    new File(zipname).delete
  }
}
