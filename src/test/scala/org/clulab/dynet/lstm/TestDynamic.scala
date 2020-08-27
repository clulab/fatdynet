package org.clulab.dynet.lstm

import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestDynamic extends FlatSpec with Matchers {
  Lstm.initialize(false)

  val xorParameters = new Lstm.LstmParameters()

  behavior of "dynamicXor"

  it should "run" in {
    val loss = Lstm.runDynamic(xorParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run in serial" in {
    1.to(8).foreach { i =>
      val loss = Lstm.runDynamic(xorParameters)

      loss should be (Lstm.expectedLoss)
    }
  }

  it should "run in parallel" in {
    1.to(100).foreach { _ =>
      1.to(8).par.foreach { i =>
        val loss = Lstm.runDynamic(xorParameters)

        println(s"Thread $i loss is $loss.")
        loss should be(Lstm.expectedLoss)
      }
    }
  }
}
