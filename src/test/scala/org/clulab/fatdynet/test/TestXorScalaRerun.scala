package org.clulab.fatdynet.test

import org.clulab.fatdynet.examples.XorScala
import org.scalatest._

class TestXorScalaRerun extends FlatSpec with Matchers {

  val osName: String = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Mac ")
  val isLinux: Boolean = !(isWindows || isMac)
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedMostRecentLoss = "6.168399E-12"
  val expectedTotalLoss = "13.83572"

  behavior of "XorScala"

  def run(index: Int): Unit = {
    it should "get the right result for round " + index in {
      val (mostRecentLoss, totalLoss) = XorScala.run()

      if (isWindows) {
        mostRecentLoss.toString should be(expectedMostRecentLoss)
        totalLoss.toString should be(expectedTotalLoss)
      }
      else if (isMac) {
        mostRecentLoss.toString should be(expectedMostRecentLoss)
        totalLoss.toString should be(expectedTotalLoss)
      }
      else if (isLinux) {
        mostRecentLoss.toString should be(expectedMostRecentLoss)
        totalLoss.toString should be(expectedTotalLoss)
      }
      else {
        throw new Exception(s"Operating system wasn't identified: $osName")
      }
    }
  }

    //run(1) // Disable because can't reset
    // reset the random number generator
    //run(2) // Don't use this just now.
}
