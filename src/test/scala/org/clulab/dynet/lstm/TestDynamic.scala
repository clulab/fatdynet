package org.clulab.dynet.lstm

import org.clulab.dynet.DynamicTest
import org.clulab.fatdynet.test.Timer

class TestDynamic extends DynamicTest {
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

  threaded should "run in parallel" in {
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
