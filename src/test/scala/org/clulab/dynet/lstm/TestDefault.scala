package org.clulab.dynet.lstm

import org.clulab.dynet.Test

class TestDefault extends Test {
  val lstm = new Lstm()
  val lstmParameters = LstmParameters()

  behavior of "default Lstm"

  it should "run" in {
    val loss = lstm.testDefault(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val loss = lstm.testDefault(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
