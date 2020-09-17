package org.clulab.dynet.xor

import org.clulab.dynet.Test

class TestStaticComputationGraph extends Test {
  val xor = new Xor()
  val xorParameters = new XorParameters()

  behavior of "static Xor"

  it should "run" in {
    val loss = xor.runStatic(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run repeatedly" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = xor.runStatic(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}
