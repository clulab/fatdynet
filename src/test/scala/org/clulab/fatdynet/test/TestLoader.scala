package org.clulab.fatdynet.test

import java.io.File

import org.scalatest._
import edu.cmu.dynet._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Loader
import org.clulab.fatdynet.utils.Loader.ClosableModelSaver
import org.clulab.fatdynet.utils.Transducer

class TestLoader extends FlatSpec with Matchers {

  /**
    * TODO
    *
    * For LstmBuilder, CompactVanillaLSTMBuilder, CoupledLstmBuilder, VanillaLstmBuilder
    *    newBuilder.setDropout(d = 0.12f, dR = 0.34f)
    * should work, but might have to reset the RNG after the model is reloaded.
    * Scala does not have access to this functionality.
    *
    * For SimpleRnnBuilder support
    *     newBuilder.addAuxiliaryInput(x: Expression, aux: Expression): Expression
    * rather than just the simple addInput.
    *
    * Figure out how to do input on tree LSTMs.
    */

  class ExpressionLoaderTester(val name: String) {
    val testname: String = name
    val filename: String = "Test" + testname + ".txt"

    def test: Unit = {
      behavior of testname

      it should "serialize the builder properly" in {
        val parameterName = "parameterTest"
        val parameterSize = 101
        val lookupParameterName = "lookupParameterTest"
        val lookupParameterSize = 111

        val oldParameters = new ParameterCollection()
        val oldParameter = oldParameters.addParameters(Dim(parameterSize))
        val oldLookupParameter = oldParameters.addLookupParameters(1L, Dim(lookupParameterSize))
        val oldExpression = Expression.parameter(oldParameter)
        val oldLookupExpression = Expression.lookup(oldLookupParameter, 0)
        val oldFloat = Expression.sumElems(oldExpression).value.toFloat
        val oldLookupFloat = Expression.sumElems(oldLookupExpression).value.toFloat

        new ClosableModelSaver(filename).autoClose { saver =>
          saver.addParameter(oldParameter, parameterName)
          saver.addLookupParameter(oldLookupParameter, lookupParameterName)
        }

        val newExpressions = Loader.loadExpressions(filename)
        val newExpression = newExpressions(parameterName)
        val newLookupExpression = newExpressions(lookupParameterName)
        val newFloat = Expression.sumElems(newExpression).value.toFloat
        val newLookupFloat = Expression.sumElems(newLookupExpression).value.toFloat

        oldFloat should be (newFloat)
        oldLookupFloat should be (newLookupFloat)
        new File(filename).delete
      }
    }
  }

