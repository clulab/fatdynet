package org.clulab.dynet.lstm

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.VanillaLstmBuilder
import org.clulab.fatdynet.cg.ComputationGraphable
import org.clulab.fatdynet.cg.DynamicComputationGraph
import org.clulab.fatdynet.cg.StaticComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

object Lstm {
  val inputDim = 1
  val layers = 2

  class LstmParameters {
    val hiddenDim = 10

    val model = new ParameterCollection
    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)
    val lookup: LookupParameter = model.addLookupParameters(hiddenDim, Dim(inputDim))
  }

  val expectedLoss: Float = 0.000341659179f

  def initialize(train: Boolean = true): Unit = {
    val map = Map(
      Initializer.RANDOM_SEED -> 10L, // Match ser-par.cc
      Initializer.DYNET_MEM -> "2048",
      Initializer.FORWARD_ONLY -> { if (train) 0 else 1 },
      Initializer.DYNAMIC_MEM -> !train
    )

    Initializer.initialize(map)
  }

  def runDefault(lstmParameters: LstmParameters): Float = {
    val builder = lstmParameters.builder
    val lookup = lstmParameters.lookup

    builder.newGraph()
    0.until(inputDim).foreach { j =>
      builder.startNewSequence()
      0.until(inputDim).foreach { k =>
        val lookedup = Expression.lookup(lookup, j * inputDim + k)

        builder.addInput(lookedup)
      }
    }

    val losses = Expression.squaredNorm(builder.finalH()(layers - 1))
    val loss = losses.value().toFloat()

    loss
  }

  def runGeneral(xorParameters: LstmParameters, computationGraph: ComputationGraphable): Float = {
/*    val expression = computationGraph.getExpressionFactory

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

//    println("loss = " + loss)
    loss*/ 0f
  }

  def runStatic(xorParameters: LstmParameters): Float = {
    new StaticComputationGraph().autoClose { computationGraph =>
      runGeneral(xorParameters, computationGraph)
    }
  }

  def runDynamic(xorParameters: LstmParameters): Float = {
    new DynamicComputationGraph().autoClose { computationGraph =>
      runGeneral(xorParameters, computationGraph)
    }
  }
}
