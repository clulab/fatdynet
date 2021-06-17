package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph

class TestDefault extends TestXor {
  Xor.initialize()

  val xorParameters = new Xor.XorParameters()

  var insideCount = 0

  def f(parallel: Boolean): Float = {
    if (parallel) {
      this.synchronized {
        val threadId = Thread.currentThread.getId
        println(s"Enter with threadId $threadId.")
        if (insideCount != 0)
          println("This isn't right")
        insideCount += 1
        // If there was previously a graph, it needs to be reset.
//        val result = Xor.expectedLoss
        val result = Xor.runDefault(xorParameters)
        // Get rid of the graph used in this thread, because the one reset just before
        // the next loop might be in a different thread and leave this one dangling.
        ComputationGraph.reset()

        insideCount -= 1
        if (insideCount != 0)
          println("This isn't right 2")
        println(s" Exit with threadId $threadId.")
        result
      }
    }
    else
      Xor.runDefault(xorParameters)
  }

  test("default Xor", f)
}
