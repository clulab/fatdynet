package org.clulab.fatdynet.test

import edu.cmu.dynet.internal.dynet_swig.reset_rng
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

  run(0, true) // See if can reset even before.
  run(1)
  run(2)
}

