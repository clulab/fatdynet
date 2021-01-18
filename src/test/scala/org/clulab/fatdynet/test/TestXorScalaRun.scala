package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.examples.XorScala

class TestXorScalaRun extends FatdynetTest {

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
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
    }
    else if (isLinux) {
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
    }
    else {
      throw new Exception(s"Operating system wasn't identified: $osName")
    }
  }
}
