package org.clulab.dynet

import edu.cmu.dynet._

//import org.scalatest._

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.net.URI

import collection.JavaConversions._
import scala.io.Source

object TestModelLoader extends App {

  val RNN_STATE_SIZE = 50
  val NONLINEAR_SIZE = 32
  val RNN_LAYERS = 1
  val CHAR_RNN_LAYERS = 1
  val CHAR_EMBEDDING_SIZE = 32
  val CHAR_RNN_STATE_SIZE = 16

  case class Model(
    parameters: ParameterCollection,
    lookupParameters: LookupParameter,
    fwRnnBuilder: RnnBuilder,
    bwRnnBuilder: RnnBuilder,
    H: Parameter,
    O: Parameter,
    T: LookupParameter,
    charLookupParameters: LookupParameter,
    charFwRnnBuilder: RnnBuilder,
    charBwRnnBuilder: RnnBuilder
  )

  def newModel(): Model = {
    // This model is intended to closely resemble one in use at clulab.
    case class Sizeable(size: Int)

    val w2i = Sizeable(100)
    val t2i = Sizeable(230)
    val c2i = Sizeable(123)

    val embeddingDim = 300

    val parameters = new ParameterCollection()
    val lookupParameters = parameters.addLookupParameters(w2i.size, Dim(embeddingDim))
    val embeddingSize = embeddingDim + 2 * CHAR_RNN_STATE_SIZE
    val fwBuilder = new LstmBuilder(RNN_LAYERS, embeddingSize, RNN_STATE_SIZE, parameters)
    val bwBuilder = new LstmBuilder(RNN_LAYERS, embeddingSize, RNN_STATE_SIZE, parameters)
    val H = parameters.addParameters(Dim(NONLINEAR_SIZE, 2 * RNN_STATE_SIZE))
    val O = parameters.addParameters(Dim(t2i.size, NONLINEAR_SIZE))
    val T = parameters.addLookupParameters(t2i.size, Dim(t2i.size))

    val charLookupParameters = parameters.addLookupParameters(c2i.size, Dim(CHAR_EMBEDDING_SIZE))
    val charFwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, parameters)
    val charBwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, parameters)

    Model(parameters, lookupParameters, fwBuilder, bwBuilder, H, O, T,
      charLookupParameters, charFwBuilder, charBwBuilder)
  }

  Initialize.initialize(Map("random-seed" -> 2522620396L, "dynet-mem" -> "2048"))

//  behavior of "serialized models"

//  it should "survive a round trip" in {
    val origFilename = "model.rnn"
    val zipFilename = "model.jar"
    val copyFromTextFilename = "modelTextCopy.rnn"
    val copyFromZipFilename = "modelZipCopy.rnn"
    val key = "/key"

    {
      val model = newModel()
      val saver = new ModelSaver(origFilename)

      saver.addModel(model.parameters, key)
      saver.done()
    }


    // See https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
    {
      val zipUri = new File(zipFilename).toURI().toString
      val jarUri = URI.create(s"jar:$zipUri")
      val env = mapAsJavaMap(Map("create" -> "true"))
      val zipFileSystem = FileSystems.newFileSystem(jarUri, env)
      val origPath = Paths.get(origFilename)
      val zipPath = zipFileSystem.getPath(origFilename)

      Files.copy(origPath, zipPath, StandardCopyOption.REPLACE_EXISTING)
      zipFileSystem.close()
    }

    val copyFromTextModel = {
      // This will be different from the original because of random initialization.
      val model = newModel()
      val loader = new ModelLoader(origFilename)

      loader.populateModel(model.parameters, key)
      loader.done()
      model
    }
    val copyFromZipModel = {
      // This will be different from the original because of random initialization.
      val model = newModel()
      val loader = new ZipModelLoader(origFilename, zipFilename)

      loader.populateModel(model.parameters, key)
      loader.done()
      model
    }

    {
      val saver = new ModelSaver(copyFromTextFilename)

      saver.addModel(copyFromTextModel.parameters, key)
      saver.done()
    }
    {
      val saver = new ModelSaver(copyFromZipFilename)

      saver.addModel(copyFromZipModel.parameters, key)
      saver.done()
    }

    val origText = Source.fromFile(origFilename).mkString
    val textText = Source.fromFile(copyFromTextFilename).mkString
    val zipText = Source.fromFile(copyFromZipFilename).mkString

//    textText should be (origText)
//    zipText should be (origText)

    new File(origFilename).delete
    new File(zipFilename).delete
    new File(copyFromTextFilename).delete
    new File(copyFromZipFilename).delete
//  }
}
