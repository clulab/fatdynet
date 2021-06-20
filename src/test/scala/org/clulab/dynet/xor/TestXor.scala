package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph
import org.clulab.dynet.utils.ThreadUtils
import org.clulab.fatdynet.Test

class TestXor extends Test {

  def test(name: String, f: Boolean => Float): Unit = {
    behavior of name

    it should "run" in {
      val loss = f(false)

      loss should be(Xor.expectedLoss)
    }

    it should "run serially" in {
      Range.inclusive(1, 8).foreach { _ =>
        val loss = f(false)

        loss should be(Xor.expectedLoss)
      }
    }

    it should "run in parallel" in {
      // The other tests may have left a computation graph in the main thread.
      ComputationGraph.reset()
      ThreadUtils.parallelize(Range.inclusive(1, 1000), 8).foreach { index =>
       val threadId = Thread.currentThread.getId
        println(s"Enter with index $index, threadId $threadId")
        val loss = f(true)
        if (loss.isNaN)
          println("loss is NaN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        loss should be(Xor.expectedLoss)
        println(s" Exit with index $index, threadId $threadId")
      }
      println(s"parallel end")
    }
  }
}
