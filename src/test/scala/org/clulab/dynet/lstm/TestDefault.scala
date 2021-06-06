package org.clulab.dynet.lstm

import org.clulab.fatdynet.Test

class TestDefault extends Test {
  Lstm.initialize()

  val lstmParameters = new Lstm.LstmParameters()

  behavior of "default Lstm"

  it should "run" in {
    val loss = Lstm.runDefault(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = Lstm.runDefault(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
