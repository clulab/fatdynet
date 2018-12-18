package org.clulab.fatdynet.utils

import edu.cmu.dynet.{
  Initialize,

  Dim,
  Expression,

  LookupParameter,
  Parameter,
  ParameterCollection,

  // These other, unused builders to be addedd

  FastLstmBuilder,
  LstmBuilder,
    CompactVanillaLSTMBuilder,
    CoupledLstmBuilder,
    VanillaLstmBuilder,

  TreeLSTMBuilder, // abstract
    UnidirectionalTreeLSTMBuilder,
    BidirectionalTreeLSTMBuilder,

  RnnBuilder, // abstract
    SimpleRnnBuilder,

  GruBuilder,

  ModelLoader,
  ModelSaver,
}

import org.clulab.fatdynet.utils.Closer.AutoCloser

import scala.io.Source

/**
  * See https://dynet.readthedocs.io/en/latest/python_saving_tutorial.html
  */
object Loader {

  class ClosableModelLoader(filename: String) extends ModelLoader(filename) {
    def close(): Unit = done
  }

  class ClosableModelSaver(filename: String) extends ModelSaver(filename) {
    def close(): Unit = done
  }

  /**
    * This converts an objectType and objectName into a decision about whether
    * to further process the line.  It can use the ModelLoader to do some
    * processing itself.  See falseModelFilter and loadLstm for examples.
    */
  protected type ModelFilter = (ModelLoader, String, String, Array[Int]) => Boolean

  protected def falseModelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Boolean = {
    // Skip these kinds of thing because they are likely a model of some kind.
    !(objectType == "#Parameter#" && objectName.matches(".*/_[0-9]+$"))
  }

  def loadExpressions(path: String, namespace: String = ""): Map[String, Expression] = {
    new ClosableModelLoader(path).autoClose { modelLoader =>
      filteredLoadExpressions(path, modelLoader, namespace, falseModelFilter)
    }
  }

  protected def filteredLoadExpressions(path: String, modelLoader: ModelLoader, namespace: String = "", modelFilter: ModelFilter = falseModelFilter): Map[String, Expression] = {

    def read(line: String, modelLoader: ModelLoader, pc: ParameterCollection): Option[(String, Expression)] = {
      val Array(objectType, objectName, dimension, _, _) = line.split(" ")
      // Skip leading { and trailing }
      val dims = dimension.substring(1, dimension.length - 1).split(",").map(_.toInt)

      if (objectName.startsWith(namespace) && modelFilter(modelLoader, objectType, objectName, dims)) {
        val expression = objectType match {
          case "#Parameter#" =>
            val param = pc.addParameters(Dim(dims))
            modelLoader.populateParameter(param, key = objectName)
            Expression.parameter(param)
          case "#LookupParameter#" =>
            val param = pc.addLookupParameters(dims.last, Dim(dims.dropRight(1)))
            modelLoader.populateLookupParameter(param, key = objectName)
            Expression.parameter(param)
          case _ => throw new RuntimeException(s"Unrecognized line in model file: '$line'")
        }
        Option((objectName, expression))
      }
      else
        None
    }

    val expressions = Source.fromFile(path).autoClose { source =>
      val pc = new ParameterCollection

      source
          .getLines
          .filter(_.startsWith("#"))
          .flatMap { line => read(line, modelLoader, pc) }
          .toMap
    }
    expressions
  }

