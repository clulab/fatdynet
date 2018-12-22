package org.clulab.fatdynet.utils

import java.util.regex.Pattern

import edu.cmu.dynet.{
  Dim,
  Expression,
  ModelLoader,
  ModelSaver,
  ParameterCollection,

  FastLstmBuilder,
  LstmBuilder,
  CompactVanillaLSTMBuilder,
  CoupledLstmBuilder,
  VanillaLstmBuilder,

  // TreeLSTMBuilder, // abstract
    UnidirectionalTreeLSTMBuilder,
    BidirectionalTreeLSTMBuilder,

  RnnBuilder, // abstract
  SimpleRnnBuilder,

  GruBuilder
}
import org.clulab.fatdynet.utils.Closer.AutoCloser

import scala.collection.mutable
import scala.io.Source

/**
  * See https://dynet.readthedocs.io/en/latest/python_saving_tutorial.html
  */
abstract class Loader(path: String, namespace: String) {

  def namespaceFilter(objectName: String): Boolean = objectName.startsWith(namespace)

  def expressionFilter(objectType: String, objectName: String): Boolean =
      (objectType == "#Parameter#" || objectType == "#LookupParameter#") && !objectName.matches(".*/_[0-9]+$")

  def modelFilter(objectType: String, objectName: String): Boolean
  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder]

  protected def readExpression(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int],
      expressions: mutable.Map[String, Expression]): Unit = {
    objectType match {
      case "#Parameter#" =>
        val pc = new ParameterCollection()
        val param = pc.addParameters(Dim(dims))
        modelLoader.populateParameter(param, key = objectName)
        expressions(objectName) = Expression.parameter(param)
      case "#LookupParameter#" =>
        val pc = new ParameterCollection()
        val param = pc.addLookupParameters(dims.last, Dim(dims.dropRight(1)))
        modelLoader.populateLookupParameter(param, key = objectName)
        expressions(objectName) = Expression.parameter(param)
      case _ => throw new RuntimeException(s"Unrecognized object type '$objectType'")
    }
  }

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit

  protected def readLine(line: String, modelLoader: ModelLoader, expressions: mutable.Map[String, Expression],
      useExpressions: Boolean, useBuilder: Boolean, useModel: Boolean): Unit = {
    val Array(objectType, objectName, dimension, _, _) = line.split(" ")
    // Skip leading { and trailing }
    val dims = dimension.substring(1, dimension.length - 1).split(",").map(_.toInt)

    if (namespaceFilter(objectName)) {
      if (modelFilter(objectType, objectName) && (useBuilder || useModel))
        readModel(modelLoader, objectType, objectName, dims)
      else if (expressionFilter(objectType, objectName) && useExpressions)
        readExpression(modelLoader, objectType, objectName, dims, expressions)
    }
  }

  protected def load(useExpressions: Boolean, useBuilder: Boolean, useModel: Boolean):
      (Map[String, Expression], Option[RnnBuilder], ParameterCollection) = {
    new Loader.ClosableModelLoader(path).autoClose { modelLoader =>
      Source.fromFile(path).autoClose { source =>
        val expressions = mutable.Map[String, Expression]()

        source
            .getLines
            .filter(_.startsWith("#"))
            .foreach(readLine(_, modelLoader, expressions, useExpressions, useBuilder, useModel))

        val model = new ParameterCollection
        val optionBuilder = if (useBuilder) newBuilder(modelLoader, model) else None

        (expressions.toMap, optionBuilder, model)
      }
    }
  }

  def loadExpressions(): Map[String, Expression] = {
    val (expressions, _, _) = load(useExpressions = true, useBuilder = false, useModel = false)
    expressions
  }

  def loadBuilder(): Option[RnnBuilder] = {
    val (_, builder, _) = load(useExpressions = false, useBuilder = true, useModel = false)
    builder
  }

  def loadExpressionsAndBuilder: (Map[String, Expression], Option[RnnBuilder]) = {
    val (expressions, builder, _) = load(useExpressions = true, useBuilder = true, useModel = true)
    (expressions, builder)
  }

  def loadExpressionsAndBuilderAndModel: (Map[String, Expression], Option[RnnBuilder], ParameterCollection) = {
    load(useExpressions = true, useBuilder = true, useModel = true)
  }
}

class ExpressionLoader(path: String, namespace: String) extends Loader(path, namespace) {

