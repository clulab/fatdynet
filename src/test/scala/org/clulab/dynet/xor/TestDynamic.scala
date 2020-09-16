package org.clulab.dynet.xor

import org.clulab.dynet.DynamicTest

class TestDynamic extends DynamicTest {
  val xor = new Xor(train = false)
  val xorParameters = new XorParameters()

  behavior of "dynamic Xor"

  it should "run" in {
    val loss = xor.runDynamic(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = xor.runDynamic(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }

  threaded should "run in parallel" in {
    Range.inclusive(1, 1000).par.foreach { _ =>
      val loss = xor.runDynamic(xorParameters)

      loss should be(Xor.expectedLoss)
    }
  }
}
