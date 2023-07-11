package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.examples.XorScala
import org.clulab.fatdynet.utils.Platform

class TestXorScalaRun extends FatdynetTest {
  val isWindows: Boolean = Platform.isWindows
  val isMac: Boolean = Platform.isMac
  val isLinux: Boolean = Platform.isLinux
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedMostRecentLoss = "8.828458E-10"
  val expectedTotalLoss = "13.468675"
  val expectedStaticLoss = "6.372183E-10"

  behavior of "XorScala"

  it should "get the right result" in {
    val (mostRecentLoss, totalLoss, staticLoss) = XorScala.run()

    if (isWindows) {
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
      staticLoss.toString should be(expectedStaticLoss)
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
      staticLoss.toString should be(expectedStaticLoss)
    }
    else {
      throw new Exception(s"Operating system wasn't identified: ${Platform.osName}")
    }
  }
}