  def modelFilter(objectType: String, objectName: String): Boolean = !expressionFilter(objectType, objectName)

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit = ()

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] = None
}

abstract class SimpleLoader(path: String, namespace: String) extends Loader(path, namespace) {
  protected val pattern: Pattern
  protected var count: Int = 0
  protected var inputDim: Int = -1
  protected var hiddenDim: Int = -1
  protected var name: String = ""

  def modelFilter(objectType: String, objectName: String): Boolean =
      objectType == "#Parameter#" && pattern.matcher(objectName).matches

  protected def readModelSingleLine(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit = {
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
    }
  }

  protected def readModelDoubleLine(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit = {
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
    }
  }

  protected def populate(builder: RnnBuilder, modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] = {
    modelLoader.populateModel(model, name)
    Some(builder)
  }
}

abstract class ComplexLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val lnLstmPattern: Pattern = Loader.lnLstmPattern
  protected var lnLstmCount = 0

  override def modelFilter(objectType: String, objectName: String): Boolean =
    objectType == "#Parameter#" && (pattern.matcher(objectName).matches || lnLstmPattern.matcher(objectName).matches)

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit = {
    val matcher = pattern.matcher(objectName)
    val lnLstmMatcher = lnLstmPattern.matcher(objectName)

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
    }
    else if (count > 0 && lnLstmMatcher.matches && lnLstmMatcher.group(1) == name)
      lnLstmCount += 1
  }
}

class FastLstmLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.fastLstmLoaderPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
      readModelSingleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 11 == 0)
        populate(new FastLstmBuilder(count / 11, inputDim, hiddenDim, model), modelLoader, model)
      else
        None
}

class LstmLoader(path: String, namespace: String) extends ComplexLoader(path, namespace) {
  protected val pattern: Pattern = Loader.lstmLoaderPattern

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 3 == 0 && (lnLstmCount == 0 || lnLstmCount == count * 2))
        populate(new LstmBuilder(count / 3, inputDim, hiddenDim, model, lnLstmCount > 0), modelLoader, model)
      else
        None
}

class CompactVanillaLstmLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.compactVanillaLstmPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
      readModelDoubleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 3 == 0)
        populate(new CompactVanillaLSTMBuilder(count / 3, inputDim, hiddenDim, model), modelLoader, model)
      else
        None
}

class CoupledLstmLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.coupledLstmPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
      readModelSingleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 11 == 0)
        populate(new CoupledLstmBuilder(count / 11, inputDim, hiddenDim, model), modelLoader, model)
      else
        None
}

class VanillaLstmLoader(path: String, namespace: String) extends ComplexLoader(path, namespace) {
  protected val pattern: Pattern = Loader.vanillaLstmPattern

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 3 == 0 && (lnLstmCount == 0 || lnLstmCount == count * 2))
        populate(new VanillaLstmBuilder(count / 3, inputDim, hiddenDim, model, lnLstmCount > 0), modelLoader, model)
      else
        None
}

class UnidirectionalTreeLstmLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.unidirectionalTreeLstmPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
    readModelDoubleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 3 == 0)
        populate(new UnidirectionalTreeLSTMBuilder(count / 3, inputDim, hiddenDim, model), modelLoader, model)
      else
        None
}

class BidirectionalTreeLstmLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.bidirectionalTreeLstmPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
      readModelSingleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 6 == 0)
        populate(new BidirectionalTreeLSTMBuilder(count / 6, inputDim, hiddenDim / 2, model), modelLoader, model)
      else
        None
}

class SimpleRnnLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.simpleRnnPattern
  protected var singleDimCount = 0

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit = {
    val matcher = pattern.matcher(objectName)

    if (matcher.matches) {
      if (count == 0) {
        inputDim = dims.last
        hiddenDim = dims.head
        name = matcher.group(1)
      }
      else {
        require(matcher.group(1) == name)
        if (dims.length == 1)
          singleDimCount += 1
      }
      count += 1
    }
  }

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] = {
    val ratio = count / singleDimCount
    val supportLags = ratio == 4

    if (count > 0 && count % singleDimCount == 0 && (ratio == 3 || ratio == 4))
      populate(new SimpleRnnBuilder(count / ratio, inputDim, hiddenDim, model, supportLags), modelLoader, model)
    else
      None
  }
}

