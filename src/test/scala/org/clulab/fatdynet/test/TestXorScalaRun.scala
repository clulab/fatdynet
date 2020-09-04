package org.clulab.fatdynet.test

import org.clulab.fatdynet.examples.XorScala

import org.scalatest._

class TestXorScalaRun extends FlatSpec with Matchers {

  val osName: String = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Mac ")
  val isLinux: Boolean = !(isWindows || isMac)
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
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
      staticLoss.toString should be(expectedStaticLoss)
    }
    else if (isLinux) {
      mostRecentLoss.toString should be (expectedMostRecentLoss)
      totalLoss.toString should be (expectedTotalLoss)
      staticLoss.toString should be(expectedStaticLoss)
    }
    else {
      throw new Exception(s"Operating system wasn't identified: $osName")
    }
  }
}
