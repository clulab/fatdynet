package org.clulab.fatdynet.test

import edu.cmu.dynet.internal.dynet_swig
import org.clulab.dynet.Test
import org.clulab.fatdynet.examples.XorScala
import org.clulab.fatdynet.utils.Initializer

class TestXorScalaRun extends Test {

  val osName: String = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Mac ")
  val isLinux: Boolean = !(isWindows || isMac)
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedCpuMostRecentLoss = "8.828458E-10"
  val expectedGpuMostRecentLoss = "2.8176572E-11"
  val expectedCpuTotalLoss = "13.468675"
  val expectedGpuTotalLoss = "8.862729"
  val expectedCpuStaticLoss = "6.372183E-10"
  val expectedGpuStaticLoss = "2.0179414E-11"

  behavior of "XorScala"

  it should "get the right result" in {
    val (mostRecentLoss, totalLoss, staticLoss) = XorScala.run()

    // This must be performed after initialization.
    val (expectedMostRecentLoss, expectedTotalLoss, expectedStaticLoss) =
        if (Initializer.isCpu) {
          println("Ran on CPU...")
          (expectedCpuMostRecentLoss, expectedCpuTotalLoss, expectedCpuStaticLoss)
        }
        else {
          println("Ran on GPU...")
          (expectedGpuMostRecentLoss, expectedGpuTotalLoss, expectedGpuStaticLoss)
        }

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
