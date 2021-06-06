package org.clulab.dynet.xor

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestDynamic extends FlatSpec with Matchers {
  Xor.initialize(false) // We're not training now.

  val xorParameters = new Xor.XorParameters()

  behavior of "dynamic Xor"

  it should "run" in {
    val loss = Xor.runDynamic(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = Xor.runDynamic(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }

  it should "run in parallel" in {
    Range.inclusive(1, 1000).par.foreach { _ =>
      val loss = Xor.runDynamic(xorParameters)

      loss should be(Xor.expectedLoss)
    }
  }
}
