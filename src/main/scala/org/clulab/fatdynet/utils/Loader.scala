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

//
//  def loadBidirectionalTreeLstm(path: String, namespace: String = ""): (Option[BidirectionalTreeLSTMBuilder], Map[String, Expression]) = {
//    val model = new ParameterCollection
//    var inputDim = -1
//    var hiddenDim = -1
//    var layers = 0
//
//    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
//      if (objectType == "#Parameter#" && objectName.matches(".*/bidirectional-tree-lstm-builder/vanilla-lstm-builder/_[0-9]+$")) {
//        val param = model.addParameters(Dim(dims))
//        modelLoader.populateParameter(param, key = objectName)
//
//        // This is only going to support one model, at least one per namespace.
//        if (layers == 0)
//          inputDim = dims(1)
//        else if (layers == 1)
//          hiddenDim = dims(1)
//        layers += 1
//        false
//      }
//      else
//        true
//    }
//
//    // Could throw exception rather than use option or turn this into a collection.
//    val expressions = filteredLoadExpressions(path, namespace, modelFilter)
//    val builder =
//      if (layers >= 2)
//        Option(new BidirectionalTreeLSTMBuilder(layers - 2, inputDim, hiddenDim, model))
//      else
//        None
//
//    (builder, expressions)
//  }
//
//  def loadSimpleRnn(path: String, namespace: String = ""): (Option[SimpleRnnBuilder], Map[String, Expression]) = {
//    val model = new ParameterCollection
//    var inputDim = -1
//    var hiddenDim = -1
//    var layers = 0
//
//    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
//      if (objectType == "#Parameter#" && objectName.matches(".*/simple-rnn-builder/_[0-9]+$")) {
//        val param = model.addParameters(Dim(dims))
//        modelLoader.populateParameter(param, key = objectName)
//
//        // This is only going to support one model, at least one per namespace.
//        if (layers == 0)
//          inputDim = dims(1)
//        else if (layers == 1)
//          hiddenDim = dims(1)
//        layers += 1
//        false
//      }
//      else
//        true
//    }
//
//    // Could throw exception rather than use option or turn this into a collection.
//    val expressions = filteredLoadExpressions(path, namespace, modelFilter)
//    val builder =
//      if (layers >= 2)
//        Option(new SimpleRnnBuilder(layers - 2, inputDim, hiddenDim, model))
//      else
//        None
//
//    (builder, expressions)
//  }
//
//  def loadGru(path: String, namespace: String = ""): (Option[GruBuilder], Map[String, Expression]) = {
//    val model = new ParameterCollection
//    var inputDim = -1
//    var hiddenDim = -1
//    var layers = 0
//
//    def modelFilter(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]) = {
//      if (objectType == "#Parameter#" && objectName.matches(".*/gru-builder/_[0-9]+$")) {
//        val param = model.addParameters(Dim(dims))
//        modelLoader.populateParameter(param, key = objectName)
//
//        // This is only going to support one model, at least one per namespace.
//        if (layers == 0)
//          inputDim = dims(1)
//        else if (layers == 1)
//          hiddenDim = dims(1)
//        layers += 1
//        false
//      }
//      else
//        true
//    }
//
//    // Could throw exception rather than use option or turn this into a collection.
//    val expressions = filteredLoadExpressions(path, namespace, modelFilter)
//    val builder =
//      if (layers >= 2)
//        Option(new GruBuilder(layers - 2, inputDim, hiddenDim, model))
//      else
//        None
//
//    (builder, expressions)
//  }
//
//  val W2V_SIZE = 1234
//  val VOC_SIZE = 3671
//
//  val WEM_DIMENSIONS = 100
//  val NUM_LAYERS = 1
//  val FF_HIDDEN_DIM = 10
//  val HIDDEN_DIM = 20
//
//  val W_KEY = "/W"
//  val B_KEY = "/b"
//  val V_KEY = "/V"
//  val W2V_WEMB_KEY = "/w2v-wemb"
//  val MISSING_WEB_KEY = "/missing-wemb"
//
//  def write(filename: String): Unit = {
//    val pc = new ParameterCollection
//
//    val W_p: Parameter = pc.addParameters(Dim(Seq(FF_HIDDEN_DIM, HIDDEN_DIM)))
//    val b_p: Parameter = pc.addParameters(Dim(Seq(FF_HIDDEN_DIM)))
//    val V_p: Parameter = pc.addParameters(Dim(Seq(1, FF_HIDDEN_DIM)))
//
//    val w2v_wemb_lp: LookupParameter = pc.addLookupParameters(W2V_SIZE, Dim(Seq(WEM_DIMENSIONS)))
//    val missing_wemb_lp: LookupParameter = pc.addLookupParameters(VOC_SIZE, Dim(Seq(WEM_DIMENSIONS)))
//
//    val model = new ParameterCollection
//    /*val builder = */ new VanillaLstmBuilder(NUM_LAYERS, WEM_DIMENSIONS, HIDDEN_DIM, model)
//
//    new ClosableModelSaver(filename).autoClose { saver =>
//      saver.addParameter(W_p, W_KEY)
//      saver.addParameter(b_p, B_KEY)
//      saver.addParameter(V_p, V_KEY)
//      saver.addLookupParameter(w2v_wemb_lp, W2V_WEMB_KEY)
//      saver.addLookupParameter(missing_wemb_lp, MISSING_WEB_KEY)
//      saver.addModel(model)
//    }
//  }
//
//  // May have to have several versions to accounts for all builders
//  def transduce(builder: RnnBuilder, inputs: Iterable[Expression]): Option[Expression] =
//    inputs.foldLeft(None: Option[Expression]){ (_, input) => Some(builder.addInput(input)) }
//  //        if (inputs.size == 0)
//  //          None
//  //        else {
//  //          inputs.dropRight(1).foreach { builder.addInput(_) }
//  //          Some(builder.addInput(inputs.last))
//  //          }
//
//  def read(filename: String): Unit = {
//    val (optionBuilder, expressions) = loadVanillaLstm(filename)
//
//    expressions.keys.foreach(println)
//
//    val builder = optionBuilder.get
//    val W = expressions(W_KEY)
//    val b = expressions(B_KEY)
//    val V = expressions(V_KEY)
//    val w2v_wemb = expressions(W2V_WEMB_KEY)
//    val missing_wemb = expressions(MISSING_WEB_KEY)
//
//    // An example run...
//    val inputs = 0.until(100) map { index =>
//      if (index % 2 == 0)
//        Expression.pick(w2v_wemb, index % W2V_SIZE, 1) // size implied from eventual dictionary
//      else
//        Expression.pick(missing_wemb, index % VOC_SIZE, 1) // size implied from eventual dictionary
//    }
//
//    builder.newGraph()
//    builder.startNewSequence()
//
//    val selected = transduce(builder, inputs).get
//    val prediction = Expression.logistic(V * (W * selected + b))
//
//    print(prediction.value().toSeq())
//  }
//
//  def main(args: Array[String]): Unit = {
//    val filename = "model.dy.kwa"
//
//    Initialize.initialize(Map("random-seed" -> 2522620396l))
//    write(filename)
//    read(filename)
//  }
}
