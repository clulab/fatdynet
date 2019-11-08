package org.clulab.fatdynet.apps

import java.io.File

import edu.cmu.dynet.Dim
import edu.cmu.dynet.Initialize
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelLoader
import org.clulab.fatdynet.utils.CloseableZipModelLoader
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Zipper
import org.clulab.fatdynet.utils.Closer.AutoCloser

object ZipApp {

  def main(args: Array[String]) {
    Initializer.initialize(Map("random-seed" -> 2522620396L))

    val filename = "model.rnn"
    val zipname = "model.jar"
    val key = "/key"

    val lookupParameter = {
      // This is an extremely simple "model" that is only a loose lookupParameter.
      val parameterCollection = new ParameterCollection()
      val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

      lookupParameter
    }

    // Save the model into a "raw" file.
    new CloseableModelSaver(filename).autoClose { modelSaver =>
      modelSaver.addLookupParameter(lookupParameter, key)
    }

    // Convert the raw file into a jar/zip file.
    Zipper.zip(filename, zipname)

    // Read it back from the raw file in the dynet way.
    val rawLookupParameter1 = new CloseableModelLoader(filename).autoClose { modelLoader =>
     // This requires you to know the dimension details and key in advance.
      val parameterCollection = new ParameterCollection()
      val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

      modelLoader.populateLookupParameter(lookupParameter, key)
      lookupParameter
    }

    // Read it back from the zip file in the dynet way.
    val zipLookupParameter1 = new CloseableZipModelLoader(filename, zipname).autoClose { modelLoader =>
      // This requires you to know the dimension details and key in advance.
      val parameterCollection = new ParameterCollection()
      val lookupParameter = parameterCollection.addLookupParameters(10, Dim(10))

      modelLoader.populateLookupParameter(lookupParameter, key)
      lookupParameter
    }

    // Alternatively, without needing to know the dimensions and maybe not even the key
    // you can do this:

    // Read it back from the raw file in the fatdynet way.
    val rawLookupParameter2 = {
      // Indicate the repository to be used.
      val repo = Repo(filename)
      // Analyze it for designs, the structure.
      val designs = repo.getDesigns()
      // Retrieve the model, the data, by key.
      //val model = repo.getModel(designs, key)
      // Retrieve by index, which default to 0.
      val model = repo.getModel(designs)
      require(model.name == key) // just checking
      val lookupParameter = model.getLookupParameter()

      lookupParameter
    }

    // Read it back from the zip file in the fatdynet way.
    val zipLookupParameter2 = {
      // Indicate the repository to be used and the zip file that contains it.
      val repo = Repo(filename, zipname)
      // Analyze it for designs, the structure.
      val designs = repo.getDesigns()
      // Retrieve the model, the data, by key.
      //val model = repo.getModel(designs, key)
      val model = repo.getModel(designs)
      require(model.name == key)
      require(model.name == key) // just checking
      val lookupParameter = model.getLookupParameter()

      lookupParameter
    }

    new File(filename).delete
    new File(zipname).delete
  }
}
