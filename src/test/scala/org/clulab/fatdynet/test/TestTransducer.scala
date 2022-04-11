package org.clulab.fatdynet.test

import java.io.File
import edu.cmu.dynet._
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.design.Design
import org.clulab.fatdynet.parser.VanillaLstmParser
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.Transducer

class TestTransducer extends FatdynetTest {
  Initializer.cluInitialize()

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

  abstract class TransducerTester(val layers: Int, val inputDim: Int, val hiddenDim: Int, val name: String) {
    val testname: String = name + "_" +  layers + "_" + inputDim + "_" + hiddenDim
    def build(model: ParameterCollection): RnnBuilder

    def canTransduce: Boolean = true

    def getDesigns(repo: Repo): Seq[Design] = repo.getDesigns()

    val filename: String = "Test" + testname + ".txt"

    def test(): Unit = {
      behavior of testname

      it should "serialize the builder properly" in {
        val oldModel = new ParameterCollection
        val oldRnnBuilder = build(oldModel)
        val modelName = "/model"

        /**
          * NOTE: If the model name is empty, then there can't seem to be any parameters.
          * dynet seems to get mixed up when loading the model then and will throw
          * an exception.
          */
        new CloseableModelSaver(filename).autoClose { saver =>
          saver.addModel(oldModel, modelName)
        }

        val repo = Repo(filename)
        val designs = getDesigns(repo)
        val model = repo.getModel(designs, modelName)

        val newParameterCollection = model.getParameterCollection
        val newRnnBuilder = model.getRnnBuilder()

        if (canTransduce) {
          val rounds = 10

          ComputationGraph.cluRenew().autoClose { computationGraph =>
            implicit val cg: ComputationGraph = computationGraph

            // The vars are used to facilitate garbage collection.
            // Note that the expression is associated with the oldCg and it is reused.
            var input: Expression = Expression.randomNormal(Dim(inputDim))(cg)
            var inputs = Array(input, input, input)

            val oldFloats = new Array[Float](rounds)
            val newFloats = new Array[Float](rounds)
            oldRnnBuilder.newGraph()(cg)
            newRnnBuilder.newGraph()(cg)
            0.until(rounds).foreach { i =>
              val oldTransduced = Transducer.transduce(oldRnnBuilder, inputs).last
              val oldSum = Expression.sumElems(oldTransduced)(cg)
              val oldFloat = oldSum.value().toFloat()
              oldFloats(i) = oldFloat

              val newTransduced = Transducer.transduce(newRnnBuilder, inputs).last
              val newSum = Expression.sumElems(newTransduced)(cg)
              val newFloat = newSum.value().toFloat()
              newFloats(i) = newFloat
            }

            0.until(rounds).foreach { i =>
              println(s"old = ${oldFloats(i)}, new = ${newFloats(i)}")
            }
            0.until(rounds).foreach { i =>
              oldFloats(i) should be(newFloats(i))
            }

            input = null
            inputs = null

            Array.fill(rounds) { oldFloats(0) } should be (oldFloats)
            Array.fill(rounds) { newFloats(0) } should be (newFloats)
          }
        }
        new File(filename).delete
      }
    }
  }

  class FastLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "FastLstmLoader") {

    def build(model: ParameterCollection): RnnBuilder = new FastLstmBuilder(layers, inputDim, hiddenDim, model)
  }

  class LstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int, val lnLSTM: Boolean)
      extends TransducerTester(layers, inputDim, hiddenDim, "LstmLoader" + "_" + lnLSTM) {

    def build(model: ParameterCollection): RnnBuilder = new LstmBuilder(layers, inputDim, hiddenDim, model, lnLSTM)
  }

  class CompactVanillaLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "CompactVanillaLSTMLoader") {

    def build(model: ParameterCollection): RnnBuilder = new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, model)
  }

  class CoupledLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "CoupledLstmLoader") {

    def build(model: ParameterCollection): RnnBuilder = new CoupledLstmBuilder(layers, inputDim, hiddenDim, model)
  }

  class VanillaLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int, val lnLSTM: Boolean)
      extends TransducerTester(layers, inputDim, hiddenDim, "VanillaLstmLoader" + "_" + lnLSTM) {

    // These should fail because they are hidden by LstmParserTester.
    override def getDesigns(repo: Repo): Seq[Design] = repo.getDesigns(Seq(VanillaLstmParser.mkParser _))

    def build(model: ParameterCollection): RnnBuilder = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model)
  }

  class UnidirectionalTreeLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "UnidirectionalTreeLstmLoader") {

    override def canTransduce: Boolean = false

    def build(model: ParameterCollection): RnnBuilder = new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)
  }

  class BidirectionalTreeLstmTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "BidirectionalTreeLstmLoader") {

    override def canTransduce: Boolean = false

    def build(model: ParameterCollection): RnnBuilder = new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)
  }

  class SimpleRnnTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int, supportLags: Boolean)
      extends TransducerTester(layers, inputDim, hiddenDim, "SimpleRnnLoader" + "_" + supportLags) {

    def build(model: ParameterCollection): RnnBuilder = new SimpleRnnBuilder(layers, inputDim, hiddenDim, model, supportLags)
  }

  class GruTransducerTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends TransducerTester(layers, inputDim, hiddenDim, "GruLoader") {

    def build(model: ParameterCollection): RnnBuilder = new GruBuilder(layers, inputDim, hiddenDim, model)
  }

  Initializer.cluInitialize(Map(Initializer.RANDOM_SEED -> 2522620396L))

  for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6) {
    new FastLstmTransducerTester(layers, inputDim, hiddenDim).test()
    new CompactVanillaLstmTransducerTester(layers, inputDim, hiddenDim).test()
    new CoupledLstmTransducerTester(layers, inputDim, hiddenDim).test()
    new BidirectionalTreeLstmTransducerTester(layers, inputDim, hiddenDim).test()
    new UnidirectionalTreeLstmTransducerTester(layers, inputDim, hiddenDim).test()
    new GruTransducerTester(layers, inputDim, hiddenDim).test()
    new LstmTransducerTester(layers, inputDim, hiddenDim, lnLSTM = false).test()
    new LstmTransducerTester(layers, inputDim, hiddenDim, lnLSTM = true).test()
    new SimpleRnnTransducerTester(layers, inputDim, hiddenDim, supportLags = false).test()
    new SimpleRnnTransducerTester(layers, inputDim, hiddenDim, supportLags = true).test()
    new VanillaLstmTransducerTester(layers, inputDim, hiddenDim, lnLSTM = false).test()
    new VanillaLstmTransducerTester(layers, inputDim, hiddenDim, lnLSTM = true).test()
  }
}
