package org.clulab.dynet.xor

import org.clulab.dynet.Test

class TestDefault extends Test {
  val xor = new Xor()
  val xorParameters = new XorParameters()

  behavior of "default Xor"

  it should "run" in {
    val loss = xor.runDefault(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run repeatedly" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = xor.runDefault(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}
