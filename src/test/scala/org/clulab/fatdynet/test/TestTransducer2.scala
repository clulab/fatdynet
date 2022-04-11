package org.clulab.fatdynet.test

import edu.cmu.dynet._
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Transducer

class TestTransducer2 extends FatdynetTest {
  Initializer.cluInitialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

  val rounds = 10

  val combinations = for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6)
    yield (layers, inputDim, hiddenDim)

  combinations.par.foreach { case (layers, inputDim, hiddenDim) =>
    println(s"Testing layers = $layers, inputDim = $inputDim, hiddenDim = $hiddenDim")
    implicit val cg: ComputationGraph = ComputationGraph.cluRenew()

    val model = new ParameterCollection
//    val rnnBuilder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)

    val input: Expression = Expression.randomNormal(Dim(inputDim))(cg)
    val floats = new Array[Float](rounds)
//    rnnBuilder.newGraph()(cg)
    0.until(rounds).foreach { i =>
      val oldSum = Expression.sumElems(input)(cg)
      val oldFloat = oldSum.value().toFloat()
      floats(i) = oldFloat
    }
    0.until(rounds).foreach { i =>
      assert(floats(i) == floats(0))
    }
  }
}
