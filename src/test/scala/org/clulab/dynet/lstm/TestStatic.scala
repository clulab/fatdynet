package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestStaticComputationGraph extends FlatSpec with Matchers {
  Lstm.initialize()

  behavior of "static Lstm"

  it should "run" in {
    val lstmParameters = new Lstm.LstmParameters()
    val loss = Lstm.runStatic(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run repeatedly" in {
    1.to(8).foreach { _ =>
      val lstmParameters = new Lstm.LstmParameters()
      val loss = Lstm.runStatic(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }
}
