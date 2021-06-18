package org.clulab.dynet.xor

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.utils.Initializer

object Xor {

  class XorParameters {
    val HIDDEN_SIZE = 8

    val m = new ParameterCollection
    val p_W = m.addParameters(Dim(HIDDEN_SIZE, 2))
    val p_b = m.addParameters(Dim(HIDDEN_SIZE))
    val p_V = m.addParameters(Dim(1, HIDDEN_SIZE))
    val p_a = m.addParameters(Dim(1))
  }

  val expectedLoss: Float = 0.09710293f

  def initialize(train: Boolean = true): Unit = {
    val map = Map(
      Initializer.RANDOM_SEED -> 2522620396L,
      Initializer.DYNET_MEM -> "2048",
      Initializer.FORWARD_ONLY -> { if (train) 0 else 1 },
      Initializer.DYNAMIC_MEM -> !train
    )

    Initializer.initialize(map)
  }

  // This is the original code to emulate.
  def runDefault(xorParameters: XorParameters): Float = {
    ComputationGraph.renew()

    val W = Expression.parameter(xorParameters.p_W)
    val b = Expression.parameter(xorParameters.p_b)
    val V = Expression.parameter(xorParameters.p_V)
    val a = Expression.parameter(xorParameters.p_a)

    val x_values = new FloatVector(2)
    val x = Expression.input(Dim(2), x_values)

    // Need a pointer representation of scalars so updates are tracked

    val y_value = new FloatPointer
    y_value.set(0)

    val y = if (true) {
      Expression.input(y_value)
      // This expression holds reference to y_value
      // Expression goes away,
      // then y_value can go away.
      // So far, they never do for Windows.
    }
    else {
      val y_values = new FloatVector(1)
      Expression.input(Dim(1), y_values)
    }

    val h = Expression.tanh(W * x + b)
    val y_pred = V * h + a
    val loss_expr = Expression.squaredDistance(y_pred, y)
    val loss = ComputationGraph.forward(loss_expr).toFloat()

//    y_value.close()
//    println("loss = " + loss)
    loss
  }
}
