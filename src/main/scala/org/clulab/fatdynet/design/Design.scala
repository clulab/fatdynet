package org.clulab.fatdynet.design

import edu.cmu.dynet.ParameterCollection
import edu.cmu.dynet._

import scala.collection.mutable.ListBuffer

// fileIndex, nameIndex, partIndex

abstract class Design(val name: String, val globalIndex: Int, val localIndex: Int) {

  def path: String = name
}

abstract class BuildableDesign[BuildableType](name: String, globalIndex: Int, localIndex: Int)
    extends Design(name, globalIndex, localIndex) {
  def build(parameterCollection: ParameterCollection): BuildableType
}

class ParameterDesign(name: String, globalIndex: Int, localIndex: Int, val dims: Dim)
    extends BuildableDesign[Parameter](name, globalIndex, localIndex) {

  def build(parameterCollection: ParameterCollection): Parameter =
    parameterCollection.addParameters(dims)
}

class LookupParameterDesign(name: String, globalIndex: Int, localIndex: Int, val n: Long, val dims: Dim)
    extends BuildableDesign[LookupParameter](name, globalIndex, localIndex) {

  def build(parameterCollection: ParameterCollection): LookupParameter =
    parameterCollection.addLookupParameters(n, dims)
}

abstract class RnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  val layers: Long, val inputDim: Long, val hiddenDim: Long)
    extends BuildableDesign[RnnBuilder](name, globalIndex, localIndex) {

  def build(parameterCollection: ParameterCollection): RnnBuilder
}

class CompactVanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): CompactVanillaLSTMBuilder =
    new CompactVanillaLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class CoupledLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): CoupledLstmBuilder =
    new CoupledLstmBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class FastLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): FastLstmBuilder =
    new FastLstmBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class GruBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): GruBuilder =
    new GruBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class LstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): LstmBuilder  =
    new LstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM)
}

abstract class TreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {
}

class BidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): BidirectionalTreeLSTMBuilder =
    new BidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class UnidirectionalTreeLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long)
    extends TreeLstmBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): UnidirectionalTreeLSTMBuilder =
    new UnidirectionalTreeLSTMBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class SimpleRnnBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long, val supportLags: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): SimpleRnnBuilder =
    new SimpleRnnBuilder(layers, inputDim, hiddenDim, parameterCollection)
}

class VanillaLstmBuilderDesign(name: String, globalIndex: Int, localIndex: Int,
  layers: Long, inputDim: Long, hiddenDim: Long, val lnLSTM: Boolean)
    extends RnnBuilderDesign(name, globalIndex, localIndex, layers, inputDim, hiddenDim) {

  def build(parameterCollection: ParameterCollection): VanillaLstmBuilder =
    new VanillaLstmBuilder(layers, inputDim, hiddenDim, parameterCollection, lnLSTM)
}

class CompositeBuild

class CompositeDesign(name: String, globalIndex: Int, localIndex: Int)
    extends BuildableDesign[CompositeBuild](name, globalIndex, localIndex) {
  val parameterDesigns: Seq[ParameterDesign] = ListBuffer.empty
  val lookupParameterDesigns: Seq[LookupParameterDesign] = ListBuffer.empty
  val rnnBuilderDesigns: Seq[RnnBuilderDesign] = ListBuffer.empty

  // Needs to put these into right order
  def build(parameterCollection: ParameterCollection): CompositeBuild = null

  //  // Maybe only have on RnnDesign as top level
  //  val compactVanillaLstmDesigns: Seq[CompactVanillaLstmBuilderDesign] = ListBuffer.empty
  //  val coupledLstmDesigns: Seq[CoupledLstmBuilderDesign] = ListBuffer.empty
  //  val fastLstmDesigns: Seq[FastLstmBuilderDesign] = ListBuffer.empty
  //  val gruDesigns: Seq[GruBuilderDesign] = ListBuffer.empty
  //  val lstmDesigns: Seq[LstmBuilderDesign] = ListBuffer.empty
  //  val bidirectionalTreeLstmDesigns: Seq[BidirectionalTreeLstmBuilderDesign] = ListBuffer.empty
  //  val unidirectionalTreeLstmDesigns: Seq[UnidirectionalTreeLstmBuilderDesign] = ListBuffer.empty
  //  val simpleRnnDesigns: Seq[SimpleRnnBuilderDesign] = ListBuffer.empty
  //  val vanillaLstmDesigns: Seq[VanillaLstmBuilderDesign] = ListBuffer.empty
}
