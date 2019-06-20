package org.clulab.fatdynet.utils

import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ModelLoader
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.ZipModelLoader

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
//import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

class ClosableModelLoader(filename: String) extends ModelLoader(filename) {
  def close(): Unit = done
}

class ClosableZipModelLoader(filename: String, zipname: String) extends ZipModelLoader(filename, zipname) {
  def close(): Unit = done
}

// The reason for these first three classes is is so that the ModelLoader
// coming from CMU need not be changed.  The ZipModelLoader is there to
// parallel the plain ModelLoader, but it is not encumbered by the needs
// of this particular project so that it can be included simply in dynet.
abstract class BaseModelLoader {
  def populateModel(model: ParameterCollection, key: String = ""): Unit
  def populateParameter(p: Parameter, key: String = ""): Unit
  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit
  def close(): Unit
}

class RawTextModelLoader(filename: String) extends BaseModelLoader {
  protected val modelLoader: ModelLoader = new ModelLoader(filename)

  def populateModel(model: ParameterCollection, key: String = ""): Unit =
    modelLoader.populateModel(model, key)

  def populateParameter(p: Parameter, key: String = ""): Unit =
    modelLoader.populateParameter(p, key)

  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit =
    modelLoader.populateLookupParameter(p, key)

  def close(): Unit = modelLoader.done()
}

class ZipTextModelLoader(filename: String, zipname: String) extends BaseModelLoader {
  protected val modelLoader: ZipModelLoader = new ZipModelLoader(filename, zipname)

  def populateModel(model: ParameterCollection, key: String = ""): Unit =
    modelLoader.populateModel(model, key)

  def populateParameter(p: Parameter, key: String = ""): Unit =
    modelLoader.populateParameter(p, key)

  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit =
    modelLoader.populateLookupParameter(p, key)

  def close(): Unit = modelLoader.done()
}

// These three classes account for the need to access the model files both
// by the model loaders and by the Repo Parsers which read their text.
abstract class BaseTextLoader {
  def newModelLoader(): BaseModelLoader
  def newBufferedReader(): BufferedReader
}

object BaseTextLoader {
  val BUFFER_SIZE = 2048
}

class RawTextLoader(filename: String) extends BaseTextLoader {

  def newModelLoader(): BaseModelLoader = new RawTextModelLoader(filename)

  def newBufferedReader(): BufferedReader = {
    val file = new File(filename)
    val fileInputStream = new FileInputStream(file)
    val inputStreamReader = new InputStreamReader(fileInputStream) // , StandardCharsets.UTF_8.toString)
    val bufferedReader = new BufferedReader(inputStreamReader, BaseTextLoader.BUFFER_SIZE)

    bufferedReader
  }
}

class ZipTextLoader(filename: String, zipname: String) extends BaseTextLoader {

  def newModelLoader(): BaseModelLoader = new ZipTextModelLoader(filename, zipname)

  def newBufferedReader(): BufferedReader = {
    val zipFile = new ZipFile(zipname)
    val zipEntry = zipFile.getEntry(filename)
    val inputStream = zipFile.getInputStream(zipEntry)
    val inputStreamReader = new InputStreamReader(inputStream)
    val bufferedReader = new BufferedReader(inputStreamReader, BaseTextLoader.BUFFER_SIZE)

    bufferedReader
  }
}
