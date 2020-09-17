package org.clulab.dynet.lstm

import org.clulab.dynet.DynamicTest
import org.clulab.fatdynet.test.Timer

class TestDynamic extends DynamicTest {
  val lstm = new Lstm(train = false)
  val referenceLstmParameters = LstmParameters()
  val threadLocalLstmParameters = ThreadLocal.withInitial(referenceLstmParameters)

  behavior of "dynamic Lstm"

  it should "run" in {
    val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
    val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

    evenLoss should be (Lstm.evenExpectedLoss)
    oddLoss should be (Lstm.oddExpectedLoss)
  }

  it should "run in serial" in {
    Range.inclusive(1, 8).foreach { _ =>
      val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
      val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

      evenLoss should be (Lstm.evenExpectedLoss)
      oddLoss should be (Lstm.oddExpectedLoss)
    }
  }

  threaded should "run in parallel" in {
    val timer = new Timer("running")

    timer.time {
      Range.inclusive(1, 10000).par.foreach { _ =>
        val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
        val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

        evenLoss should be (Lstm.evenExpectedLoss)
        oddLoss should be (Lstm.oddExpectedLoss)
      }
    }
    println(timer.toString)
  }
}
