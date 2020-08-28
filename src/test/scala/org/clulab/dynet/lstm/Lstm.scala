package org.clulab.dynet.lstm

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.VanillaLstmBuilder
import edu.cmu.dynet.internal.dynet_swig.reset_rng
import org.clulab.fatdynet.cg.ComputationGraphable
import org.clulab.fatdynet.cg.DynamicComputationGraph
import org.clulab.fatdynet.cg.StaticComputationGraph
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

object Lstm {
  val inputDim = 1
  val layers = 2
  val hiddenDim = 10

  val seed = 42L

  class LstmParameters {
    val model = new ParameterCollection
    val lookup: LookupParameter = model.addLookupParameters(hiddenDim, Dim(inputDim))
    for (i <- 0.until(hiddenDim)) {
      lookup.initialize(i, Vector(14.5f - i))
    }
    reset_rng(seed)
    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)
  }

  // This one matches C++ output when the builder is reused.
  // When the builder is created anew each time, the result varies.
  // val expectedLoss: Float = 0.00018254126f
  // val expectedLoss: Float = 0.0113544082f
  val expectedLoss: Float = 0.031386387f // when reseeded

  def initialize(train: Boolean = true): Unit = {
    val map = Map(
      Initializer.RANDOM_SEED -> seed, // Match ser-par.cc
      Initializer.DYNET_MEM -> "2048",
      Initializer.FORWARD_ONLY -> { if (train) 0 else 1 },
      Initializer.DYNAMIC_MEM -> !train
    )

    Initializer.initialize(map)
  }

  def runDefault(lstmParameters: LstmParameters): Float = {
    // Check for use of cg in this code.
    val model = new ParameterCollection
    val lookup: LookupParameter = model.addLookupParameters(hiddenDim, Dim(inputDim))
    for (i <- 0.until(hiddenDim)) {
      lookup.initialize(i, Vector(14.5f - i))
    }
    reset_rng(seed)
    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)




//    val model = lstmParameters.model
//    val lookup = lstmParameters.lookup
    // This builder has state, so it needs to be new if there is any multi-threading.
//    reset_rng(seed)
//    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)
    // val builder = lstmParameters.builder

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

  def runGeneral(lstmParameters: LstmParameters, computationGraph: ComputationGraphable): Float = {
    val expression = computationGraph.getExpressionFactory

    val model = lstmParameters.model
    val lookup = lstmParameters.lookup
    // This builder has state, so it needs to be new if there is any multi-threading.
    reset_rng(seed)
    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)
    // val builder = lstmParameters.builder

    builder.newGraph()
    0.until(inputDim).foreach { j =>
      builder.startNewSequence()
      0.until(inputDim).foreach { k =>
        val lookedup = expression.lookup(lookup, j * inputDim + k)

        builder.addInput(lookedup.scalaExpression)
      }
    }
    val finalHLayers = expression.newFatExpression(builder.finalH()(layers - 1))
    val losses = expression.squaredNorm(finalHLayers)
    val loss = losses.value().toFloat()

    loss
  }

  def runStatic(lstmParameters: LstmParameters): Float = {
    runDefault(lstmParameters)
//    new StaticComputationGraph().autoClose { computationGraph =>
//      runGeneral(lstmParameters, computationGraph)
//    }
  }

  def runDynamic(lstmParameters: LstmParameters): Float = {
    runDefault(lstmParameters)
//    new DynamicComputationGraph().autoClose { computationGraph =>
//      runGeneral(lstmParameters, computationGraph)
//    }
  }
}
