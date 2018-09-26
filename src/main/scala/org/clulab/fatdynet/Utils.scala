package org.clulab.fatdynet

import java.io.File

object Utils {
  val os = System.getProperty("os.name");

  def isWin: Boolean = os.take(7) == "Windows"

  def isMac: Boolean = os.take(3) == "Mac"

  def isLinux: Boolean = os == "Linux"

  def isKnownOS: Boolean = isWin || isMac || isLinux

  def isDynetFileAvailable: Boolean =
      if (isWin) new File("dynet.dll").exists()
      else if (isMac) new File("libdynet.dylib").exists()
      else if (isLinux) new File("libdynet.so").exists()
      else false

  def isDynetLibAvailable: Boolean =
      try {
        System.loadLibrary("dynet")
        true
      }
      catch {
        case throwable: Throwable => throwable.printStackTrace(); false
      }
}
