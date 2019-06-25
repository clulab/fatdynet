package org.clulab.dynet

import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import edu.cmu.dynet.Dim
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.LstmBuilder
import edu.cmu.dynet.ModelLoader
import edu.cmu.dynet.ModelSaver
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.RnnBuilder
import edu.cmu.dynet.ZipModelLoader
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import scala.collection.JavaConverters.mapAsJavaMap
import scala.io.Source

class TestLoader extends FlatSpec with Matchers {

  def newModel(): TestLoader.Model = TestLoader.newModel()

  def zip(rawFilename: String, zipFilename: String): Unit = TestLoader.zip(rawFilename, zipFilename)

  def save(filename: String, model: TestLoader.Model, key: String) = {
    val saver = new ModelSaver(filename)

    saver.addModel(model.parameters, key)
    saver.done()
  }

  def save(filename: String, modelA: TestLoader.Model, keyA: String, modelB: TestLoader.Model, keyB: String): Unit = {
    val saver = new ModelSaver(filename)

    saver.addModel(modelA.parameters, keyA)
    saver.addModel(modelB.parameters, keyB)
    saver.done()
  }

  def load(filename: String, key: String): TestLoader.Model = {
    val model = TestLoader.newModel()
    val loader = new ModelLoader(filename)

    loader.populateModel(model.parameters, key)
    loader.done()
    model
  }

  def loadZip(filename: String, zipname: String, key: String): TestLoader.Model = {
    val model = TestLoader.newModel()
    val loader = new ZipModelLoader(filename, zipname)

    loader.populateModel(model.parameters, key)
    loader.done()
    model
  }

  def textFromFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val text = source.mkString

    source.close()
    text
  }
}

object TestLoader {
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
    case class Sizeable(size: Int)

    val w2i = Sizeable(100)
    val t2i = Sizeable(230)
    val c2i = Sizeable(123)
    val embeddingDim = 300

    // This model is intended to closely resemble one in use at clulab.
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

  // See https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
  def zip(rawFilename: String, zipFilename: String): Unit = {
    val zipUri = new File(zipFilename).toURI.toString
    val jarUri = URI.create(s"jar:$zipUri")
    val env = mapAsJavaMap(Map("create" -> "true"))
    val zipFileSystem = FileSystems.newFileSystem(jarUri, env)
    val origPath = Paths.get(rawFilename)
    val zipPath = zipFileSystem.getPath(rawFilename)

    Files.copy(origPath, zipPath, StandardCopyOption.REPLACE_EXISTING)
    zipFileSystem.close()
  }
}
