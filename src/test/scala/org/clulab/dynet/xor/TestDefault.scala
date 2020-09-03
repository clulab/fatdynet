package org.clulab.dynet.xor

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestDefault extends FlatSpec with Matchers {
  Xor.initialize()

  val xorParameters = new Xor.XorParameters()

  behavior of "default Xor"

  it should "run" in {
    val loss = Xor.runDefault(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run repeatedly" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = Xor.runDefault(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}