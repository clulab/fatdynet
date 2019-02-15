package org.clulab.fatdynet.test

import java.io.File

import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Deleter.AutoDeleter
import org.clulab.fatdynet.utils.Saver
import edu.cmu.dynet._
import org.scalatest._

import scala.io.Source

class TestModels extends FlatSpec with Matchers {
  Initialize.initialize(Map("random-seed" -> 2522620396L))

  def equals(lefts: Seq[Float], rights: Seq[Float]): Boolean = {
    lefts.size == rights.size &&
        lefts.zip(rights).forall { case (left, right) => println(left); left == right }
  }

  def equalsPS(lefts: Seq[ParameterStorage], rights: Seq[ParameterStorage]): Boolean = {
    lefts.size == rights.size &&
        lefts.zip(rights).forall { case (left, right) => equals(left.values.toSeq, right.values.toSeq) }
  }

  def equalsLPS(lefts: Seq[LookupParameterStorage], rights: Seq[LookupParameterStorage]): Boolean = {
    lefts.size == rights.size &&
        // These values cannot be accessed!
        lefts.zip(rights).forall { case (left, right) => left.size == right.size }
  }

  def asString(operation: ModelSaver => Unit): String = {
    val tmpFile = File.createTempFile("model-", ".fatdynet")
    val filename = tmpFile.getCanonicalPath

    new AutoDeleter(tmpFile).autoDelete { file =>
      new Saver.ClosableModelSaver(filename).autoClose { modelSaver =>
        operation(modelSaver)
      }
      val string = Source.fromFile(filename).mkString
      string
    }
  }

  def asString(parameter: Parameter): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addParameter(parameter)

    asString(operation)
  }

  def asString(lookupParameter: LookupParameter): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addLookupParameter(lookupParameter)

    asString(operation)
  }

  def asString(parameterCollection: ParameterCollection): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addModel(parameterCollection)

    asString(operation)
  }

  def equals(left: Parameter, right: Parameter): Boolean = {
    left.dim == right.dim &&
        equals(left.values.toSeq, right.values.toSeq) &&
        asString(left) == asString(right)
  }

  def equals(left: LookupParameter, right: LookupParameter): Boolean = {
    left.dim == right.dim &&
        asString(left) == asString(right)
  }

  def equals(left: ParameterCollection, right: ParameterCollection): Boolean = {
    // Because of type erasure on Seq, equals cannot be overloaded here
    equalsLPS(left.lookupParametersList, right.lookupParametersList) &&
        equalsPS(left.parametersList, right.parametersList) &&
        asString(left) == asString(right)
  }

  def testNamedParameter = {
    behavior of "loaded parameters"

    it should "serialize" in {
      val filename = "parameter.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldParameter = oldParameterCollection.addParameters(Dim(51))

      new Saver.ClosableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addParameter(oldParameter, name)
      }

      val repo = new Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.parameterCollection
      val newParameter = model.getParameter(0)

      equals(newParameter, oldParameter) should be(true)
      equals(newParameterCollection, oldParameterCollection) should be(true)
    }
  }

  def testNamedLookupParameter = {
    behavior of "loaded lookup parameters"

    it should "serialize" in {
      val filename = "lookupParameter.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldLookupParameter = oldParameterCollection.addLookupParameters(51, Dim(52))

      new Saver.ClosableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addLookupParameter(oldLookupParameter, name)
      }

      val repo = new Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.parameterCollection
      val newLookupParameter = model.getLookupParameter(0)

      equals(newLookupParameter, oldLookupParameter) should be (true)
      equals(newParameterCollection, oldParameterCollection) should be (true)
    }
  }

  def testNamedRnnBuilder = {
    behavior of "loaded RNN builder"

    it should "serialize" in {
      val filename = "rnnBuilder.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldRnnBuilder = new SimpleRnnBuilder(layers = 2, inputDim = 3, hiddenDim = 4, oldParameterCollection)

      new Saver.ClosableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addModel(oldParameterCollection, name)
      }

      val repo = new Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.parameterCollection
      val newRnnBuilder = model.getRnnBuilder(0)

      equals(newParameterCollection, oldParameterCollection) should be (true)
      // There isn't a way to compare the builders.
    }
  }

  def testComposite = {
    behavior of "loaded composite model"

    it should "serialize" in {
      val filename = "compositeModel.dat"
      val name = "/all"

      val EMBEDDING_SIZE = 300
      val RNN_STATE_SIZE = 50
      val NONLINEAR_SIZE = 32
      val RNN_LAYERS = 1
      val CHAR_RNN_LAYERS = 1
      val CHAR_EMBEDDING_SIZE = 32
      val CHAR_RNN_STATE_SIZE = 16
      val W2I_SIZE = 1234
      val T2I_SIZE = 63
      val C2I_SIZE = 46
      val EMBEDDINGS_SIZE = EMBEDDING_SIZE + 2 * CHAR_RNN_STATE_SIZE

      val oldParameterCollection = new ParameterCollection()
      val lookupParameters = oldParameterCollection.addLookupParameters(W2I_SIZE, Dim(EMBEDDING_SIZE))
      val fwBuilder = new LstmBuilder(RNN_LAYERS, EMBEDDINGS_SIZE, RNN_STATE_SIZE, oldParameterCollection)
      val bwBuilder = new LstmBuilder(RNN_LAYERS, EMBEDDINGS_SIZE, RNN_STATE_SIZE, oldParameterCollection)
      val H = oldParameterCollection.addParameters(Dim(NONLINEAR_SIZE, 2 * RNN_STATE_SIZE))
      val O = oldParameterCollection.addParameters(Dim(T2I_SIZE, NONLINEAR_SIZE))

      val charLookupParameters = oldParameterCollection.addLookupParameters(C2I_SIZE, Dim(CHAR_EMBEDDING_SIZE))
      val charFwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, oldParameterCollection)
      val charBwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, oldParameterCollection)

      new Saver.ClosableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addModel(oldParameterCollection, name)
      }

      val repo = new Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.parameterCollection
      val newRnnBuilder = model.getRnnBuilder(0)

      equals(newParameterCollection, oldParameterCollection) should be (true)
      // There isn't a way to compare the builders.
    }
  }


