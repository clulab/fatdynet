package org.clulab.fatdynet.test

import org.scalatest._

import edu.cmu.dynet.{
  Initialize,

  Dim,
  Expression,

  ParameterCollection,

  FastLstmBuilder,
  LstmBuilder,
    CompactVanillaLSTMBuilder,
    CoupledLstmBuilder,
    VanillaLstmBuilder,

  // TreeLSTMBuilder, // abstract
    UnidirectionalTreeLSTMBuilder,
    BidirectionalTreeLSTMBuilder,

  // RnnBuilder, // abstract
    SimpleRnnBuilder,

  GruBuilder
}

import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver
import org.clulab.fatdynet.utils.Transducer

class TestLoader extends FlatSpec with Matchers {
  Initialize.initialize(Map("random-seed" -> 2522620396l))

  val layers = 1 // Loop over this for sure
  val inputDim =99
  val hiddenDim = 21
//  val input: Expression = Expression.parameter(new ParameterCollection().addParameters(Dim(inputDim)))
  val input: Expression = Expression.randomNormal(Dim(inputDim))
  val inputs = Array(input)

  behavior of "FastLstmBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestFastLstmLoader.txt"
    val oldModel = new ParameterCollection

    // How to randomize these values?
    val oldBuilder = new FastLstmBuilder(layers, inputDim, hiddenDim, oldModel)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadFastLstm(filename)
//    val newBuilder = optionBuilder.get
    val newModel = newOptionModel.get
//    newBuilder.newGraph()
//    newBuilder.startNewSequence()
//    val newTransduced = Transducer.transduce(newBuilder, inputs)
//    val newSum = Expression.sumElems(newTransduced.get)

    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    println(oldSum.value.toFloat)
//    println(newSum.value.toFloat)
//    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "LstmBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestLstmLoader.txt"
    val oldModel = new ParameterCollection
    val lnLSTM: Boolean = true // false is default, but also test true

    val oldBuilder = new LstmBuilder(layers, inputDim, hiddenDim, oldModel, lnLSTM)
    // TODO These don't work
    //oldBuilder.setDropout(d = 0.12f, dR = 0.34f)
    //oldBuilder.setDropoutMasks(batchSize = 5L)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadLstm(filename)
    val newBuilder = optionBuilder.get
    // TODO These don't work
    //newBuilder.setDropout(d = 0.12f, dR = 0.34f)
    //newBuilder.setDropoutMasks(batchSize = 5L)
    val newModel = newOptionModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
    val newTransduced = Transducer.transduce(newBuilder, inputs)
    val newSum = Expression.sumElems(newTransduced.get)

    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    println(oldSum.value.toFloat)
    println(newSum.value.toFloat)
    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "CompactVanillaLSTMBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestCompactVanillaLSTMLoader.txt"
    val oldModel = new ParameterCollection
//    def setDropout(d: Float, dR: Float): Unit = builder.set_dropout(d, dR)
//    def setDropoutMasks(batchSize:Long): Unit = builder.set_dropout_masks(batchSize)

    val oldBuilder = new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, oldModel)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadCompactVanillaLstm(filename)
    val newBuilder = optionBuilder.get
    val newModel = newOptionModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
    val newTransduced = Transducer.transduce(newBuilder, inputs)
    val newSum = Expression.sumElems(newTransduced.get)

    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    println(oldSum.value.toFloat)
    println(newSum.value.toFloat)
    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "CoupledLstmBuilder"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestCoupledLstmLoader.txt"
    val oldModel = new ParameterCollection
