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
import java.net.JarURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile
import scala.util.Using

class CloseableModelLoader(filename: String) extends ModelLoader(filename) with AutoCloseable {
}

class CloseableZipModelLoader(filename: String, zipname: String) extends ZipModelLoader(filename, zipname) with AutoCloseable {
}

case class ResourceInfo(resourceName: String, resourceFilename: String, nativeResourceFilename: String, isZipped: Boolean)

// The reason for these first three classes is is so that the ModelLoader
// coming from CMU need not be changed.  The ZipModelLoader is there to
// parallel the plain ModelLoader, but it is not encumbered by the needs
// of this particular project so that it can be included simply in dynet.
// The models are read in C++ and can't read from resources.
abstract class BaseTextModelLoader extends AutoCloseable {
  def populateModel(model: ParameterCollection, key: String = ""): Unit
  def populateParameter(p: Parameter, key: String = ""): Unit
  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit
  def close(): Unit

  def toNativeFileName(filename: String): String = new File(filename).getCanonicalPath
}

object BaseTextModelLoader {

  def getResourceInfo(resourceName: String, classLoader: ClassLoader): ResourceInfo = {
    val url = classLoader.getResource(resourceName)
    if (Option(url).isEmpty)
      throw new RuntimeException(s"ERROR: cannot locate the model file $resourceName!")
    val protocol = url.getProtocol
    if (protocol == "jar") {
      // The resource has been jarred, and must be extracted with a ZipModelLoader.
      val jarUrl = url.openConnection().asInstanceOf[JarURLConnection].getJarFileURL
      val protocol2 = jarUrl.getProtocol
      assert(protocol2 == "file")
      val uri = new URI(jarUrl.toString)
      // This converts both percent encoded characters and file separators.
      val nativeJarFileName = new File(uri).getCanonicalPath
      val resourceFileName = uri.getPath

      ResourceInfo(resourceName, resourceFileName, nativeJarFileName, true)
    }
    else if (protocol == "file") {
      // The resource has not been jarred, but lives in a classpath directory.
      val uri = new URI(url.toString)
      // This converts both percent encoded characters and file separators.
      val nativeFileName = new File(uri).getCanonicalPath
      val resourceFileName = uri.getPath

      ResourceInfo(resourceName, resourceFileName, nativeFileName, false)
    }
    else
      throw new RuntimeException(s"ERROR: cannot locate the model file $resourceName with protocol $protocol!")
  }

  def getResourceInfo(filename: String, classLoaderProvider: Any): ResourceInfo =
      getResourceInfo(filename, classLoaderProvider.getClass.getClassLoader)

  def getResourceInfo(filename: String): ResourceInfo = getResourceInfo(filename, this)

  def newTextModelLoader(filename: String): BaseTextModelLoader = {
    val possibleFile = new File(filename)

    if (possibleFile.exists())
      // Read from this file on disk.
      new RawTextModelLoader(filename)
    else {
      val resourceInfo = BaseTextModelLoader.getResourceInfo(filename)

      if (resourceInfo.isZipped)
        // The resource has been zipped/jarred, and must be extracted with a ZipModelLoader.
        new ZipTextModelLoader(filename, resourceInfo.resourceFilename)
      else
        // The resource has not been zipped/jarred, but lives in a classpath directory.
        new RawTextModelLoader(resourceInfo.resourceFilename)
    }
  }
}

class RawTextModelLoader(filename: String) extends BaseTextModelLoader {
  // For this modelLoader below, the filename needs to be in native format because C++ will open the file.
  // The argument to the constructor above is expected to be in Java format, so it is converted.
  protected val modelLoader: ModelLoader = new ModelLoader(toNativeFileName(filename))

  def populateModel(model: ParameterCollection, key: String = ""): Unit =
    modelLoader.populateModel(model, key)

  def populateParameter(p: Parameter, key: String = ""): Unit =
    modelLoader.populateParameter(p, key)

  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit =
    modelLoader.populateLookupParameter(p, key)

  def close(): Unit = modelLoader.done()
}

class ZipTextModelLoader(filename: String, zipname: String) extends BaseTextModelLoader {
  // For this modelLoader below, the zipname needs to be in native format because C++ will open the file.
  // The argument to the constructor above is expected to be in Java format, so it is converted.
  // The filename, which is used by zLib to look inside the zip file, can stay in Java format.
  protected val modelLoader: ZipModelLoader = new ZipModelLoader(filename, toNativeFileName(zipname))

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
  def newTextModelLoader(): BaseTextModelLoader
  def withBufferedReader(f: BufferedReader => Unit): Unit
}

object BaseTextLoader {
  val BUFFER_SIZE = 2048
  // If there are performance problems related to converting large files from
  // UTF8, then switch to ASCII, but then also turn off TestUnicode.
  // There will probably never be a UTF8 key in any file.
  val CHAR_SET: String = StandardCharsets.UTF_8.toString
  // val CHAR_SET = StandardCharsets.US_ASCII.toString

  def newTextLoader(filename: String): BaseTextLoader = {
    val possibleFile = new File(filename)

    if (possibleFile.exists())
      // Read from this file on disk.
      new RawTextLoader(filename)
    else {
      val resourceInfo = BaseTextModelLoader.getResourceInfo(filename)

      if (resourceInfo.isZipped)
        // The resource has been zipped/jarred, and must be extracted with a ZipModelLoader.
        new ZipTextLoader(filename, resourceInfo.resourceFilename)
      else
        // The resource has not been zipped/jarred, but lives in a classpath directory.
        new RawTextLoader(resourceInfo.resourceFilename)
    }
  }
}

class RawTextLoader(filename: String) extends BaseTextLoader {

  def newTextModelLoader(): BaseTextModelLoader = new RawTextModelLoader(filename)

  def withBufferedReader(f: BufferedReader => Unit): Unit = {
    val file = new File(filename)
    val fileInputStream = new FileInputStream(file)
    val inputStreamReader = new InputStreamReader(fileInputStream, BaseTextLoader.CHAR_SET)
    val bufferedReader = new BufferedReader(inputStreamReader, BaseTextLoader.BUFFER_SIZE)

    Using.resource(bufferedReader) { bufferedReader =>
      f(bufferedReader)
    }
  }
}

class ZipTextLoader(filename: String, zipname: String) extends BaseTextLoader {

  def newTextModelLoader(): BaseTextModelLoader = new ZipTextModelLoader(filename, zipname)

  def withBufferedReader(f: BufferedReader => Unit): Unit = {
    val zipFile = new ZipFile(zipname)

    // The zipFile needs to be closed as well or else the file won't delete.
    // Closing the bufferedReader alone is insufficient.
    Using.resource(zipFile) { zipFile =>
      val zipEntry = zipFile.getEntry(filename)
      val inputStream = zipFile.getInputStream(zipEntry)
      val inputStreamReader = new InputStreamReader(inputStream, BaseTextLoader.CHAR_SET)
      val bufferedReader = new BufferedReader(inputStreamReader, BaseTextLoader.BUFFER_SIZE)

      Using.resource(bufferedReader) { bufferedReader =>
        f(bufferedReader)
      }
    }
  }
}
