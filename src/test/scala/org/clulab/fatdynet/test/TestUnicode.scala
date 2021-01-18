package org.clulab.fatdynet.test

import java.io.File
import edu.cmu.dynet.Dim
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelLoader
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.CloseableZipModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Zipper

class TestUnicode extends FatdynetTest {

  def newModel(): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

    lookupParameter
  }

  def save(filename: String, lookupParameter: LookupParameter, key: String): Unit = {
    new CloseableModelSaver(filename).autoClose { saver =>
      saver.addLookupParameter(lookupParameter, key)
    }
  }

  def loadRaw(filename: String, key: String): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

    new CloseableModelLoader(filename).autoClose { loader =>
      loader.populateLookupParameter(lookupParameter, key)
      lookupParameter
    }
  }

  def loadZip(filename: String, zipname: String, key: String): LookupParameter = {
    val parameterCollection = new ParameterCollection()
    val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

    new CloseableZipModelLoader(filename, zipname).autoClose { loader =>
      loader.populateLookupParameter(lookupParameter, key)
      lookupParameter
    }
  }

  Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

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
