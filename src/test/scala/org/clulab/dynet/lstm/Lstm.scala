package org.clulab.dynet.lstm

import java.util.function.Supplier

import edu.cmu.dynet.ComputationGraph
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet.VanillaLstmBuilder
import org.clulab.fatdynet.utils.BaseTextModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer

class LstmParameters(parameterPack: LstmParameters.LstmParameterPack) extends Supplier[LstmParameters] {
  // This provides access to the variables for backward compatibility.
  val (model, lookup, builder) = (parameterPack.model, parameterPack.lookup, parameterPack.builder)
  builder.newGraph() // I may be in a new thread and have a new thread-specific ComputationGraph.

  def this() = this {
    // This is the otherwise normal constructor.
    val model = new ParameterCollection
    val lookup: LookupParameter = model.addLookupParameters(LstmParameters.hiddenDim, Dim(LstmParameters.inputDim))
    val builder = new VanillaLstmBuilder(LstmParameters.layers, LstmParameters.inputDim, LstmParameters.hiddenDim, model)

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

  override def get(): LstmParameters = this.clone
}

object LstmParameters {
  val inputDim = 1
  val layers = 2
  val hiddenDim = 10

  case class LstmParameterPack(model: ParameterCollection, lookup: LookupParameter, builder: VanillaLstmBuilder)
}

class Lstm(train: Boolean = true) {
  initialize(train)

  def initialize(train: Boolean = true): Unit = {
    val map = Map(
      Initializer.RANDOM_SEED -> Lstm.seed, // Match ser-par.cc
      Initializer.DYNET_MEM -> "2048",
      Initializer.FORWARD_ONLY -> { if (train) 0 else 1 },
      Initializer.DYNAMIC_MEM -> !train
    )

    Initializer.initialize(map)
  }

  protected def test(lstmParameters: LstmParameters): Float = {
    val builder = lstmParameters.builder
    val lookup = lstmParameters.lookup

    builder.startNewSequence()
    Range(0, LstmParameters.inputDim).foreach { k =>
      val lookedup = Expression.lookup(lookup, (LstmParameters.inputDim - 1) * LstmParameters.inputDim + k)

      builder.addInput(lookedup)
    }

    val losses = Expression.squaredNorm(builder.finalH()(LstmParameters.layers - 1))
    val loss = losses.value().toFloat()

    loss
  }

  def testDefault(lstmParameters: LstmParameters): Float = {
    test(lstmParameters)
  }

  def testStatic(lstmParameters: LstmParameters): Float = {
    test(lstmParameters)
  }

  def testDynamic(lstmParameters: LstmParameters): Float = {
    test(lstmParameters)
  }
}

object Lstm {
  val seed = 42L
  val expectedLoss: Float = 0.031386387f // This should match the C++ value, regardless of seed.
}