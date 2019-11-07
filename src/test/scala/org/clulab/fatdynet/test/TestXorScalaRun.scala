package org.clulab.fatdynet.test

import org.clulab.fatdynet.examples.XorScala

import org.scalatest._

class TestXorScalaRun extends FlatSpec with Matchers {

  val osName = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Macintosh ")
  val isLinux: Boolean = !(isWindows || isMac)

  behavior of "XorScala"

  it should "get the right result" in {
    val result = XorScala.run()

    if (isWindows)
      result.toString should be ("13.83572")
  }
}
