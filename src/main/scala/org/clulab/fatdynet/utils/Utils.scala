package org.clulab.fatdynet.utils

import java.io._

import edu.cmu.dynet.internal.dynet_swigJNI

object Utils {
  val os = System.getProperty("os.name");

  def isWin: Boolean = os.take(7) == "Windows"

  def isMac: Boolean = os.take(3) == "Mac"

  def isLinux: Boolean = os == "Linux"

  def isKnownOS: Boolean = isWin || isMac || isLinux

  def getLibFileName: String =
      if (isWin) "dynet.dll"
      else if (isMac) "libdynet.dylib"
      else if (isLinux) "libdynet.so"
      else throw new Exception("Unrecognized operating system")

  def isDynetFileAvailable: Boolean =
      new File(getLibFileName).exists()

  def isDynetLibAvailable: Boolean =
      try {
        System.loadLibrary("dynet")
        true
      }
      catch {
        case throwable: Throwable => false
      }

  // This is adapted from dynet_swig.i
  def loadDynet(): Boolean = {
    if (isDynetLibAvailable)
      true
    else {
      try {
 //       val tempFile = File.createTempFile("fatdynet", "")
        val tempFile = new File(getLibFileName) 
        val libname = System.mapLibraryName("dynet")
        val classLoader = Utils.getClass().getClassLoader()
        val is: InputStream = classLoader.getResourceAsStream(libname)
        val os: OutputStream = new FileOutputStream(tempFile)

        var buf = new Array[Byte](8192);
        var continue = true

        while (continue) {
          val len = is.read(buf)

          continue =
              if (len > 0) { os.write(buf, 0, len); true }
              else false
        }

        os.flush()
        val lock: InputStream = new FileInputStream(tempFile)
        os.close()

println("Loading library from " + tempFile.getCanonicalPath())
        // Load the library from the tempfile.
        System.load(tempFile.getCanonicalPath())
        lock.close()

        // And delete the tempfile.
        tempFile.delete()
        true
      }
      catch {
        case exception: IOException => println(exception); false
      }
    }
  }
}
