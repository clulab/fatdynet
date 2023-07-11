package org.clulab.dynet.apps

import org.clulab.dynet.models.hot.scala.HotModel
import org.clulab.fatdynet.utils.CloseableModelLoader
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.CloseableZipModelLoader
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Zipper

import scala.util.Using

object ModelLoaderApp extends App {

  Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L, Initializer.DYNET_MEM -> "2048"))

  val origFilename = "model.rnn"
  val zipFilename = "model.jar"
  val copyFromTextFilename = "modelTextCopy.rnn"
  val copyFromZipFilename = "modelZipCopy.rnn"
  val key = "/key"

  Using.resource(new CloseableModelSaver(origFilename)) { saver =>
    val model = HotModel()

    saver.addModel(model.parameters, key)
  }

  Zipper.zip(origFilename, zipFilename)

  val copyFromTextModel = {
    // This will be different from the original because of random initialization.
    Using.resource(new CloseableModelLoader(origFilename)) { loader =>
      val model = HotModel()

      loader.populateModel(model.parameters, key)
      model
    }
  }
  val copyFromZipModel = {
    // This will be different from the original because of random initialization.
    Using.resource(new CloseableZipModelLoader(origFilename, zipFilename)) { loader =>
      val model = HotModel()

      loader.populateModel(model.parameters, key)
      model
    }
  }

  Using.resource(new CloseableModelSaver(copyFromTextFilename)) { saver =>
    saver.addModel(copyFromTextModel.parameters, key)
  }
  Using.resource(new CloseableModelSaver(copyFromZipFilename)) { saver =>
    saver.addModel(copyFromZipModel.parameters, key)
  }
}
