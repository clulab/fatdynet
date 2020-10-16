package org.clulab.fatdynet.test

import org.clulab.dynet.Test
import org.clulab.fatdynet.examples.XorScala

class TestXorScalaRun extends Test {

  val osName: String = System.getProperty("os.name")
  val isWindows: Boolean = osName.startsWith("Windows ")
  val isMac: Boolean = osName.startsWith("Mac ")
  val isLinux: Boolean = !(isWindows || isMac)
  // Recent versions of fatdynet should produce the same results, independently of operating system.
  val expectedMostRecentLoss = "8.828458E-10"
  val expectedGpuMostRecentLoss = "5.9952043E-13"
  val expectedTotalLoss = "13.468675"
  val expectedGpuTotalLoss = "11.2634"
  val expectedStaticLoss = "6.372183E-10"

  behavior of "XorScala"

  it should "get the right result" in {
    val (mostRecentLoss, totalLoss, staticLoss) = XorScala.run()

    // This must be performed after initialization.
    val deviceType = dynet_swig.getDefault_device.getType.toString
    val (expectedMostRecentLoss, expectedTotalLoss) = deviceType match {
      case "CPU" =>
        println("Ran on CPU...")
        (expectedCpuMostRecentLoss, expectedCpuTotalLoss)
      case "GPU" =>
        println("Ran on GPU...")
        (expectedGpuMostRecentLoss, expectedGpuTotalLoss)
      case _ =>
        throw new RuntimeException(s"Could not recognize device $deviceType!")
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
