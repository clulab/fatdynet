package org.clulab.dynet.lstm

import org.clulab.fatdynet.Test

class TestStaticComputationGraph extends Test {
  Lstm.initialize()

  val lstmParameters = new Lstm.LstmParameters()

  behavior of "static Lstm"

  it should "run" in {
    val loss = Lstm.runStatic(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = Lstm.runStatic(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
