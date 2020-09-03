package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers

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
    Range.inclusive(1, 100).foreach { _ =>
      Range.inclusive(1, 8).foreach { i =>
        val loss = Lstm.runDynamic(lstmParameters)

        println(s"Thread $i loss is $loss.")
        loss should be(Lstm.expectedLoss)
      }
    }
  }
}
