package org.clulab.dynet.lstm

import org.clulab.dynet.DynamicTest
import org.clulab.fatdynet.test.Timer

class TestDynamic extends DynamicTest {
  val lstm = new Lstm(train = false)
  val referenceLstmParameters = LstmParameters()
  val threadLocalLstmParameters = ThreadLocal.withInitial(referenceLstmParameters)

  behavior of "dynamic Lstm"

  it should "run" in {
    if (lstm.isCpu) {
      val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
      val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

      evenLoss should be (lstm.evenExpectedLoss)
      oddLoss should be (lstm.oddExpectedLoss)
    }
    else
      println("Skipped dynamic test on GPU")
  }

  it should "run in serial" in {
    if (lstm.isCpu) {
      Range.inclusive(1, 8).foreach { _ =>
        val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
        val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

        evenLoss should be (lstm.evenExpectedLoss)
        oddLoss should be (lstm.oddExpectedLoss)
      }
    }
    else
      println("Skipped dynamic test on GPU")
  }

  threaded should "run in parallel" in {
    if (lstm.isCpu) {
      val timer = new Timer("running")

      timer.time {
        Range.inclusive(1, 10000).par.foreach { _ =>
          val evenLoss = lstm.testStatic(threadLocalLstmParameters.get, true)
          val oddLoss = lstm.testStatic(threadLocalLstmParameters.get, false)

          evenLoss should be (lstm.evenExpectedLoss)
          oddLoss should be (lstm.oddExpectedLoss)
        }
      }
      println(timer.toString)
    }
    else
      println("Skipped dynamic test on GPU")
  }
}
