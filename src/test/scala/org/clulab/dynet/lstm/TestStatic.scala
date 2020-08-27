package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestStaticComputationGraph extends FlatSpec with Matchers {
  Lstm.initialize()

  val xorParameters = new Lstm.LstmParameters()

  behavior of "staticXor"

  it should "run" in {
    val loss = Lstm.runStatic(xorParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = Lstm.runStatic(xorParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