//  def tryMe1 = {
//    val parameterCollection = new ParameterCollection()
//
//    val parameter1 = parameterCollection.addParameters(Dim(51))
//    val lookupParameter1 = parameterCollection.addLookupParameters(11, Dim(21))
//    val parameter2 = parameterCollection.addParameters(Dim(52))
//    val lookupParameter2 = parameterCollection.addLookupParameters(12, Dim(22))
//    val rnnBuilder1 = new LstmBuilder(3, 3, 3, parameterCollection)
//    val rnnBuilder2 = new CompactVanillaLSTMBuilder(4, 4, 4, parameterCollection)
//    val rnnBuilder3 = new CompactVanillaLSTMBuilder(5, 5, 5, parameterCollection)
//    val rnnBuilder4 = new LstmBuilder(6, 6, 6, parameterCollection)
//
//    new Saver.ClosableModelSaver("model1.dat").autoClose { modelSaver =>
//      modelSaver.addParameter(parameter11, "/keith1/more")
//      modelSaver.addParameter(parameter11, "/keith1/more")
//      modelSaver.addModel(parameterCollection, "/keith1")
//    }
//  }

//  def tryMe2 = {
//    val repo = new Repo("model1.dat")
//    val (parameterDesigns, lookupParameterDesigns, parameterCollectionDesigns) = repo.getDesigns()
//    val design = parameterCollectionDesigns(0) // by name or index?
//    val model = design.getModel("model1.dat")
//
//    model.getParameter("/keith1")
//    model.getLookupParameter("/keith1")
//    model.getBuilder("/keith1")
//  }
//
//  def tryMe3 = {
//    new Loader.ClosableModelLoader("model1.dat").autoClose { modelLoader =>
//      val parameterCollection1 = new ParameterCollection()
//      val parameter = parameterCollection1.addParameters(Dim(3))
//      modelLoader.populateParameter(parameter, "name")
//
//      val parameterCollection2 = new ParameterCollection()
//      val lookupParameter = parameterCollection2.addLookupParameters(5, Dim(3))
//      modelLoader.populateLookupParameter(lookupParameter, "name")
//
//      val parameterCollection3 = new ParameterCollection()
//      modelLoader.populateModel(parameterCollection3, "name")
//    }
//  }
//
//  def tryMe4 = {
//    val filename = "XorModel.dat"
//
//    val (designs, model) = new Loader.ClosableModelLoader(filename).autoClose { modelLoader =>
//      val designs = modelLoader.getDesigns()
//
//      designs(0).getModel(modelLoader)
//      val model = modelLoader.getModel(design)
//
//      (designs, model)
//    }
//
//
//
//
//
//
//    }
//    // Have repo take a loader to do its stuff?
//    // Then would only have to open once
//    val repo = new Repo(filename)
//
//    val designs = repo.getDesigns()
//    // There should be 4 of them
//    val W = designs(0).getModel(filename).parameters(0)._2
//    val b = designs(1).getModel(filename).parameters(0)._2
//    val V = designs(2).getModel(filename).parameters(0)._2
//    val a = designs(3).getModel(filename).parameters(0)._2
//  }

//  testNamedParameter
//  testNamedLookupParameter
    testNamedRnnBuilder
//  tryMe2
//  tryMe3
}
