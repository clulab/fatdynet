package org.clulab.dynet

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.{ComputationGraph => ScalaComputationGraph}
import org.clulab.fatdynet.cg.ComputationGraphable
import org.clulab.fatdynet.cg.DynamicComputationGraph
import org.clulab.fatdynet.cg.StaticComputationGraph
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class TestComputationGraph extends FlatSpec with Matchers {

  Initializer.initialize(
    Map(
      Initializer.RANDOM_SEED -> 2522620396L,
      Initializer.DYNET_MEM -> "2048"
    )
  )

  class XorParameters {
    val HIDDEN_SIZE = 8

    val m = new ParameterCollection
    val p_W = m.addParameters(Dim(HIDDEN_SIZE, 2))
    val p_b = m.addParameters(Dim(HIDDEN_SIZE))
    val p_V = m.addParameters(Dim(1, HIDDEN_SIZE))
    val p_a = m.addParameters(Dim(1))
  }

  // This is the original code to emulate.
  def defaultXor(xorParameters: XorParameters): Unit = {
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
    val loss = ComputationGraph.forward(loss_expr).toFloat

    println("loss = " + loss)
  }

  def generalXor(xorParameters: XorParameters, computationGraph: ComputationGraphable): Unit = {
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
    val loss = computationGraph.forward(loss_expr).toFloat

    println("loss = " + loss)
  }

  def staticXor(xorParameters: XorParameters): Unit = {
    new StaticComputationGraph().autoClose { computationGraph =>
      generalXor(xorParameters, computationGraph)
    }
  }

  def dynamicXor(xorParameters: XorParameters): Unit = {
    new DynamicComputationGraph().autoClose { computationGraph =>
      generalXor(xorParameters, computationGraph)
    }
  }

  // Do both of Scala
  // One of global graph
  // Other of dynamic graph
  behavior of "Java ComputationGraph"

  it should "support getNew()" in {
    val cg1 = JavaComputationGraph.getNew()
    cg1.clear()

    val cg2 = JavaComputationGraph.getNew()
    cg2.clear()

    cg1.eq(cg2) should be (false)

    // This should no longer work.  It will completely crash Java.
    // cg1.clear()
  }

  behavior of "Scala ComputationGraph"

  it should "support renew()" in {
    ScalaComputationGraph.version should be (0)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (1)
    ScalaComputationGraph.renew()
    ScalaComputationGraph.version should be (2)
  }

  val xorParameters = new XorParameters()

  behavior of "defaultXor"

  it should "run" in {
    defaultXor(xorParameters)
  }

  behavior of "staticXor"

  it should "run" in {
    staticXor(xorParameters)
  }

  behavior of "dynamicXor"

  it should "run in serial" in {
//    1.to(2).foreach { i =>
//      dynamicXor(xorParameters)
//    }
  }

  it should "run in parallel" in {
//    1.to(8).par.foreach { i =>
//      dynamicXor(xorParameters)
//    }
  }
}
