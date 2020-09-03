package org.clulab.dynet.lstm

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.VanillaLstmBuilder
import edu.cmu.dynet.internal.dynet_swig.reset_rng
import org.clulab.fatdynet.cg.ComputationGraphable
import org.clulab.fatdynet.utils.BaseTextModelLoader
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
    val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)

    // Rather than allowing any random initialization to be used, load parameters from a file.
    BaseTextModelLoader.newTextModelLoader("./src/test/resources/lstm.rnn").autoClose { textModelLoader =>
      textModelLoader.populateModel(model)
    }
  }

  val expectedLoss: Float = 0.031386387f // This should match the C++ value, regardless of seed.

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
    // lstmParameters.clone
    val model = lstmParameters.model
    // Would like to clone these somehow.  Making a new one doesn't work.
    println(model.parameterCount)
    val builder = new VanillaLstmBuilder(lstmParameters.builder)
    println(model.parameterCount)
    val lookup = lstmParameters.lookup

    builder.newGraph()
    Range(0, inputDim).foreach { j =>
      builder.startNewSequence()
      Range(0, inputDim).foreach { k =>
        val lookedup = Expression.lookup(lookup, j * inputDim + k)

        builder.addInput(lookedup)
      }
    }

    val losses = Expression.squaredNorm(builder.finalH()(layers - 1))
    val loss = losses.value().toFloat()

    loss
  }

  def runStatic(lstmParameters: LstmParameters): Float = {
    runDefault(lstmParameters)
  }

  def runDynamic(lstmParameters: LstmParameters): Float = {
    // Make a copy of the lstmParameters here?
    runDefault(lstmParameters)
  }
}
