package org.clulab.fatdynet.test

import edu.cmu.dynet._
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.synchronizers.Synchronizer

class TestTransducer2 extends FatdynetTest {
  Initializer.cluInitialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

  val rounds = 10

  val combinations = for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6)
    yield (layers, inputDim, hiddenDim)

while (true) {
  combinations.take(2).par.foreach { case (layers, inputDim, hiddenDim) =>
//    println(s"Testing layers = $layers, inputDim = $inputDim, hiddenDim = $hiddenDim")

    Synchronizer.withComputationGraph("TestTransducer2") { implicit cg =>
      val threadId = Thread.currentThread.getId
      val input: Expression = Expression.randomNormal(Dim(inputDim)) // TODO try something non-random?
      val total = input.value().toSeq().sum
      val floats = new Array[Float](rounds)

      0.until(rounds).foreach { i =>
        val oldSum = Expression.sumElems(input)
        val oldFloat = oldSum.value().toFloat()
        floats(i) = oldFloat
      }
      val floatString = floats.mkString("[", ", ", "]")
      println(s"Comparing layers = $layers, inputDim = $inputDim, hiddenDim = $hiddenDim, threadId = $threadId, total = $total, floats = $floatString")
      0.until(rounds).foreach { i =>
        if (floats(i) != total)
          println(s"Mismatch layers = $layers, inputDim = $inputDim, hiddenDim = $hiddenDim, round = $i")
      }
    }
  }
}
}
