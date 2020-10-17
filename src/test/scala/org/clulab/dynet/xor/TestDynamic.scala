package org.clulab.dynet.xor

import org.clulab.dynet.DynamicTest

class TestDynamic extends DynamicTest {
  val xor = new Xor(train = false)
  val xorParameters = new XorParameters()

  behavior of "dynamic Xor"

  it should "run" in {
    if (xor.isCpu) {
      val loss = xor.runDynamic(xorParameters)

      loss should be (xor.expectedLoss)
    }
    else
      println("Skipped dynamic test on GPU")
  }

  it should "run in serial" in {
    if (xor.isCpu) {
      Range.inclusive(1, 8).foreach { _ =>
        val loss = xor.runDynamic(xorParameters)

        loss should be (xor.expectedLoss)
      }
    }
    else
      println("Skipped dynamic test on GPU")
  }

  threaded should "run in parallel" in {
    if (xor.isCpu) {
      Range.inclusive(1, 1000).par.foreach { _ =>
        val loss = xor.runDynamic(xorParameters)

        loss should be(xor.expectedLoss)
      }
    }
    else
      println("Skipped dynamic test on GPU")
  }
}
