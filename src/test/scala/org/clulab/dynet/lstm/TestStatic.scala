package org.clulab.dynet.lstm

import org.clulab.dynet.Test

class TestStaticComputationGraph extends Test {
  val lstm = new Lstm()
  val lstmParameters = LstmParameters()

  behavior of "static Lstm"

  it should "run" in {
    val loss = lstm.testStatic(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = lstm.testStatic(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
