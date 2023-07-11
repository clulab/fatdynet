package org.clulab.fatdynet.utils

object Platform {
  val osName: String = System.getProperty("os.name")
  val osArch: String = System.getProperty("os.arch")

  def isWindows: Boolean = osName.startsWith("Windows ")
  def isMac: Boolean = osName.startsWith("Mac ")
  def isLinux: Boolean = !(isWindows || isMac)

  def isApple: Boolean = osArch == "aarch64"
  def isIntel: Boolean = osArch == "x86_64"
}
