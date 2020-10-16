package org.clulab.fatdynet.test

import java.io.File

import edu.cmu.dynet.Dim
import edu.cmu.dynet.ParameterCollection
import org.clulab.dynet.Test
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Zipper

class TestRepoDelete extends Test {

  behavior of "Repo"

  it should "be deleted successfull" in {
    Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

    val filename = "model.rnn"
    val zipname = "model.jar"
    val key = "/key"

    val lookupParameter = {
      val parameterCollection = new ParameterCollection()
      val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

      lookupParameter
    }

    new CloseableModelSaver(filename).autoClose { modelSaver =>
      modelSaver.addLookupParameter(lookupParameter, key)
    }

    Zipper.zip(filename, zipname)

    val repo = Repo(filename, zipname)
    val designs = repo.getDesigns()
    val model = repo.getModel(designs)

    require(model.name == key)
    model.getLookupParameter()

    new File(filename).delete should be (true)
    new File(zipname).delete should be (true)
  }
}
