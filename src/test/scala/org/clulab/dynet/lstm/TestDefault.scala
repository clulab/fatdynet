package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestDefault extends FlatSpec with Matchers {
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
