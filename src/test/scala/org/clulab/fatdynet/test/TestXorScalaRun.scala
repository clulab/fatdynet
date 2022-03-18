package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.examples.XorScala
import org.clulab.fatdynet.utils.Platform

class TestXorScalaRun extends FatdynetTest {
  val isWindows: Boolean = Platform.isWindows
  val isMac: Boolean = Platform.isMac
  val isLinux: Boolean = Platform.isLinux
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
      if (Platform.isIntel) {
        mostRecentLoss.toString should be (expectedMostRecentLoss)
        totalLoss.toString should be (expectedTotalLoss)
      }
      else if (Platform.isApple) {
        mostRecentLoss.toString should be ("5.2651217E-12")
        totalLoss.toString should be ("13.835722")
      }
      else
        throw new Exception(s"Architecture wasn't identified: ${Platform.osArch}")
    }
    else if (isLinux) {
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
    }
    else {
      throw new Exception(s"Operating system wasn't identified: ${Platform.osName}")
    }
  }
}
