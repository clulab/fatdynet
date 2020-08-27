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

  ignore should "run in serial" in {
    1.to(8).foreach { _ =>
      val loss = Lstm.runDynamic(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }

  ignore should "run in parallel" in {
    1.to(100).foreach { _ =>
      1.to(8).par.foreach { i =>
        val loss = Lstm.runDynamic(lstmParameters)

        println(s"Thread $i loss is $loss.")
        loss should be(Lstm.expectedLoss)
      }
    }
  }
}
