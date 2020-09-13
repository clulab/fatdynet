package org.clulab.dynet.lstm

import org.clulab.dynet.DynamicTest
import org.clulab.fatdynet.test.Timer

class TestDynamic extends DynamicTest {
  val lstm = new Lstm(false)
  val referenceLstmParameters = new LstmParameters()
  val lstmParameters = ThreadLocal.withInitial(referenceLstmParameters)

  behavior of "dynamic Lstm"

  it should "run" in {
    val loss = lstm.testDynamic(lstmParameters.get)

    loss should be (Lstm.expectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val loss = lstm.testDynamic(lstmParameters.get)

      loss should be (Lstm.expectedLoss)
    }
  }

  threaded should "run in parallel" in {
    val timer = new Timer("running")

    timer.time {
      Range.inclusive(1, 10000).par.foreach { _ =>
        val loss = lstm.testDynamic(lstmParameters.get)

        loss should be(Lstm.expectedLoss)
      }
    }
    println(timer.toString)
  }
}
