package org.clulab.dynet.models.hot.java

import edu.cmu.dynet.internal.Dim
import edu.cmu.dynet.internal.LookupParameter
import edu.cmu.dynet.internal.Parameter
import edu.cmu.dynet.internal.ParameterCollection
import edu.cmu.dynet.internal.RNNBuilder
import edu.cmu.dynet.internal.VanillaLSTMBuilder
import org.clulab.dynet.models.hot.HotSizes

case class HotModel(
  parameters: ParameterCollection,
  lookupParameters: LookupParameter,
  fwRnnBuilder: RNNBuilder,
  bwRnnBuilder: RNNBuilder,
  H: Parameter,
  O: Parameter,
  T: LookupParameter,
  charLookupParameters: LookupParameter,
  charFwRnnBuilder: RNNBuilder,
  charBwRnnBuilder: RNNBuilder
)

object HotModel {

  def apply(hotSizes: HotSizes = HotSizes.newDefault()): HotModel = {
    // This model is intended to closely resemble one in use at clulab.
    val hs = hotSizes
    val parameters = new ParameterCollection()
    val lookupParameters = parameters.add_lookup_parameters(hs.w2i.size, new Dim(hs.embeddingDim))
    val embeddingSize = hs.embeddingDim + 2 * hs.CHAR_RNN_STATE_SIZE
    val fwBuilder = new VanillaLSTMBuilder(hs.RNN_LAYERS, embeddingSize, hs.RNN_STATE_SIZE, parameters)
    val bwBuilder = new VanillaLSTMBuilder(hs.RNN_LAYERS, embeddingSize, hs.RNN_STATE_SIZE, parameters)
    val H = parameters.add_parameters(new Dim(hs.NONLINEAR_SIZE, 2 * hs.RNN_STATE_SIZE))
    val O = parameters.add_parameters(new Dim(hs.t2i.size, hs.NONLINEAR_SIZE))
    val T = parameters.add_lookup_parameters(hs.t2i.size, new Dim(hs.t2i.size))

    val charLookupParameters = parameters.add_lookup_parameters(hs.c2i.size, new Dim(hs.CHAR_EMBEDDING_SIZE))
    val charFwBuilder = new VanillaLSTMBuilder(hs.CHAR_RNN_LAYERS, hs.CHAR_EMBEDDING_SIZE, hs.CHAR_RNN_STATE_SIZE, parameters)
    val charBwBuilder = new VanillaLSTMBuilder(hs.CHAR_RNN_LAYERS, hs.CHAR_EMBEDDING_SIZE, hs.CHAR_RNN_STATE_SIZE, parameters)

    HotModel(parameters, lookupParameters, fwBuilder, bwBuilder, H, O, T,
        charLookupParameters, charFwBuilder, charBwBuilder)
  }

}
