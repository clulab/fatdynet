package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.clulab.fatdynet.test.Timer

class TestDynamic extends FlatSpec with Matchers {
  Lstm.initialize(false)

  val lstmParameters = new Lstm.LstmParameters()

  behavior of "dynamic Lstm"

  it should "run" in {
    val loss = Lstm.runDynamic(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = Lstm.runDynamic(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }

  it should "run in parallel" in {
    val timer = new Timer("running")

    timer.time {
      Range.inclusive(1, 10000).par.foreach { _ =>
        val loss = Lstm.runDynamic(lstmParameters)

        loss should be(Lstm.expectedLoss)
      }
    }
    println(timer.toString)
  }
}
