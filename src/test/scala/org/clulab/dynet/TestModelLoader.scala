package org.clulab.dynet

import java.io.File

import org.clulab.fatdynet.utils.Zipper
import org.clulab.dynet.models.hot.scala.{HotModel => Model}
import org.clulab.fatdynet.utils.CloseableModelLoader
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.CloseableZipModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

// This version tests the Scala interfaces.
class TestModelLoader extends TestLoader {

  def save(filename: String, model: Model, key: String): Unit = {
    new CloseableModelSaver(filename).autoClose { saver =>
      saver.addModel(model.parameters, key)
    }
  }

  def save(filename: String, modelA: Model, keyA: String, modelB: Model, keyB: String): Unit = {
    new CloseableModelSaver(filename).autoClose { saver =>
      saver.addModel(modelA.parameters, keyA)
      saver.addModel(modelB.parameters, keyB)
    }
  }

  def loadRaw(filename: String, key: String): Model = {
    new CloseableModelLoader(filename).autoClose { loader =>
      val model = Model()
      loader.populateModel(model.parameters, key)

      model
    }
  }

  def loadZip(filename: String, zipname: String, key: String): Model = {
    new CloseableZipModelLoader(filename, zipname).autoClose { loader =>
      val model = Model()

      loader.populateModel(model.parameters, key)
      model
    }
  }

  def initialize(): Unit = {
    Initializer.initialize(Map("random-seed" -> 2522620396L, "dynet-mem" -> "2048"))
  }

  initialize()

  behavior of "serialized Scala models"

  // The rest of this should be the same in Java and Scala.

  it should "survive a round trip" in {
    val origFilenameA = "modelA.rnn"
    val origFilenameB = "modelB.rnn"
    val origFilenameAB = "modelAB.rnn"
    val zipFilenameA = "modelA.jar"
    val zipFilenameB = "modelB.jar"
    val zipFilenameAB = "modelAB.jar"
    val copyFromTextFilenameA1 = "modelTextCopyA1.rnn"
    val copyFromTextFilenameA2 = "modelTextCopyA2.rnn"
    val copyFromTextFilenameB1 = "modelTextCopyB1.rnn"
    val copyFromTextFilenameB2 = "modelTextCopyB2.rnn"
    val copyFromZipFilenameA1 = "modelZipCopyA1.rnn"
    val copyFromZipFilenameA2 = "modelZipCopyA2.rnn"
    val copyFromZipFilenameB1 = "modelZipCopyB1.rnn"
    val copyFromZipFilenameB2 = "modelZipCopyB2.rnn"
    val keyA = "/keyA"
    val keyB = "/keyB"

    {
      val modelA = Model()
      val modelB = Model()

      save(origFilenameA, modelA, keyA)
      save(origFilenameB, modelB, keyB)
      save(origFilenameAB, modelA, keyA, modelB, keyB)

      Zipper.zip(origFilenameA, zipFilenameA)
      Zipper.zip(origFilenameB, zipFilenameB)
      Zipper.zip(origFilenameAB, zipFilenameAB)
    }

    val copyFromTextModelA1 = loadRaw(origFilenameA, keyA)
    val copyFromTextModelB1 = loadRaw(origFilenameB, keyB)
    val copyFromTextModelA2 = loadRaw(origFilenameAB, keyA)
    val copyFromTextModelB2 = loadRaw(origFilenameAB, keyB)

    val copyFromZipModelA1 = loadZip(origFilenameA, zipFilenameA, keyA)
    val copyFromZipModelB1 = loadZip(origFilenameB, zipFilenameB, keyB)
    val copyFromZipModelA2 = loadZip(origFilenameAB, zipFilenameAB, keyA)
    val copyFromZipModelB2 = loadZip(origFilenameAB, zipFilenameAB, keyB)

    save(copyFromTextFilenameA1, copyFromTextModelA1, keyA)
    save(copyFromTextFilenameA2, copyFromTextModelA2, keyA)
    save(copyFromTextFilenameB1, copyFromTextModelB1, keyB)
    save(copyFromTextFilenameB2, copyFromTextModelB2, keyB)

    save(copyFromZipFilenameA1, copyFromZipModelA1, keyA)
    save(copyFromZipFilenameA2, copyFromZipModelA2, keyA)
    save(copyFromZipFilenameB1, copyFromZipModelB1, keyB)
    save(copyFromZipFilenameB2, copyFromZipModelB2, keyB)

    val origTextA = textFromFile(origFilenameA)
    val textTextA1 = textFromFile(copyFromTextFilenameA1)
    val textTextA2 = textFromFile(copyFromTextFilenameA2)
    val zipTextA1 = textFromFile(copyFromZipFilenameA1)
    val zipTextA2 = textFromFile(copyFromZipFilenameA2)

    val origTextB = textFromFile(origFilenameB)
    val textTextB1 = textFromFile(copyFromTextFilenameB1)
    val textTextB2 = textFromFile(copyFromTextFilenameB2)
    val zipTextB1 = textFromFile(copyFromZipFilenameB1)
    val zipTextB2 = textFromFile(copyFromZipFilenameB2)

    textTextA1 should be (origTextA)
    textTextA2 should be (origTextA)
    zipTextA1 should be (origTextA)
    zipTextA2 should be (origTextA)

    textTextB1 should be (origTextB)
    textTextB2 should be (origTextB)
    zipTextB1 should be (origTextB)
    zipTextB2 should be (origTextB)

    origTextA should not be (origTextB)

    new File(origFilenameA).delete
    new File(origFilenameB).delete
    new File(origFilenameAB).delete
    new File(zipFilenameA).delete
    new File(zipFilenameB).delete
    new File(zipFilenameAB).delete
    new File(copyFromTextFilenameA1).delete
    new File(copyFromTextFilenameA2).delete
    new File(copyFromTextFilenameB1).delete
    new File(copyFromTextFilenameB2).delete
    new File(copyFromZipFilenameA1).delete
    new File(copyFromZipFilenameA2).delete
    new File(copyFromZipFilenameB1).delete
    new File(copyFromZipFilenameB2).delete
  }
}
