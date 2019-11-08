package org.clulab.dynet.apps

import org.clulab.dynet.models.hot.scala.HotModel
import org.clulab.fatdynet.utils.CloseableModelLoader
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.CloseableZipModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Zipper

object ModelLoaderApp extends App {

  Initializer.initialize(Map("random-seed" -> 2522620396L, "dynet-mem" -> "2048"))

  val origFilename = "model.rnn"
  val zipFilename = "model.jar"
  val copyFromTextFilename = "modelTextCopy.rnn"
  val copyFromZipFilename = "modelZipCopy.rnn"
  val key = "/key"

  new CloseableModelSaver(origFilename).autoClose { saver =>
    val model = HotModel()

    saver.addModel(model.parameters, key)
  }

  Zipper.zip(origFilename, zipFilename)

  val copyFromTextModel = {
    // This will be different from the original because of random initialization.
    new CloseableModelLoader(origFilename).autoClose { loader =>
      val model = HotModel()

      loader.populateModel(model.parameters, key)
      model
    }
  }
  val copyFromZipModel = {
    // This will be different from the original because of random initialization.
    new CloseableZipModelLoader(origFilename, zipFilename).autoClose { loader =>
      val model = HotModel()

      loader.populateModel(model.parameters, key)
      model
    }
  }

  new CloseableModelSaver(copyFromTextFilename).autoClose { saver =>
    saver.addModel(copyFromTextModel.parameters, key)
  }
  new CloseableModelSaver(copyFromZipFilename).autoClose { saver =>
    saver.addModel(copyFromZipModel.parameters, key)
  }
}