class GruLoader(path: String, namespace: String) extends SimpleLoader(path, namespace) {
  protected val pattern: Pattern = Loader.gruPattern

  protected def readModel(modelLoader: ModelLoader, objectType: String, objectName: String, dims: Array[Int]): Unit =
      readModelSingleLine(modelLoader, objectType, objectName, dims)

  def newBuilder(modelLoader: ModelLoader, model: ParameterCollection): Option[RnnBuilder] =
      if (count > 0 && count % 9 == 0)
        populate(new GruBuilder(count / 9, inputDim, hiddenDim, model), modelLoader, model)
      else
        None
}

object Loader {
  lazy val fastLstmLoaderPattern: Pattern = "(.*)/fast-lstm-builder/_[0-9]+$".r.pattern
  lazy val lstmLoaderPattern: Pattern = "(.*)/vanilla-lstm-builder/_[0-9]+$".r.pattern
  lazy val compactVanillaLstmPattern: Pattern = "(.*)/compact-vanilla-lstm-builder/_[0-9]+$".r.pattern
  lazy val coupledLstmPattern: Pattern = "(.*)/lstm-builder/_[0-9]+$".r.pattern
  lazy val vanillaLstmPattern: Pattern = "(.*)/vanilla-lstm-builder/_[0-9]+$".r.pattern
  lazy val unidirectionalTreeLstmPattern: Pattern = "(.*)/unidirectional-tree-lstm-builder/vanilla-lstm-builder/_[0-9]+$".r.pattern
  lazy val bidirectionalTreeLstmPattern: Pattern = "(.*)/bidirectional-tree-lstm-builder/vanilla-lstm-builder(_1)?/_[0-9]+$".r.pattern
  lazy val simpleRnnPattern: Pattern = "(.*)/simple-rnn-builder/_[0-9]+$".r.pattern
  lazy val gruPattern: Pattern = "(.*)/gru-builder/_[0-9]+$".r.pattern

  lazy val lnLstmPattern: Pattern = "(.*)/_[0-9]+$".r.pattern

  class ClosableModelLoader(filename: String) extends ModelLoader(filename) {
    def close(): Unit = done
  }

  class ClosableModelSaver(filename: String) extends ModelSaver(filename) {
    def close(): Unit = done
  }

  def loadExpressions(path: String, namespace: String = ""): Map[String, Expression] = {
    new ExpressionLoader(path, namespace).loadExpressions
  }

  def loadFastLstm(path: String, namespace: String = ""): (Option[FastLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new FastLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[FastLstmBuilder]), Some(model), expressions)
  }

  def loadLstm(path: String, namespace: String = ""): (Option[LstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new LstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[LstmBuilder]), Some(model), expressions)
  }

  def loadCompactVanillaLstm(path: String, namespace: String = ""): (Option[CompactVanillaLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new CompactVanillaLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[CompactVanillaLSTMBuilder]), Some(model), expressions)
  }

  def loadCoupledLstm(path: String, namespace: String = ""): (Option[CoupledLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new CoupledLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[CoupledLstmBuilder]), Some(model), expressions)
  }

  def loadVanillaLstm(path: String, namespace: String = ""): (Option[VanillaLstmBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new VanillaLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[VanillaLstmBuilder]), Some(model), expressions)
  }

  def loadUnidirectionalTreeLstm(path: String, namespace: String = ""): (Option[UnidirectionalTreeLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new UnidirectionalTreeLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[UnidirectionalTreeLSTMBuilder]), Some(model), expressions)
  }

  def loadBidirectionalTreeLstm(path: String, namespace: String = ""): (Option[BidirectionalTreeLSTMBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new BidirectionalTreeLstmLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[BidirectionalTreeLSTMBuilder]), Some(model), expressions)
  }

  def loadSimpleRnn(path: String, namespace: String = ""): (Option[SimpleRnnBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new SimpleRnnLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[SimpleRnnBuilder]), Some(model), expressions)
  }

  def loadGru(path: String, namespace: String = ""): (Option[GruBuilder], Option[ParameterCollection], Map[String, Expression]) = {
    val (expressions, someBuilder: Option[RnnBuilder], model) = new GruLoader(path, namespace).loadExpressionsAndBuilderAndModel
    (someBuilder.map(_.asInstanceOf[GruBuilder]), Some(model), expressions)
  }
}