  abstract class LoaderTester(val layers: Int, val inputDim: Int, val hiddenDim: Int, val name: String) {
    val testname: String = name + "_" +  layers + "_" + inputDim + "_" + hiddenDim
    def makeBuilder(model: ParameterCollection): RnnBuilder
    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression])

    def canTransduce: Boolean = true

    val filename: String = "Test" + testname + ".txt"
    val input: Expression = Expression.randomNormal(Dim(inputDim))
    val inputs = Array(input)

    def test: Unit = {
      behavior of testname

      it should "serialize the builder properly" in {
        val oldModel = new ParameterCollection
        val oldBuilder = makeBuilder(oldModel)
        val parameterName = "parameterTest"
        val parameterSize = 101
        val lookupParameterName = "lookupParameterTest"
        val lookupParameterSize = 111
        val modelName = "/model"

        oldBuilder.newGraph()
        oldBuilder.startNewSequence()

        val oldParameters = new ParameterCollection()
        val oldParameter = oldParameters.addParameters(Dim(parameterSize))
        val oldLookupParameter = oldParameters.addLookupParameters(1L, Dim(lookupParameterSize))
        val oldExpression = Expression.parameter(oldParameter)
        val oldLookupExpression = Expression.lookup(oldLookupParameter, 0)
        val oldFloat = Expression.sumElems(oldExpression).value.toFloat
        val oldLookupFloat = Expression.sumElems(oldLookupExpression).value.toFloat

        /**
          * NOTE: If the model name is empty, then there can't seem to be any parameters.
          * dynet seems to get mixed up when loading the model then and will throw
          * an exception.
          */
        new ClosableModelSaver(filename).autoClose { saver =>
          saver.addModel(oldModel, modelName)
          saver.addParameter(oldParameter, parameterName)
          saver.addLookupParameter(oldLookupParameter, lookupParameterName)
        }

        val (optionBuilder, newOptionModel, newExpressions) = loadBuilder
        val newBuilder = optionBuilder.get
        val newModel = newOptionModel.get
        val newExpression = newExpressions(parameterName)
        val newLookupExpression = newExpressions(lookupParameterName)
        val newFloat = Expression.sumElems(newExpression).value.toFloat
        val newLookupFloat = Expression.sumElems(newLookupExpression).value.toFloat

        newBuilder.newGraph()
        newBuilder.startNewSequence()

        oldFloat should be (newFloat)
        oldLookupFloat should be (newLookupFloat)

        oldModel.parameterCount should be (newModel.parameterCount)
        val oldParametersList = oldModel.parametersList()
        val newParametersList = newModel.parametersList()
        oldParametersList.size should be (newParametersList.size)
        0.until(oldParametersList.size).foreach { index =>
          val oldDim: Dim = oldParametersList(index).dim
          val newDim = newParametersList(index).dim
          oldDim should be (newDim)

          val oldValues = oldParametersList(index).values.toSeq
          val newValues = newParametersList(index).values.toSeq

          oldValues should be (newValues)
        }

        if (canTransduce) {
          val oldTransduced = Transducer.transduce(oldBuilder, inputs)
          val oldSum = Expression.sumElems(oldTransduced.get)

          val newTransduced = Transducer.transduce(newBuilder, inputs)
          val newSum = Expression.sumElems(newTransduced.get)

          oldSum.value.toFloat should be (newSum.value.toFloat)
        }
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

  class UnidirectionalTreeLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "UnidirectionalTreeLstmLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadUnidirectionalTreeLstm(filename)

    override def canTransduce: Boolean = false
  }

  class BidirectionalTreeLstmLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "BidirectionalTreeLstmLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadBidirectionalTreeLstm(filename)

    override def canTransduce: Boolean = false
  }

  class SimpleRnnLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int, supportLags: Boolean)
      extends LoaderTester(layers, inputDim, hiddenDim, "SimpleRnnLoader" + "_" + supportLags) {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new SimpleRnnBuilder(layers, inputDim, hiddenDim, model, supportLags)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadSimpleRnn(filename)
  }

  class GruLoaderTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends LoaderTester(layers, inputDim, hiddenDim, "GruLoader") {

    def makeBuilder(model: ParameterCollection): RnnBuilder = new GruBuilder(layers, inputDim, hiddenDim, model)

    def loadBuilder: (Option[RnnBuilder], Option[ParameterCollection], Map[String, Expression]) = Loader.loadGru(filename)
  }

  Initialize.initialize(Map("random-seed" -> 2522620396L))

  // Single tests of crashing combinations
//  new FastLstmBuilderTester(3, 54, 22).test
//  new LstmLoaderTester(4, 9, 16, lnLSTM = false).test
//  new VanillaLstmLoaderTester(2, 9, 22).test

  new ExpressionLoaderTester("Expressions").test

  for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6) {
    new FastLstmLoaderTester(layers, inputDim, hiddenDim).test
    new CompactVanillaLstmLoaderTester(layers, inputDim, hiddenDim).test
    new CoupledLstmLoaderTester(layers, inputDim, hiddenDim).test
    new VanillaLstmLoaderTester(layers, inputDim, hiddenDim).test
    new UnidirectionalTreeLstmLoaderTester(layers, inputDim, hiddenDim).test
    new BidirectionalTreeLstmLoaderTester(layers, inputDim, hiddenDim).test

    new GruLoaderTester(layers, inputDim, hiddenDim).test

    new LstmLoaderTester(layers, inputDim, hiddenDim, lnLSTM = false).test
    new LstmLoaderTester(layers, inputDim, hiddenDim, lnLSTM = true).test

    new SimpleRnnLoaderTester(layers, inputDim, hiddenDim, supportLags = false).test
    new SimpleRnnLoaderTester(layers, inputDim, hiddenDim, supportLags = true).test
  }
}
