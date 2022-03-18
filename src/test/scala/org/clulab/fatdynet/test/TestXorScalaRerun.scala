package org.clulab.fatdynet.test

import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.examples.cmu.XorScala
import org.clulab.fatdynet.utils.Platform

class TestXorScalaRerun extends FatdynetTest {
  val isWindows: Boolean = Platform.isWindows
  val isMac: Boolean = Platform.isMac
  val isLinux: Boolean = Platform.isLinux
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedMostRecentLoss = "6.168399E-12"
  val expectedTotalLoss = "13.83572"

  behavior of "XorScala"

  def run(index: Int, check: Boolean = true): Unit = {
    it should "get the right result for round " + index in {
      // If this is not the first test, dynet will have been previously initialized
      // before run() and the new setting of the random seed will be ignored.  However,
      // reset_rng can be carried out even before and independently of initialization so
      // that this code works whether or not it is the first or only test run.
      // However, this functionality has been incorporated into the Initializer.
      // reset_rng(XorScala.RANDOM_SEED)
    
      val (mostRecentLoss, totalLoss) = XorScala.run()

      if (check)
        if (isWindows) {
          mostRecentLoss.toString should be(expectedMostRecentLoss)
          totalLoss.toString should be(expectedTotalLoss)
        }
        else if (isMac) {
          if (Platform.isIntel) {
            mostRecentLoss.toString should be(expectedMostRecentLoss)
            totalLoss.toString should be(expectedTotalLoss)
          }
          else if (Platform.isApple) {
            mostRecentLoss.toString should be("5.2651217E-12")
            totalLoss.toString should be("13.835722")
          }
          else
            throw new Exception(s"Architecture wasn't identified: ${Platform.osArch}")
        }
        else if (isLinux) {
          mostRecentLoss.toString should be(expectedMostRecentLoss)
          totalLoss.toString should be(expectedTotalLoss)
        }
        else {
          throw new Exception(s"Operating system wasn't identified: ${Platform.osName}")
        }
    }
  }

  run(0, true) // See if can reset even before.
  run(1)
  run(2)
}