  def loadFastLstm(path: String, namespace: String = ""): (Option[FastLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/fast-lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            hiddenDim = dims.head
            name = matcher.group(1)
          }
          else
            require(matcher.group(1) == name)
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 11 == 0) {
          val model = new ParameterCollection
          val builder = new FastLstmBuilder(count / 11, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadLstm(path: String, namespace: String = ""): (Option[LstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/vanilla-lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    val inLstmPattern = "(.*)/_[0-9]+$".r.pattern
    var inLstmCount = 0

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)
        val inLstmMatcher = inLstmPattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            name = matcher.group(1)
          }
          else {
            require(matcher.group(1) == name)
            if (count == 1)
              hiddenDim = dims.last
          }
          count += 1
          false
        }
        else if (count > 0 && inLstmMatcher.matches && inLstmMatcher.group(1) == name) {
          inLstmCount += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 3 == 0 && (inLstmCount == 0 || inLstmCount == count * 2)) {
          val model = new ParameterCollection
          val builder = new LstmBuilder(count / 3, inputDim, hiddenDim, model, inLstmCount > 0)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadCompactVanillaLstm(path: String, namespace: String = ""): (Option[CompactVanillaLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/compact-vanilla-lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            name = matcher.group(1)
          }
          else {
            require(matcher.group(1) == name)
            if (count == 1)
              hiddenDim = dims.last
          }
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 3 == 0) {
          val model = new ParameterCollection
          val builder = new CompactVanillaLSTMBuilder(count / 3, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadCoupledLstm(path: String, namespace: String = ""): (Option[CoupledLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            hiddenDim = dims.head
            name = matcher.group(1)
          }
          else
            require(matcher.group(1) == name)
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 11 == 0) {
          val model = new ParameterCollection
          val builder = new CoupledLstmBuilder(count / 11, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadVanillaLstm(path: String, namespace: String = ""): (Option[VanillaLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/vanilla-lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    val inLstmPattern = "(.*)/_[0-9]+$".r.pattern
    var inLstmCount = 0

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)
        val inLstmMatcher = inLstmPattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            name = matcher.group(1)
          }
          else {
            require(matcher.group(1) == name)
            if (count == 1)
              hiddenDim = dims.last
          }
          count += 1
          false
        }
        else if (count > 0 && inLstmMatcher.matches && inLstmMatcher.group(1) == name) {
          inLstmCount += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 3 == 0 && (inLstmCount == 0 || inLstmCount == count * 2)) {
          val model = new ParameterCollection
          val builder = new VanillaLstmBuilder(count / 3, inputDim, hiddenDim, model, inLstmCount > 0)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadUnidirectionalTreeLstm(path: String, namespace: String = ""): (Option[UnidirectionalTreeLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/unidirectional-tree-lstm-builder/vanilla-lstm-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            name = matcher.group(1)
          }
          else {
            require(matcher.group(1) == name)
            if (count == 1)
              hiddenDim = dims.last
          }
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 3 == 0) {
          val model = new ParameterCollection
          val builder = new UnidirectionalTreeLSTMBuilder(count / 3, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }


  def loadBidirectionalTreeLstm(path: String, namespace: String = ""): (Option[BidirectionalTreeLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/bidirectional-tree-lstm-builder/vanilla-lstm-builder(_1)?/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            hiddenDim = dims.head / 2
            name = matcher.group(1)
          }
          else
            require(matcher.group(1) == name)
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 6 == 0) {
          val model = new ParameterCollection
          val builder = new BidirectionalTreeLSTMBuilder(count / 6, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadSimpleRnn(path: String, namespace: String = ""): (Option[SimpleRnnBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/simple-rnn-builder/_[0-9]+$".r.pattern
    var count = 0
    var singleDimCount = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            hiddenDim = dims.head
            name = matcher.group(1)
          }
          else {
            require(matcher.group(1) == name)
            if (dims.size == 1)
              singleDimCount += 1
          }
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) = {
        val ratio = count / singleDimCount
        if (count > 0 && count % singleDimCount == 0 && (ratio == 3 || ratio == 4)) {
          val model = new ParameterCollection
          val supportLags = ratio == 4
          val builder = new SimpleRnnBuilder(count / ratio, inputDim, hiddenDim, model, supportLags)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)
      }

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }

  def loadGru(path: String, namespace: String = ""): (Option[GruBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val pattern = "(.*)/gru-builder/_[0-9]+$".r.pattern
    var count = 0
    var inputDim = -1
    var hiddenDim = -1
    var name = ""

    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
      if (objectType == "#Parameter#") {
        val matcher = pattern.matcher(objectName)

        if (matcher.matches) {
          if (count == 0) {
            inputDim = dims.last
            hiddenDim = dims.head
            name = matcher.group(1)
          }
          else
            require(matcher.group(1) == name)
          count += 1
          false
        }
        else true
      }
      else true
    }

    val (optionBuilder, optionModel, expressions) = new ClosableModelLoader(path).autoClose { modelLoader =>
      val expressions = filteredLoadExpressions(path, modelLoader, namespace, modelFilter)
      val (optionModel, optionBuilder) =
        if (count > 0 && count % 9 == 0) {
          val model = new ParameterCollection
          val builder = new GruBuilder(count / 9, inputDim, hiddenDim, model)

          modelLoader.populateModel(model, name)
          (Some(model), Some(builder))
        }
        else
          (None, None)

      (optionBuilder, optionModel, expressions)
    }

    (optionBuilder, optionModel, expressions)
  }
}
