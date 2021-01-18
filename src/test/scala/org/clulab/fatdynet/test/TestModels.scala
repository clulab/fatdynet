package org.clulab.fatdynet.test

import java.io.File
import edu.cmu.dynet._
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.CloseableModelSaver
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Deleter.AutoDeleter
import org.clulab.fatdynet.utils.Initializer

import scala.io.Source

class TestModels extends FatdynetTest {
  Initializer.initialize(Map(Initializer.RANDOM_SEED -> 2522620396L, Initializer.DYNET_MEM -> "2048"))

  def equals(lefts: Seq[Float], rights: Seq[Float]): Boolean = {
    lefts.size == rights.size && {
      val result = lefts.zip(rights).forall { case (left, right) => /*println(left);*/ left == right }
//      println
      result
    }
  }

  def equalsPS(lefts: Seq[ParameterStorage], rights: Seq[ParameterStorage]): Boolean = {
    lefts.size == rights.size &&
        lefts.zip(rights).forall { case (left, right) => equals(left.values.toSeq(), right.values.toSeq()) }
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
      new CloseableModelSaver(filename).autoClose { modelSaver =>
        operation(modelSaver)
      }

      Source.fromFile(filename).autoClose { source =>
        source.mkString
      }
    }
  }

  def asString(parameter: Parameter, name: String): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addParameter(parameter, name)

    asString(operation)
  }

  def asString(lookupParameter: LookupParameter, name: String): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addLookupParameter(lookupParameter, name)

    asString(operation)
  }

  def asString(parameterCollection: ParameterCollection, name: String): String = {
    val operation: ModelSaver => Unit = modelSaver => modelSaver.addModel(parameterCollection, name)

    asString(operation)
  }

  def equals(left: Parameter, right: Parameter, name: String): Boolean = {
    left.dim == right.dim &&
        equals(left.values().toSeq(), right.values().toSeq()) &&
        asString(left, name) == asString(right, name)
  }

  def equals(left: LookupParameter, right: LookupParameter, name: String): Boolean = {
    left.dim == right.dim &&
        asString(left, name) == asString(right, name)
  }

  def equals(left: ParameterCollection, right: ParameterCollection, name: String = ""): Boolean = {
    // Because of type erasure on Seq, equals cannot be overloaded here
    asString(left, name) == asString(right, name) &&
        equalsPS(left.parametersList(), right.parametersList()) &&
        equalsLPS(left.lookupParametersList(), right.lookupParametersList())
  }

  def testNamedParameter(): Unit = {
    behavior of "loaded parameters"

    it should "serialize" in {
      val filename = "parameter.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldParameter = oldParameterCollection.addParameters(Dim(51))

      new CloseableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addParameter(oldParameter, name)
      }

      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.getParameterCollection
      val newParameter = model.getParameter()

      equals(newParameter, oldParameter, name) should be (true)
      equals(newParameterCollection, oldParameterCollection) should be (true)

      new File(filename).delete
    }
  }

  def testNamedLookupParameter(): Unit = {
    behavior of "loaded lookup parameters"

    it should "serialize" in {
      val filename = "lookupParameter.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldLookupParameter = oldParameterCollection.addLookupParameters(51, Dim(52))

      new CloseableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addLookupParameter(oldLookupParameter, name)
      }

      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.getParameterCollection
      val newLookupParameter = model.getLookupParameter()

      equals(newLookupParameter, oldLookupParameter, name) should be (true)
      equals(newParameterCollection, oldParameterCollection) should be (true)

      new File(filename).delete
    }
  }

  def testNamedRnnBuilder(): Unit = {
    behavior of "loaded RNN builder"

    it should "serialize" in {
      val filename = "rnnBuilder.dat"
      val name = "/name"

      val oldParameterCollection = new ParameterCollection()
      val oldRnnBuilder = new SimpleRnnBuilder(layers = 2, inputDim = 3, hiddenDim = 4, oldParameterCollection)

      new CloseableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addModel(oldParameterCollection, name)
      }

      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.getParameterCollection
      val newRnnBuilder = model.getRnnBuilder()

      equals(newParameterCollection, oldParameterCollection) should be (true)
      // There isn't a way to compare the builders.

      new File(filename).delete
    }
  }

  def testMihaiModel(): Unit = {
    behavior of "loaded composite model from Mihai"

    it should "serialize" in {
      val filename = "mihaiModel.dat"
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
      // Note that these have been reordered so that old and new produce bitwise identical files for string comparison.
      val oldFwBuilder = new LstmBuilder(RNN_LAYERS, EMBEDDINGS_SIZE, RNN_STATE_SIZE, oldParameterCollection, false)
      val oldBwBuilder = new LstmBuilder(RNN_LAYERS, EMBEDDINGS_SIZE, RNN_STATE_SIZE, oldParameterCollection)
      val oldH = oldParameterCollection.addParameters(Dim(NONLINEAR_SIZE, 2 * RNN_STATE_SIZE))
      val oldO = oldParameterCollection.addParameters(Dim(T2I_SIZE, NONLINEAR_SIZE))
      val oldCharFwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, oldParameterCollection)
      val oldCharBwBuilder = new LstmBuilder(CHAR_RNN_LAYERS, CHAR_EMBEDDING_SIZE, CHAR_RNN_STATE_SIZE, oldParameterCollection)
      val oldLookupParameters = oldParameterCollection.addLookupParameters(W2I_SIZE, Dim(EMBEDDING_SIZE))
      val oldCharLookupParameters = oldParameterCollection.addLookupParameters(C2I_SIZE, Dim(CHAR_EMBEDDING_SIZE))

      new CloseableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addModel(oldParameterCollection, name)
      }

      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.getParameterCollection
      val newFwBuilder = model.getRnnBuilder()
      val newBwBuilder = model.getRnnBuilder(1)
      val newH = model.getParameter()
      val newO = model.getParameter(1)
      val newCharFwBuilder = model.getRnnBuilder(2)
      val newCharBwBuilder = model.getRnnBuilder(3)
      val newLookupParameters = model.getLookupParameter()
      val newCharLookupParameters = model.getLookupParameter(1)

      equals(newLookupParameters, oldLookupParameters, name) should be (true) // Sometimes causes crash
      equals(newH, oldH, name) should be (true)
      equals(newO, oldO, name) should be (true)
      equals(newCharLookupParameters, oldCharLookupParameters, name)
      equals(newParameterCollection, oldParameterCollection, name) should be (true)

      new File(filename).delete
    }
  }

  def testEnriqueModel(): Unit = {
    behavior of "loaded composite model from Enrique"

    it should "serialize" in {
      val filename = "enriqueModel.dat"
      val name = "" // Try without a name.

      val EMBEDDING_SIZE = 300
      val W2I_SIZE = 1234
      val WEM_DIMENSIONS = 100
      val NUM_LAYERS = 1
      val HIDDEN_DIM = 20
      val FF_HIDDEN_DIM = 10

      val oldParameterCollection = new ParameterCollection()
      val oldW = oldParameterCollection.addParameters(Dim(FF_HIDDEN_DIM, HIDDEN_DIM + 1))
      val oldb = oldParameterCollection.addParameters(Dim(FF_HIDDEN_DIM))
      val oldV = oldParameterCollection.addParameters(Dim(1, FF_HIDDEN_DIM))
      val oldBuilder = new LstmBuilder(NUM_LAYERS, WEM_DIMENSIONS, HIDDEN_DIM, oldParameterCollection)
      // This was moved to bottom for bitwise comparison.
      val old_w2v_wemb = oldParameterCollection.addLookupParameters(W2I_SIZE, Dim(EMBEDDING_SIZE))

      new CloseableModelSaver(filename).autoClose { modelSaver =>
        modelSaver.addModel(oldParameterCollection, name)
      }

      val repo = Repo(filename)
      val designs = repo.getDesigns()
      val model = repo.getModel(designs, name)

      val newParameterCollection = model.getParameterCollection
      val new_w2v_wemb = model.getLookupParameter()
      val newW = model.getParameter()
      val newb = model.getParameter(1)
      val newV = model.getParameter(2)
      val newBuilder = model.getRnnBuilder()

      equals(new_w2v_wemb, old_w2v_wemb, name) should be (true) // Sometimes causes crash
      equals(newW, oldW, name) should be (true)
      equals(newb, oldb, name) should be (true)
      equals(newV, oldV, name) should be (true)
      equals(newParameterCollection, oldParameterCollection, name) should be (true)

      new File(filename).delete
    }
  }

  testNamedParameter()
  testNamedLookupParameter()
  testNamedRnnBuilder()
  testMihaiModel()
  testEnriqueModel()
}
