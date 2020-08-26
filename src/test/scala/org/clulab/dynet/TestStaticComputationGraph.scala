package org.clulab.dynet

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestStaticComputationGraph extends FlatSpec with Matchers {
  Xor.initialize()

  val xorParameters = new Xor.XorParameters()

  behavior of "staticXor"

  it should "run" in {
    val loss = Xor.staticXor(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = Xor.staticXor(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}