//    def setDropout(d: Float, dH: Float, dC: Float): Unit = builder.set_dropout(d, dH, dC)
//    def setDropoutMasks(batchSize:Long): Unit = builder.set_dropout_masks(batchSize)

    val oldBuilder = new CoupledLstmBuilder(layers, inputDim, hiddenDim, oldModel)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadCoupledLstm(filename)
    val newBuilder = optionBuilder.get
    val newModel = newOptionModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
    val newTransduced = Transducer.transduce(newBuilder, inputs)
    val newSum = Expression.sumElems(newTransduced.get)

    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    println(oldSum.value.toFloat)
    println(newSum.value.toFloat)
    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "VanillaLstmBuilder Loader"

  ignore should "load the builder with proper dimensions" in {
    val filename = "TestVanillaLstmLoader.txt"
    val oldModel = new ParameterCollection
    val lnLSTM: Boolean = true // false is default, but also test true
//    def setDropout(d: Float, dR: Float): Unit = builder.set_dropout(d, dR)
//    def setDropoutMasks(batchSize:Long): Unit = builder.set_dropout_masks(batchSize)

    val oldBuilder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, oldModel, lnLSTM)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    val oldTransduced = Transducer.transduce(oldBuilder, inputs)
    val oldSum = Expression.sumElems(oldTransduced.get)

    new ClosableModelSaver(filename).autoClose { saver =>
      saver.addModel(oldModel)
    }

    val (optionBuilder, newOptionModel, _) = Loader.loadVanillaLstm(filename)
    val newBuilder = optionBuilder.get
    val newModel = newOptionModel.get
    newBuilder.newGraph()
    newBuilder.startNewSequence()
    val newTransduced = Transducer.transduce(newBuilder, inputs)
    val newSum = Expression.sumElems(newTransduced.get)

    oldModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    newModel.parametersList.foreach { parameterStorage =>
      println(parameterStorage.dim)
    }

    println(oldSum.value.toFloat)
    println(newSum.value.toFloat)
    oldSum.value.toFloat should be (newSum.value.toFloat)
  }

  behavior of "UnidirectionalTreeLSTMBuilder"

  it should "load the builder with proper dimensions" in {
    val filename = "TestUnidirectionalTreeLSTMLoader.txt"
    val oldModel = new ParameterCollection

    val oldBuilder = new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, oldModel)
    oldBuilder.newGraph()
    oldBuilder.startNewSequence()
    // Perhaps need to add input?
//    val oldTransduced = Transducer.transduce(oldBuilder, inputs) // This doesn't work!
//    val oldSum = Expression.sumElems(oldTransduced.get)

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

//  behavior of "BidirectionalTreeLSTMBuilder"
//
//  ignore should "load the builder with proper dimensions" in {
//    val filename = "TestBidirectionalTreeLSTMLoader.txt"
//    val model = new ParameterCollection
//
//    val oldBuilder = new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)
//    oldBuilder.newGraph()
//    oldBuilder.startNewSequence()
////    Transducer.transduce(oldBuilder, inputs)
//
//    new ClosableModelSaver(filename).autoClose { saver =>
//      saver.addModel(model)
//    }
//
//    val (optionBuilder, _) = Loader.loadBidirectionalTreeLstm(filename)
//    val newBuilder = optionBuilder.get
//    newBuilder.newGraph()
//    newBuilder.startNewSequence()
//    Transducer.transduce(newBuilder, inputs)
//  }
//
//  behavior of "SimpleRnnBuilder"
//
//  ignore should "load the builder with proper dimensions" in {
//    val filename = "TestSimpleRnnLoader.txt"
//    val model = new ParameterCollection
//    val supportLags: Boolean = false // default
////    def addAuxiliaryInput(x: Expression, aux: Expression): Expression = {
//
//    val oldBuilder = new SimpleRnnBuilder(layers, inputDim, hiddenDim, model, supportLags)
//    oldBuilder.newGraph()
//    oldBuilder.startNewSequence()
//    Transducer.transduce(oldBuilder, inputs)
//
//    new ClosableModelSaver(filename).autoClose { saver =>
//      saver.addModel(model)
//    }
//
//    val (optionBuilder, _) = Loader.loadSimpleRnn(filename)
//    val newBuilder = optionBuilder.get
//    newBuilder.newGraph()
//    newBuilder.startNewSequence()
//    Transducer.transduce(newBuilder, inputs)
//  }
//
//  behavior of "GruBuilder"
//
//  ignore should "load the builder with proper dimensions" in {
//    val filename = "TestGruLoader.txt"
//    val model = new ParameterCollection
//
//    val oldBuilder = new GruBuilder(layers, inputDim, hiddenDim, model)
//    oldBuilder.newGraph()
//    oldBuilder.startNewSequence()
//    Transducer.transduce(oldBuilder, inputs)
//
//    new ClosableModelSaver(filename).autoClose { saver =>
//      saver.addModel(model)
//    }
//
//    val (optionBuilder, _) = Loader.loadGru(filename)
//    val newBuilder = optionBuilder.get
//    newBuilder.newGraph()
//    newBuilder.startNewSequence()
//    Transducer.transduce(newBuilder, inputs)
//  }
}
