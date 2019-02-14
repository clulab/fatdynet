package org.clulab.fatdynet.design

import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet._

import scala.collection.mutable.ListBuffer

// fileIndex, nameIndex, partIndex

abstract class Design(val designType: Int, val name: String, val globalIndex: Int, val localIndex: Int) {

  def buildParameter(parameterCollection: ParameterCollection): Option[Parameter] = None

  def buildLookupParameter(parameterCollection: ParameterCollection): Option[LookupParameter] = None

  def buildRnnBuilder(parameterCollection: ParameterCollection): Option[RnnBuilder] = None
}

object Design {
  val parameterType: Int = 1
  val lookupParameterType: Int = 2
  val rnnBuilderType: Int = 3
}

abstract class BuildableDesign[BuildableType](designType: Int, name: String, globalIndex: Int, localIndex: Int)
    extends Design(designType, name, globalIndex, localIndex) {
//  def build(parameterCollection: ParameterCollection): BuildableType
}

class ParameterDesign(name: String, globalIndex: Int, localIndex: Int, val dims: Dim)
    extends BuildableDesign[Parameter](Design.parameterType, name, globalIndex, localIndex) {

  override def buildParameter(parameterCollection: ParameterCollection): Option[Parameter] =
      build(parameterCollection)

  def build(parameterCollection: ParameterCollection): Option[Parameter] =
      Some(parameterCollection.addParameters(dims))
}

class LookupParameterDesign(name: String, globalIndex: Int, localIndex: Int, val n: Long, val dims: Dim)
    extends BuildableDesign[LookupParameter](Design.lookupParameterType, name, globalIndex, localIndex) {

  override def buildLookupParameter(parameterCollection: ParameterCollection): Option[LookupParameter] =
      build(parameterCollection)

  def build(parameterCollection: ParameterCollection): Option[LookupParameter] =
      Some(parameterCollection.addLookupParameters(n, dims))
}

abstract class RnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    val layers: Long, val inputDim: Long, val hiddenDim: Long)
    extends BuildableDesign[RnnBuilder](Design.rnnBuilderType, name, globalIndex, localIndex) {

  override def buildRnnBuilder(parameterCollection: ParameterCollection): Option[RnnBuilder] =
      build(parameterCollection)

  def build(parameterCollection: ParameterCollection): Option[RnnBuilder] = None
}

class CompactVanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[CompactVanillaLSTMBuilder] =
      Some(new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class CoupledLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[CoupledLstmBuilder] =
      Some(new CoupledLstmBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class FastLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[FastLstmBuilder] =
      Some(new FastLstmBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class GruBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[GruBuilder] =
      Some(new GruBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class LstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[LstmBuilder]  =
      Some(new LstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM))
}

abstract class TreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {
}

class BidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[BidirectionalTreeLSTMBuilder] =
      Some(new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class UnidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[UnidirectionalTreeLSTMBuilder] =
      Some(new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class SimpleRnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long, val supportLags: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[SimpleRnnBuilder] =
      Some(new SimpleRnnBuilder(layers, inputDim, hiddenDim, parameterCollection))
}

class VanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
    layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  override def build(parameterCollection: ParameterCollection): Option[VanillaLstmBuilder] =
      Some(new VanillaLstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM))
}
