package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.utils.BaseTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

object Xor {

  class XorParameters {
    val HIDDEN_SIZE = 8

    val model = new ParameterCollection
    val p_W: Parameter = model.addParameters(Dim(HIDDEN_SIZE, 2))
    val p_b: Parameter = model.addParameters(Dim(HIDDEN_SIZE))
    val p_V: Parameter = model.addParameters(Dim(1, HIDDEN_SIZE))
    val p_a: Parameter = model.addParameters(Dim(1))

    // Rather than allowing any random initialization to be used, load parameters from a file.
    BaseTextModelLoader.newTextModelLoader("./src/test/resources/xor.rnn").autoClose { textModelLoader =>
      textModelLoader.populateModel(model)
    }
  }

  val expectedLoss: Float = 6.372183E-10f

  def initialize(train: Boolean = true): Unit = {
    val map = Map(
      Initializer.RANDOM_SEED -> 411865951L,
      Initializer.DYNET_MEM -> "2048",
      Initializer.FORWARD_ONLY -> { if (train) 0 else 1 },
      Initializer.DYNAMIC_MEM -> !train
    )

    Initializer.initialize(map)
  }

  // This is the original code to emulate.
  def runDefault(xorParameters: XorParameters): Float = {
    ComputationGraph.renew() // not necessary here

    val W = Expression.parameter(xorParameters.p_W)
    val b = Expression.parameter(xorParameters.p_b)
    val V = Expression.parameter(xorParameters.p_V)
    val a = Expression.parameter(xorParameters.p_a)

    val x_values = new FloatVector(2)
    val x = Expression.input(Dim(2), x_values)

    // Need a pointer representation of scalars so updates are tracked
    val y_value = new FloatPointer
    val y = Expression.input(y_value)

    val h = Expression.tanh(W * x + b)
    val y_pred = V * h + a
    val loss_expr = Expression.squaredDistance(y_pred, y)

    var loss: Float = 0
    for (mi <- 0 to 3) {
      val x1: Boolean = mi % 2 > 0
      val x2: Boolean = (mi / 2) % 2 > 0
      x_values.update(0, if (x1) 1 else -1)
      x_values.update(1, if (x2) 1 else -1)
      y_value.set(if (x1 != x2) 1 else -1)
      val spotLoss = ComputationGraph.forward(loss_expr).toFloat
      loss += spotLoss
    }
//    println("loss = " + loss)
    loss
  }

  def runStatic(xorParameters: XorParameters): Float = {
    runDefault(xorParameters)
  }

  def runDynamic(xorParameters: XorParameters): Float = {
    runDefault(xorParameters)
  }
}
