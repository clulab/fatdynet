package org.clulab.dynet

import org.scalatest.FlatSpec
import org.scalatest.Matchers

// If FatDynet has not been updated, this causes the error
// "Memory allocator assumes only a single ComputationGraph at a time."
class TestDynamicComputationGraph extends FlatSpec with Matchers {
  Xor.initialize(true)

  val xorParameters = new Xor.XorParameters()

  behavior of "dynamicXor"

  it should "run" in {
    val loss = Xor.dynamicXor(xorParameters)

    loss should be (Xor.expectedLoss)
  }

  it should "run in serial" in {
    1.to(8).foreach { i =>
      val loss = Xor.dynamicXor(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }

  it should "run in parallel" in {
    1.to(8).par.foreach { i =>
      val loss = Xor.dynamicXor(xorParameters)

      loss should be (Xor.expectedLoss)
    }
  }
}
