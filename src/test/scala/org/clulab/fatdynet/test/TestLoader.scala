package org.clulab.fatdynet.test

import java.io.File

import org.scalatest._
import edu.cmu.dynet._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver
import org.clulab.fatdynet.utils.Transducer

class TestLoader extends FlatSpec with Matchers {

  // TODO
  // For LstmBuilder, CompactVanillaLSTMBuilder, CoupledLstmBuilder, VanillaLstmBuilder
  //    newBuilder.setDropout(d = 0.12f, dR = 0.34f)
  // should work, but might have to reset the RNG after the model is reloaded.
  // Scala does not have access to this functionality.

  // For SimpleRnnBuilder support
  //     newBuilder.addAuxiliaryInput(x: Expression, aux: Expression): Expression
  // rather than just the simple addInput.

  abstract class LoaderTester(val layers: Int, val inputDim: Int, val hiddenDim: Int, val name: String) {
    val testname: String = name + "_" +  layers + "_" + inputDim + "_" + hiddenDim
    def makeBuilder(model: ParameterCollection): RnnBuilder
    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression])

    val filename: String = "Test" + testname + ".txt"
    val input: Expression = Expression.randomNormal(Dim(inputDim))
    val inputs = Array(input)

    def test: Unit = {
      behavior of testname

      it should "serialize the builder properly" in {
        val oldModel = new ParameterCollection
        val oldBuilder = makeBuilder(oldModel)
        oldBuilder.newGraph()
        oldBuilder.startNewSequence()
        val oldTransduced = Transducer.transduce(oldBuilder, inputs)
        val oldSum = Expression.sumElems(oldTransduced.get)

        new ClosableModelSaver(filename).autoClose { saver =>
          saver.addModel(oldModel)
        }

        val (optionBuilder, newOptionModel, _) = loadBuilder
        val newBuilder = optionBuilder.get
        val newModel = newOptionModel.get
        newBuilder.newGraph()
        newBuilder.startNewSequence()
        val newTransduced = Transducer.transduce(newBuilder, inputs)
        val newSum = Expression.sumElems(newTransduced.get)

//        oldModel.parametersList.foreach { parameterStorage =>
//          println(parameterStorage.dim)
//        }
//
//        newModel.parametersList.foreach { parameterStorage =>
//          println(parameterStorage.dim)
//        }
//
//        println(oldSum.value.toFloat)
//        println(newSum.value.toFloat)
        oldSum.value.toFloat should be(newSum.value.toFloat)

        new File(filename).delete
      }
    }
  }

  class FastLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "FastLstmLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new FastLstmBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadFastLstm(filename)
  }

  class LstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int, val lnLSTM: Boolean)
      extends LoaderTester(layers, inputDim, hiddenDim, "LstmLoader" + "_" + lnLSTM) {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new LstmBuilder(layers, inputDim, hiddenDim, model, lnLSTM)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadLstm(filename)
  }

  class CompactVanillaLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "CompactVanillaLSTMLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadCompactVanillaLstm(filename)
  }

  class CoupledLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "CoupledLstmLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new CoupledLstmBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadCoupledLstm(filename)
  }

  class VanillaLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "VanillaLstmLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadVanillaLstm(filename)
  }

  class SimpleRnnLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int, supportLags: Boolean)
      extends LoaderTester(layers, inputDim, hiddenDim, "SimpleRnn" + "_" + supportLags) {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new SimpleRnnBuilder(layers, inputDim, hiddenDim, model, supportLags)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadSimpleRnn(filename)
  }

  class GruLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "Gru") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new GruBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadGru(filename)
  }

  Initialize.initialize(Map("random-seed" -> 2522620396L))

  // Single tests of crashing combinations
//  new FastLstmBuilderTester(3, 54, 22).test
//  new LstmLoaderTester(4, 9, 16, lnLSTM = false).test
//  new VanillaLstmLoaderTester(2, 9, 22).test

  for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6) {
    new FastLstmLoaderTester(layers, inputDim, hiddenDim).test
    new CompactVanillaLstmLoaderTester(layers, inputDim, hiddenDim).test
    new CoupledLstmLoaderTester(layers, inputDim, hiddenDim).test
    new VanillaLstmLoaderTester(layers, inputDim, hiddenDim).test
    new GruLoaderTester(layers, inputDim, hiddenDim).test

    new LstmLoaderTester(layers, inputDim, hiddenDim, lnLSTM = false).test
    new LstmLoaderTester(layers, inputDim, hiddenDim, lnLSTM = true).test

    new SimpleRnnLoaderTester(layers, inputDim, hiddenDim, supportLags = false).test
    new SimpleRnnLoaderTester(layers, inputDim, hiddenDim, supportLags = true).test
  }


  val layers = 1 // Loop over this for sure
  val inputDim = 99
  val hiddenDim = 22 // Must be even for bidirectional tree builder
  val input: Expression = Expression.randomNormal(Dim(inputDim))
  val inputs = Array(input)

  behavior of "UnidirectionalTreeLSTMBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestUnidirectionalTreeLSTMLoader.txt"
    val oldModel = new ParameterCollection

    val oldBuilder = new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, oldModel)
    val test: IntVector = new IntVector(0.until(inputDim))
    oldBuilder.setNumElements(inputDim)
    // from tree, def addInput(id: Int, children: IntVector, x: Expression): Expression = {
    // from rnn   def addInput(prev: Int, x: Expression): Expression = {
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
//    oldBuilder.addInput(0, test, input)
    val oldTransduced = Transducer.transduce(oldBuilder, inputs) // This doesn't work!
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, optionNewModel, _) = Loader.loadUnidirectionalTreeLstm(filename)
    val newBuilder = optionBuilder.get
    val newModel = optionNewModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
    val newTransduced = Transducer.transduce(newBuilder, inputs)
//    val newSum = Expression.sumElems(newTransduced.get)
//
    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

//    println(oldSum.value.toFloat)
//    println(newSum.value.toFloat)
//    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "BidirectionalTreeLSTMBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestBidirectionalTreeLSTMLoader.txt"
    val oldModel = new ParameterCollection

    val oldBuilder = new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, oldModel)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
//    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
//    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadBidirectionalTreeLstm(filename)
    val newBuilder = optionBuilder.get
    val newModel = newOptionModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
//    val newTransduced = Transducer.transduce(newBuilder, inputs)
//    val newSum = Expression.sumElems(newTransduced.get)
//
//    oldModel.parametersList.foreach { parameterStorage =>
//      println(parameterStorage.dim)
//    }
//
//    newModel.parametersList.foreach { parameterStorage =>
//      println(parameterStorage.dim)
//    }
//
//    println(oldSum.value.toFloat)
//    println(newSum.value.toFloat)
//    oldSum.value.toFloat should be (newSum.value.toFloat)
  }
}
