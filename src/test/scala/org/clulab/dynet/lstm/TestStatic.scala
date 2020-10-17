package org.clulab.dynet.lstm

import org.clulab.dynet.Test

class TestStatic extends Test {
  val lstm = new Lstm()
  val lstmParameters = LstmParameters()

  behavior of "static Lstm"

  it should "run" in {
    val evenLoss = lstm.testStatic(lstmParameters, true)
    val oddLoss = lstm.testStatic(lstmParameters, false)

    evenLoss should be (lstm.evenExpectedLoss)
    oddLoss should be (lstm.oddExpectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val evenLoss = lstm.testStatic(lstmParameters, true)
      val oddLoss = lstm.testStatic(lstmParameters, false)

      evenLoss should be (lstm.evenExpectedLoss)
      oddLoss should be (lstm.oddExpectedLoss)
    }
  }
}
