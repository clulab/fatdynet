package org.clulab.dynet

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.cg.ComputationGraphable
import org.clulab.fatdynet.cg.DynamicComputationGraph
import org.clulab.fatdynet.cg.StaticComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser
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

  def initialize(dynamic: Boolean = false): Unit = {
    Initializer.initialize(
      Map(
        Initializer.RANDOM_SEED -> 2522620396L,
        Initializer.DYNET_MEM -> "2048"
      )
    )
  }

  // This is the original code to emulate.
  def defaultXor(xorParameters: XorParameters): Float = {
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
    val y = Expression.input(y_value)

    val h = Expression.tanh(W * x + b)
    val y_pred = V * h + a
    val loss_expr = Expression.squaredDistance(y_pred, y)
    val loss = ComputationGraph.forward(loss_expr).toFloat()

    println("loss = " + loss)
    loss
  }

  def generalXor(xorParameters: XorParameters, computationGraph: ComputationGraphable): Float = {
    val expression = computationGraph.getExpressionFactory

    val W = expression.parameter(xorParameters.p_W)
    val b = expression.parameter(xorParameters.p_b)
    val V = expression.parameter(xorParameters.p_V)
    val a = expression.parameter(xorParameters.p_a)

    val x_values = new FloatVector(2)
    val x = expression.input(Dim(2), x_values)

    // Need a pointer representation of scalars so updates are tracked
    val y_value = new FloatPointer
    y_value.set(0)
    val y = expression.input(y_value)

    val h = expression.tanh(W * x + b)
    val y_pred = V * h + a
    val loss_expr = expression.squaredDistance(y_pred, y)
    val loss = computationGraph.forward(loss_expr).toFloat()

    println("loss = " + loss)
    loss
  }

  def staticXor(xorParameters: XorParameters): Float = {
    new StaticComputationGraph().autoClose { computationGraph =>
      Xor.generalXor(xorParameters, computationGraph)
    }
  }

  def dynamicXor(xorParameters: XorParameters): Float = {
    new DynamicComputationGraph().autoClose { computationGraph =>
      Xor.generalXor(xorParameters, computationGraph)
    }
  }
}
