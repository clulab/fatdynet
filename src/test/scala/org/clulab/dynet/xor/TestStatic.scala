package org.clulab.dynet.xor

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestStaticComputationGraph extends FlatSpec with Matchers {
  Xor.initialize()

  val xorParameters = new Xor.XorParameters()

  behavior of "static Xor"

  it should "run" in {
    val loss = Xor.runStatic(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run repeatedly" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = Xor.runStatic(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}
