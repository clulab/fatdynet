package org.clulab.dynet.lstm

import org.clulab.fatdynet.Test
import org.clulab.fatdynet.test.Timer

class TestSynchronized extends Test {
  Lstm.initialize(false)

  // If this is initialized in a thread that will stay around, it is OK.
  val lstmParameters = new Lstm.LstmParameters()

  behavior of "dynamic Lstm"

  it should "run" in {
    val loss = Lstm.runSynchronized(lstmParameters)

    loss should be (Lstm.expectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = Lstm.runSynchronized(lstmParameters)

      loss should be (Lstm.expectedLoss)
    }
  }

  it should "run in parallel" in {
    val timer = new Timer("running")

    timer.time {
      Range.inclusive(1, 10000).par.foreach { _ =>
        val loss = Lstm.runSynchronized(lstmParameters)

        loss should be(Lstm.expectedLoss)
      }
    }
    println(timer.toString)
  }
}
