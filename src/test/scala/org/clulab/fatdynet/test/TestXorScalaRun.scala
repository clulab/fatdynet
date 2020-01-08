package org.clulab.fatdynet.test

import org.clulab.fatdynet.examples.XorScala

import org.scalatest._

class TestXorScalaRun extends FlatSpec with Matchers {

  val osName: String = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Mac ")
  val isLinux: Boolean = !(isWindows || isMac)
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedMostRecentLoss = "6.168399E-12"
  val expectedTotalLoss = "13.83572"

  behavior of "XorScala"

  it should "get the right result" in {
    val (mostRecentLoss, totalLoss) = XorScala.run()

    if (isWindows) {
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
    }
    else if (isMac) {
      mostRecentLoss.toString should be ("7.2066797E-12")
      totalLoss.toString should be ("13.835721")
    }
    else if (isLinux) {
      mostRecentLoss.toString should be ("5.954348E-12")
      totalLoss.toString should be ("13.83572")
    }
    else {
      throw new Exception(s"Operating system wasn't identified: $osName")
    }
  }
}
