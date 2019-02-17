package org.clulab.fatdynet.test

import java.io.File

import edu.cmu.dynet._

import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.design._
import org.clulab.fatdynet.parser._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.CloseableModelSaver

import org.scalatest._

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

class TestRepo extends FlatSpec with Matchers {
  Initialize.initialize(Map("random-seed" -> 2522620396L))

  abstract class ParserTester(val name: String) {
    val testname: String
    val count: Int = 2

    def build(model: ParameterCollection): Unit
    def testDesigns(designs: Seq[Design]): Boolean

    val modelName = "/model"

    def getDesigns(repo: Repo): Seq[Design] = repo.getDesigns()

    def test: Unit = {
      val filename: String = "Test" + testname + ".txt"

      behavior of testname

      it should "serialize the builder properly" in {
        val oldModel = new ParameterCollection

        for (_ <- 0 until count)
          build(oldModel)

        new CloseableModelSaver(filename).autoClose { saver =>
          saver.addModel(oldModel, modelName)
        }

        val repo = new Repo(filename)
        val designs = getDesigns(repo)

        testDesigns(designs) should be (true)

        new File(filename).delete
      }
    }
  }

  abstract class BaseParameterParserTester(val dim1: Int, val dim2: Int, name: String) extends ParserTester(name) {
    val testname: String = name + "_" +  dim1 + "_" + dim2
  }

  class ParameterParserTester(dim1: Int, dim2: Int)
      extends BaseParameterParserTester(dim1, dim2, "ParameterLoader") {

    def build(model: ParameterCollection): Unit = model.addParameters(Dim(dim1, dim2))

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[ParameterDesign]

        design.dims.get(0) == dim1 && design.dims.get(1) == dim2
      }.forall(success => success)
    }
  }

  class LookupParameterParserTester(dim1: Int, dim2: Int, val n: Int = 5)
      extends BaseParameterParserTester(dim1, dim2, "LookupParameterLoader") {

    def build(model: ParameterCollection): Unit = model.addLookupParameters(n, Dim(dim1, dim2))

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[LookupParameterDesign]

        design.n == n && design.dims.get(0) == dim1 && design.dims.get(1) == dim2
      }.forall(success => success)
    }
  }

  abstract class RnnParserTester(val layers: Int, val inputDim: Int, val hiddenDim: Int, name: String) extends ParserTester(name) {
    val testname: String = name + "_" +  layers + "_" + inputDim + "_" + hiddenDim
  }

  class FastLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "FastLstmLoader") {

    def build(model: ParameterCollection): Unit = new FastLstmBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[FastLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  class LstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int, val lnLSTM: Boolean)
      extends RnnParserTester(layers, inputDim, hiddenDim, "LstmLoader" + "_" + lnLSTM) {

    def build(model: ParameterCollection): Unit = new LstmBuilder(layers, inputDim, hiddenDim, model, lnLSTM)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[LstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim && design.lnLSTM == lnLSTM
      }.forall(success => success)
    }
  }

  class CompactVanillaLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "CompactVanillaLSTMLoader") {

    def build(model: ParameterCollection): Unit = new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[CompactVanillaLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  class CoupledLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "CoupledLstmLoader") {

    def build(model: ParameterCollection): Unit = new CoupledLstmBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[CoupledLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  class VanillaLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int, val lnLSTM: Boolean)
      extends RnnParserTester(layers, inputDim, hiddenDim, "VanillaLstmLoader" + "_" + lnLSTM) {

    // These should fail because they are hidden by LstmParserTester.
    override def getDesigns(repo: Repo): Seq[Design] = repo.getDesigns(Array(VanillaLstmParser.mkParser _))

    def build(model: ParameterCollection): Unit = new VanillaLstmBuilder(layers, inputDim, hiddenDim, model, lnLSTM)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[VanillaLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim && design.lnLSTM == lnLSTM
      }.forall(success => success)
    }
  }

  class UnidirectionalTreeLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "UnidirectionalTreeLstmLoader") {

    def build(model: ParameterCollection): Unit = new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[UnidirectionalTreeLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  class BidirectionalTreeLstmParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "BidirectionalTreeLstmLoader") {

    def build(model: ParameterCollection): Unit = new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[BidirectionalTreeLstmBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  class SimpleRnnParserTester(layers: Int, inputDim: Int, hiddenDim: Int, supportLags: Boolean)
      extends RnnParserTester(layers, inputDim, hiddenDim, "SimpleRnnLoader" + "_" + supportLags) {

    def build(model: ParameterCollection): Unit = new SimpleRnnBuilder(layers, inputDim, hiddenDim, model, supportLags)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[SimpleRnnBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim && design.supportLags == supportLags
      }.forall(success => success)
    }
  }

  class GruParserTester(layers: Int, inputDim: Int, hiddenDim: Int)
      extends RnnParserTester(layers, inputDim, hiddenDim, "GruLoader") {

    def build(model: ParameterCollection): Unit = new GruBuilder(layers, inputDim, hiddenDim, model)

    def testDesigns(designs: Seq[Design]): Boolean = {
      designs.size == count && designs.map { genericDesign =>
        val design = genericDesign.asInstanceOf[GruBuilderDesign]

        design.layers == layers && design.inputDim == inputDim && design.hiddenDim == hiddenDim
      }.forall(success => success)
    }
  }

  for (dim1 <- 1 to 4; dim2 <- 9 to 99 by 45) {
    new ParameterParserTester(dim1, dim2).test
    new LookupParameterParserTester(dim1, dim2).test
  }

  for (layers <- 1 to 4; inputDim <- 9 to 99 by 45; hiddenDim <- 10 to 22 by 6) {
    new FastLstmParserTester(layers, inputDim, hiddenDim).test
    new CompactVanillaLstmParserTester(layers, inputDim, hiddenDim).test
    new CoupledLstmParserTester(layers, inputDim, hiddenDim).test
    new BidirectionalTreeLstmParserTester(layers, inputDim, hiddenDim).test
    new UnidirectionalTreeLstmParserTester(layers, inputDim, hiddenDim).test
    new GruParserTester(layers, inputDim, hiddenDim).test
    new LstmParserTester(layers, inputDim, hiddenDim, lnLSTM = false).test
    new LstmParserTester(layers, inputDim, hiddenDim, lnLSTM = true).test
    new SimpleRnnParserTester(layers, inputDim, hiddenDim, supportLags = false).test
    new SimpleRnnParserTester(layers, inputDim, hiddenDim, supportLags = true).test
    new VanillaLstmParserTester(layers, inputDim, hiddenDim, lnLSTM = false).test
    new VanillaLstmParserTester(layers, inputDim, hiddenDim, lnLSTM = true).test
  }
}
