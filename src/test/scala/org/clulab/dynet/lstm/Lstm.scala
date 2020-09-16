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

case class LstmParameters(model: ParameterCollection, lookup: LookupParameter, builder: VanillaLstmBuilder) extends Supplier[LstmParameters] {
  // It is assumed that the builder has not been informed of the graph; otherwise this is redundant.
  // If this is multithreaded and the builder is cloned in get() below, it certainly doesn't know about
  // the graph in the new thread and learns about it here.  Using this strategy, single and multithreaded
  // applications can otherwise use the parameters in the same way.
  builder.newGraph()

  override def get(): LstmParameters = {
    copy(builder = builder.clone)
  }
}

object LstmParameters {
  val inputDim = 1
  val layers = 2
  val hiddenDim = 10

  def apply(filename: String = "./src/test/resources/lstm.rnn"): LstmParameters = {
    // This is the otherwise normal constructor.
    val model = new ParameterCollection
    val lookup: LookupParameter = model.addLookupParameters(LstmParameters.hiddenDim, Dim(LstmParameters.inputDim))
    val builder = new VanillaLstmBuilder(LstmParameters.layers, LstmParameters.inputDim, LstmParameters.hiddenDim, model)

    // Rather than allowing any random initialization to be used, load parameters from a file.
    BaseTextModelLoader.newTextModelLoader(filename).autoClose { textModelLoader =>
      textModelLoader.populateModel(model)
    }

    new LstmParameters(model, lookup, builder)
  }
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