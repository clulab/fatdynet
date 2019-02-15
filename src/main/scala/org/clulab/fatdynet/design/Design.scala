package org.clulab.fatdynet.design

import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet._

import scala.collection.mutable.ListBuffer

// fileIndex, nameIndex, partIndex

class Artifact(val parameter: Option[Parameter],
    val lookupParameter: Option[LookupParameter], val rnnBuilder: Option[RnnBuilder]) {

  def this(parameter: Parameter) = this(Some(parameter), None, None)

  def this(lookupParameter: LookupParameter) = this(None, Some(lookupParameter), None)

  def this(rnnBuilder: RnnBuilder) = this(None, None, Some(rnnBuilder))

  def isParameter: Boolean = parameter.isDefined

  def isLookupParameter: Boolean = lookupParameter.isDefined

  def isRnnBuilder: Boolean = rnnBuilder.isDefined
}

abstract class Design(val name: String, val globalIndex: Int, val localIndex: Int) {

  def build(parameterCollection: ParameterCollection): Artifact

  def isReorderable = false

  def getNumber: Option[Int] = {
    val matcher = Design.numberedPattern.matcher(name)

    if (matcher.matches()) Some(matcher.group(1).toInt)
    else None
  }

  def isNumbered: Boolean = getNumber.nonEmpty
}

object Design {
  val numberedPattern = "/_(0|[1-9][0-9]*)$".r.pattern
}

class ParameterDesign(name: String, globalIndex: Int, localIndex: Int, val dims: Dim)
    extends Design(name, globalIndex, localIndex) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(parameterCollection.addParameters(dims))

  override def isReorderable = true
}

class LookupParameterDesign(name: String, globalIndex: Int, localIndex: Int, val n: Long, val dims: Dim)
    extends Design(name, globalIndex, localIndex) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(parameterCollection.addLookupParameters(n, dims))

  override def isReorderable = true
}

abstract class RnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    val layers: Long, val inputDim: Long, val hiddenDim: Long)
    extends Design(name, globalIndex, localIndex)

class CompactVanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class CoupledLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new CoupledLstmBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class FastLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new FastLstmBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class GruBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new GruBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class LstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact  =
      new Artifact(new LstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM))
}

abstract class TreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim)

class BidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class UnidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class SimpleRnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long, val supportLags: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new SimpleRnnBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class VanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Artifact =
      new Artifact(new VanillaLstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM))
}
