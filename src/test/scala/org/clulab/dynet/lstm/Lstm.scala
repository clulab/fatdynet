package org.clulab.dynet.lstm

import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.VanillaLstmBuilder
import org.clulab.fatdynet.utils.BaseTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

object Lstm {
  val inputDim = 1
  val layers = 2
  val hiddenDim = 10

  val seed = 42L

  class LstmParameters(parameterPack: LstmParameters.LstmParameterPack) extends Cloneable {
    // This provides access to the variables for backward compatibility.
    val (model, lookup, builder) = (parameterPack.model, parameterPack.lookup, parameterPack.builder)

    def this() = this {
      // This is the otherwise normal constructor.
      val model = new ParameterCollection
      val lookup: LookupParameter = model.addLookupParameters(hiddenDim, Dim(inputDim))
      val builder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)

      // Rather than allowing any random initialization to be used, load parameters from a file.
      BaseTextModelLoader.newTextModelLoader("./src/test/resources/lstm.rnn").autoClose { textModelLoader =>
        textModelLoader.populateModel(model)
      }

      LstmParameters.LstmParameterPack(model, lookup, builder)
    }

    override def clone: LstmParameters = {
      val newParameterPack = parameterPack.copy(builder = builder.clone) // The builder must be cloned.

      new LstmParameters(newParameterPack)
    }
  }

  object LstmParameters {
    case class LstmParameterPack(model: ParameterCollection, lookup: LookupParameter, builder: VanillaLstmBuilder)
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

  def runDefault(referenceLstmParameters: LstmParameters): Float = {
    val lstmParameters = referenceLstmParameters.clone
    val model = lstmParameters.model
    val builder = lstmParameters.builder
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
    runDefault(lstmParameters)
  }
}
