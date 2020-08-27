package org.clulab.fatdynet.expr

import edu.cmu.dynet.{ComputationGraph => ScalaComputationGraph}
import edu.cmu.dynet.Dim
import edu.cmu.dynet.Expression
import edu.cmu.dynet.{ExpressionVector => ScalaExpressionVector}
import edu.cmu.dynet.{Expression => ScalaExpression}
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.IntVector
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.UnsignedPointer
import edu.cmu.dynet.UnsignedVector
import edu.cmu.dynet.UnsignedVectorVector
import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.internal.{Expression => JavaExpression}
import edu.cmu.dynet.internal.{ExpressionVector => JavaExpressionVector}
import org.clulab.fatdynet.cg.DynamicComputationGraph

class DynamicExpressionFactory(computationGraph: DynamicComputationGraph) extends ExpressionFactory[FatExpression] {

  def newFatExpression(scalaExpression: ScalaExpression): FatExpression = {
    new FatExpression(this, scalaExpression)
  }

  // This part below was copied verbatim from object Expression and then changed as follows:
  // 1. Change Expression to FatExpression, but not ExpressionVector or internal.Expression.

  import edu.cmu.dynet.internal.{dynet_swig => dn}

  /** Private helper function for wrapping methods that get expressions from the computation
    * graph */
  private def makeExpr(
    f: JavaComputationGraph => JavaExpression,
    references: Seq[AnyRef] = Seq.empty
  ): FatExpression = {
   // val version = ComputationGraph.version
    val expr = f(computationGraph)
    newFatExpression(new ScalaExpression(expr, references)) // was "new Expression(expr, references)"
  }

  def input(s: Float): FatExpression = makeExpr(cg => dn.input(computationGraph, s))
  def input(fp: FloatPointer): FatExpression =
    makeExpr(cg => dn.input(computationGraph, fp.floatp), Seq(fp))
  def input(d: Dim, pdata: FloatVector): FatExpression =
    makeExpr(cg => dn.input(cg, d.dim, pdata.vector), Seq(d, pdata))
  def input(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0f) =
    makeExpr(cg => dn.input(cg, d.dim, ids.vector, data.vector, defdata), Seq(d, ids, data))

  def parameter(p: Parameter): FatExpression = makeExpr(cg => dn.parameter(cg, p.parameter), Seq(p))
  def parameter(lp: LookupParameter): FatExpression = makeExpr(cg => dn.parameter(cg, lp.lookupParameter), Seq(lp))
  def constParameter(p: Parameter): FatExpression =
    makeExpr(cg => dn.const_parameter(cg, p.parameter), Seq(p))
  def constParameter(lp: LookupParameter): FatExpression =
    makeExpr(cg => dn.const_parameter(cg, lp.lookupParameter), Seq(lp))

  def lookup(p: LookupParameter, index: Long) =
    makeExpr(cg => dn.lookup(cg, p.lookupParameter, index), Seq(p))
  def lookup(p: LookupParameter, pindex: UnsignedPointer) =
    makeExpr(cg => dn.lookup(cg, p.lookupParameter, pindex.uintp), Seq(p, pindex))
  def constLookup(p: LookupParameter, index: Long) =
    makeExpr(cg => dn.const_lookup(cg, p.lookupParameter, index), Seq(p))
  def constLookup(p: LookupParameter, pindex: UnsignedPointer) =
    makeExpr(cg => dn.const_lookup(cg, p.lookupParameter, pindex.uintp), Seq(p, pindex))
  def lookup(p: LookupParameter, indices: UnsignedVector) =
    makeExpr(cg => dn.lookup(cg, p.lookupParameter, indices.vector), Seq(p, indices))
  def constLookup(p: LookupParameter, indices: UnsignedVector) =
    makeExpr(cg => dn.const_lookup(cg, p.lookupParameter, indices.vector), Seq(p, indices))

  def zeros(d: Dim) = makeExpr(cg => dn.zeros(cg, d.dim), Seq(d))
  def zeroes(d: Dim) = makeExpr(cg => dn.zeros(cg, d.dim), Seq(d))
  def ones(d: Dim) = makeExpr(cg => dn.ones(cg, d.dim), Seq(d))
  def constant(d: Dim, v: Float) = makeExpr(cg => dn.constant(cg, d.dim, v), Seq(d))
  def randomNormal(d: Dim) = makeExpr(cg => dn.random_normal(cg, d.dim), Seq(d))
  def randomBernoulli(d: Dim, p: Float, scale: Float = 1.0f) = makeExpr(
    cg => dn.random_bernoulli(cg, d.dim, p, scale), Seq(d))
  def randomUniform(d: Dim, left: Float, right: Float) = makeExpr(
    cg => dn.random_uniform(cg, d.dim, left, right), Seq(d))
  def randomGumbel(d: Dim, mu: Float, beta: Float) = makeExpr(
    cg => dn.random_gumbel(cg, d.dim, mu, beta), Seq(d))

  /* ARITHMETIC OPERATIONS */

  private type BinaryTransform = (JavaExpression, JavaExpression) => JavaExpression
  private def binary(e1: FatExpression, e2: FatExpression, combiner: BinaryTransform) = {
    // e1.ensureFresh() // fatdynet remove
    // e2.ensureFresh() // fatdynet remove
    val expr = combiner(e1.javaExpression, e2.javaExpression)
    // Specify e1 and e2 as references so they can't get prematurely garbage collected.
    newFatExpression(new Expression(expr, Seq(e1, e2))) // was "new Expression(expr, Seq(e1, e2))"
  }

  private type UnaryTransform = JavaExpression => JavaExpression
  private def unary(e: FatExpression, transformer: UnaryTransform) = {
    // e.ensureFresh() // fatdynet remove
    // Specify e as reference so it can't get prematurely garbage collected.
    newFatExpression(new Expression(transformer(e.javaExpression), Seq(e)))
  }

  def exprMinus(e: FatExpression): FatExpression = unary(e, dn.exprMinus)
  def exprPlus(e1: FatExpression, e2: FatExpression): FatExpression = binary(e1, e2, dn.exprPlus)
  def exprPlus(e1: FatExpression, x: Float): FatExpression = unary(e1, e1 => dn.exprPlus(e1, x))
  def exprPlus(x: Float, e2: FatExpression): FatExpression = unary(e2, e2 => dn.exprPlus(x, e2))
  def exprMinus(e1: FatExpression, e2: FatExpression): FatExpression = binary(e1, e2, dn.exprMinus)
  def exprMinus(e1: FatExpression, x: Float): FatExpression = unary(e1, e1 => dn.exprMinus(e1, x))
  def exprMinus(x: Float, e2: FatExpression): FatExpression = unary(e2, e2 => dn.exprMinus(x, e2))
  def exprTimes(e1: FatExpression, e2: FatExpression): FatExpression = binary(e1, e2, dn.exprTimes)
  def exprTimes(e1: FatExpression, x: Float): FatExpression = unary(e1, e1 => dn.exprTimes(e1, x))
  def exprTimes(x: Float, e2: FatExpression): FatExpression = unary(e2, e2 => dn.exprTimes(x, e2))
  def exprDivide(e1: FatExpression, x: Float): FatExpression = unary(e1, e1 => dn.exprDivide(e1, x))

  private type VectorTransform = JavaExpressionVector => JavaExpression
  private def vectory(v: ScalaExpressionVector, transformer: VectorTransform): FatExpression = {
    // DyNet segfaults if we pass a zero-length vector.
    // This check results in a nicer error message.
    assert(v.nonEmpty, "Operation requires > 0 expression arguments")
    v.ensureFresh()
    // Specify v as reference so it can't get prematurely garbage collected.
    newFatExpression(new Expression(transformer(v.vector), Seq(v)))
  }

  def affineTransform(ev: ScalaExpressionVector): FatExpression = vectory(ev, dn.affine_transform)
  def affineTransform(exprs: FatExpression*): FatExpression =
      affineTransform(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  def sum(ev: ScalaExpressionVector): FatExpression = vectory(ev, dn.sum)
  def sum(exprs: FatExpression*): FatExpression = sum(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  def sumElems(e: FatExpression): FatExpression = unary(e, dn.sum_elems)
  def momentElems(e: FatExpression, r: Long) = unary(e, e => dn.moment_elems(e, r))
  def meanElems(e: FatExpression): FatExpression = unary(e, dn.mean_elems)
  def stdElems(e: FatExpression): FatExpression = unary(e, dn.std_elems)

  def average(ev: ScalaExpressionVector): FatExpression = vectory(ev, dn.average)
  def average(exprs: FatExpression*): FatExpression = average(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  def sqrt(e: FatExpression): FatExpression = unary(e, dn.sqrt)
  def abs(e: FatExpression): FatExpression = unary(e, dn.abs)
  def erf(e: FatExpression): FatExpression = unary(e, dn.erf)
  def tanh(e: FatExpression): FatExpression = unary(e, dn.tanh)
  def exp(e: FatExpression): FatExpression = unary(e, dn.exp)
  def square(e: FatExpression): FatExpression = unary(e, dn.square)
  def cube(e: FatExpression): FatExpression = unary(e, dn.cube)
  def lgamma(e: FatExpression): FatExpression = unary(e, dn.lgamma)
  def log(e: FatExpression): FatExpression = unary(e, dn.log)
  def logistic(e: FatExpression): FatExpression = unary(e, dn.logistic)
  def rectify(e: FatExpression): FatExpression = unary(e, dn.rectify)
  def elu(e: FatExpression): FatExpression = unary(e, dn.elu)
  def selu(e: FatExpression): FatExpression = unary(e, dn.selu)
  def softsign(e: FatExpression): FatExpression = unary(e, dn.softsign)
  def pow(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.pow)

  def min(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.min)

  def max(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.max)
  def max(v: ScalaExpressionVector): FatExpression = vectory(v, dn.max)
  def dotProduct(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.dot_product)
  def cmult(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.cmult)
  def cdiv(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.cdiv)
  def colwiseAdd(x: FatExpression, bias: FatExpression): FatExpression = binary(x, bias, dn.colwise_add)

  /* PROBABILITY / LOSS OPERATIONS */

  def softmax(e: FatExpression, d: Long = 0L): FatExpression = unary(e, e => dn.softmax(e, d))
  def logSoftmax(e: FatExpression): FatExpression = unary(e, dn.log_softmax)
  def logSoftmax(e: FatExpression, restriction: UnsignedVector) =
    unary(e, e => dn.log_softmax(e, restriction.vector))

  def logSumExp(v: ScalaExpressionVector): FatExpression = vectory(v, dn.logsumexp)

  def pickNegLogSoftmax(e: FatExpression, v: Long): FatExpression = unary(e, e => dn.pickneglogsoftmax(e, v))
  def pickNegLogSoftmax(e: FatExpression, v: UnsignedPointer): FatExpression =
    unary(e, e => dn.pickneglogsoftmax(e, v.uintp))
  def pickNegLogSoftmax(e: FatExpression, v: UnsignedVector): FatExpression =
    unary(e, e => dn.pickneglogsoftmax(e, v.vector))

  def hinge(e: FatExpression, index: Long, m: Float = 1.0f): FatExpression = unary(e, e => dn.hinge(e, index, m))
  def hinge(e: FatExpression, index: UnsignedPointer, m: Float): FatExpression =
    unary(e, e => dn.hinge(e, index.uintp, m))
  def hinge(e: FatExpression, indices: UnsignedVector, m: Float): FatExpression =
    unary(e, e => dn.hinge(e, indices.vector, m))

  def hinge(e: FatExpression, index: UnsignedPointer): FatExpression =
    unary(e, e => dn.hinge(e, index.uintp, 1.0f))
  def hinge(e: FatExpression, indices: UnsignedVector): FatExpression =
    unary(e, e => dn.hinge(e, indices.vector, 1.0f))

  def hingeDim(e: FatExpression, indices: UnsignedVector, d: Long = 0L, m: Float = 1.0f): FatExpression =
    unary(e, e => dn.hinge_dim(e, indices.vector, d, m))
  def hingeDimBatch(e: FatExpression, indices: UnsignedVectorVector, d: Long = 0L, m: Float = 1.0f): FatExpression =
    unary(e, e => dn.hinge_dim(e, indices.vector, d, m))

  def sparsemax(e: FatExpression): FatExpression = unary(e, dn.sparsemax)
  def sparsemaxLoss(e: FatExpression, targetSupport: UnsignedVector): FatExpression =
    unary(e, e => dn.sparsemax_loss(e, targetSupport.vector))

  def squaredNorm(e: FatExpression): FatExpression = unary(e, dn.squared_norm)
  def l2Norm(e: FatExpression): FatExpression = unary(e, dn.l2_norm)
  def squaredDistance(e1: FatExpression, e2: FatExpression): FatExpression = binary(e1, e2, dn.squared_distance)
  def l1Distance(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.l1_distance)
  def huberDistance(x: FatExpression, y: FatExpression, c: Float = 1.345f) = {
    binary(x, y, (x, y) => dn.huber_distance(x, y, c))
  }
  def binaryLogLoss(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.binary_log_loss)
  def pairwiseRankLoss(x: FatExpression, y: FatExpression, m: Float = 1.0f) =
    binary(x, y, (x, y) => dn.pairwise_rank_loss(x, y, m))
  def poissonLoss(x: FatExpression, y: Long): FatExpression = unary(x, x => dn.poisson_loss(x, y))
  def poissonLoss(x: FatExpression, y: UnsignedPointer): FatExpression =
    unary(x, x => dn.poisson_loss(x, y.uintp))

  /* FLOW / SHAPING OPERATIONS */

  def noBackProp(x: FatExpression): FatExpression = unary(x, dn.nobackprop)
  def flipGradient(x: FatExpression): FatExpression = unary(x, dn.flip_gradient)
  def reshape(x: FatExpression, d: Dim): FatExpression = unary(x, x => dn.reshape(x, d.dim))
  def transpose(x: FatExpression): FatExpression = unary(x, dn.transpose)
  def selectRows(x: FatExpression, rows: UnsignedVector): FatExpression =
    unary(x, x => dn.select_rows(x, rows.vector))
  def selectCols(x: FatExpression, rows: UnsignedVector): FatExpression =
    unary(x, x => dn.select_cols(x, rows.vector))
  def sumBatches(x: FatExpression): FatExpression = unary(x, dn.sum_batches)
  def momentBatches(x: FatExpression, r: Long): FatExpression = unary(x, x => dn.moment_batches(x, r))
  def stdBatches(x: FatExpression): FatExpression = unary(x, dn.std_batches)
  def momentDim(x: FatExpression, v: UnsignedVector, r: Long, b: Boolean = false, n: Long = 0L): FatExpression =
    unary(x, x => dn.moment_dim(x, v.vector, r, b, n))
  def meanDim(x: FatExpression, v: UnsignedVector, b: Boolean = false, n: Long = 0L): FatExpression =
    unary(x, x => dn.mean_dim(x, v.vector, b, n))
  def stdDim(x: FatExpression, v: UnsignedVector, b: Boolean = false, n: Long = 0L): FatExpression =
    unary(x, x => dn.std_dim(x, v.vector, b, n))

  def pick(x: FatExpression, v: Long, d: Long = 0l): FatExpression = unary(x, x => dn.pick(x, v, d))
  def pick(x: FatExpression, v: UnsignedVector, d: Long): FatExpression =
    unary(x, x => dn.pick(x, v.vector, d))
  def pick(x: FatExpression, v: UnsignedPointer, d: Long): FatExpression =
    unary(x, x => dn.pick(x, v.uintp, d))
  def pickrange(x: FatExpression, v: Long, u: Long, d: Long = 0l): FatExpression =
    unary(x, x => dn.pick_range(x, v, u, d))
  def pickBatchElem(x: FatExpression, v: Long): FatExpression = unary(x, x => dn.pick_batch_elem(x, v))
  def pickBatchElems(x: FatExpression, v: UnsignedVector): FatExpression =
    unary(x, x => dn.pick_batch_elems(x, v.vector))

  def concatenateToBatch(v: ScalaExpressionVector): FatExpression = vectory(v, dn.concatenate_to_batch)
  def concatenateToBatch(exprs: FatExpression*): FatExpression = concatenateToBatch(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  def stridedSelect(x:FatExpression, strides:IntVector, from:IntVector, to:IntVector):FatExpression = {
    unary(x, x => dn.strided_select(x, strides.vector, from.vector, to.vector))
  }
  def stridedSelect(x:FatExpression, strides:Seq[Int], from:Seq[Int], to:Seq[Int]):FatExpression =
    stridedSelect(x, new IntVector(strides), new IntVector(from), new IntVector(to))

  def concatenateCols(v: ScalaExpressionVector): FatExpression = vectory(v, dn.concatenate_cols)
  def concatenateCols(exprs: FatExpression*): FatExpression = concatenateCols(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  def concatenate(v: ScalaExpressionVector): FatExpression = vectory(v, dn.concatenate)
  def concatenate(exprs: FatExpression*): FatExpression = concatenate(new ScalaExpressionVector(exprs.map(_.scalaExpression)))

  /* NOISE OPERATIONS */

  def noise(x: FatExpression, stddev: Float): FatExpression = unary(x, x => dn.noise(x, stddev))
  def dropout(x: FatExpression, p: Float): FatExpression = unary(x, x => dn.dropout(x, p))
  def dropoutDim(x: FatExpression, d: Long, p: Float): FatExpression = unary(x, x => dn.dropout_dim(x, d, p))
  def dropoutBatch(x: FatExpression, p: Float): FatExpression = unary(x, x => dn.dropout_batch(x, p))
  def blockDropout(x: FatExpression, p: Float): FatExpression = unary(x, x => dn.block_dropout(x, p))

  /* CONVOLUTION OPERATIONS */

  // These were commented out in the C++ code.
  //def conv1dNarrow(x: FatExpression, f: FatExpression): FatExpression = binary(x, f, dn.conv1d_narrow)
  //def conv1dWide(x: FatExpression, f: FatExpression): FatExpression = binary(x, f, dn.conv1d_wide)
  def filter1DNarrow(x: FatExpression, f: FatExpression): FatExpression = binary(x, f, dn.filter1d_narrow)
  def kMaxPooling(x: FatExpression, k: Long): FatExpression = unary(x, x => dn.kmax_pooling(x, k))
  def foldRows(x: FatExpression, nRows: Long = 2l): FatExpression = unary(x, x => dn.fold_rows(x, nRows))
  def sumDim(x: FatExpression, dims: UnsignedVector, b: Boolean = false): FatExpression = unary(x, x => dn.sum_dim(x, dims.vector, b))
  def sumCols(x: FatExpression): FatExpression = unary(x, dn.sum_cols)
  def sumRows(x: FatExpression): FatExpression = unary(x, dn.sum_rows)
  def averageCols(x: FatExpression): FatExpression = unary(x, dn.average_cols)
  def kmhNgram(x: FatExpression, n: Long): FatExpression = unary(x, x => dn.kmh_ngram(x, n))

  // In the C++ code, is_valid has a default value of true. Scala won't let you have two overloaded
  // methods with default values, so I just got rid of the default value here.
  // TODO(joelgrus): write tests for these
  def conv2d(x: FatExpression, f: FatExpression, stride: UnsignedVector, isValid: Boolean) =
    newFatExpression(new Expression(dn.conv2d(x.javaExpression, f.javaExpression, stride.vector, isValid), Seq(x, f, stride)))
  def conv2d(x: FatExpression, f: FatExpression, stride: UnsignedVector) =
    newFatExpression(new Expression(dn.conv2d(x.javaExpression, f.javaExpression, stride.vector, true), Seq(x, f, stride)))

  def conv2d(x: FatExpression, f: FatExpression, b: FatExpression, stride: UnsignedVector, isValid: Boolean) =
    newFatExpression(new Expression(dn.conv2d(x.javaExpression, f.javaExpression, b.javaExpression, stride.vector, isValid), Seq(x, f, b, stride)))
  def conv2d(x: FatExpression, f: FatExpression, b: FatExpression, stride: UnsignedVector) =
    newFatExpression(new Expression(dn.conv2d(x.javaExpression, f.javaExpression, b.javaExpression, stride.vector, true), Seq(x, f, b, stride)))

  /* TENSOR OPERATIONS */

  def contract3d1d(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.contract3d_1d)
  def contract3d1d1d(x: FatExpression, y: FatExpression, z: FatExpression): FatExpression = {
    // Seq(x, y, z).foreach(_.ensureFresh)
    newFatExpression(new Expression(dn.contract3d_1d_1d(x.javaExpression, y.javaExpression, z.javaExpression), Seq(x, y, z)))
  }
  def contract3d1d1d(x: FatExpression, y: FatExpression, z: FatExpression, b: FatExpression): FatExpression = {
    // Seq(x, y, z, b).foreach(_.ensureFresh)
    newFatExpression(new Expression(dn.contract3d_1d_1d(x.javaExpression, y.javaExpression, z.javaExpression, b.javaExpression), Seq(x, y, z, b)))
  }
  def contract3d1d(x: FatExpression, y: FatExpression, b: FatExpression): FatExpression = {
    // Seq(x, y, b).foreach(_.ensureFresh)
    newFatExpression(new Expression(dn.contract3d_1d(x.javaExpression, y.javaExpression, b.javaExpression), Seq(x, y, b)))
  }

  /* LINEAR ALGEBRA OPERATIONS */

  def inverse(x: FatExpression): FatExpression = unary(x, dn.inverse)
  def logdet(x: FatExpression): FatExpression = unary(x, dn.logdet)
  def traceOfProduct(x: FatExpression, y: FatExpression): FatExpression = binary(x, y, dn.trace_of_product)

  /* NORMALIZATION OPERATIONS */

  def layerNorm(x: FatExpression, g: FatExpression, b: FatExpression): FatExpression = {
    // Seq(x, g, b).foreach(_.ensureFresh)
    newFatExpression(new Expression(dn.layer_norm(x.javaExpression, g.javaExpression, b.javaExpression), Seq(x, g, b)))
  }
  def weightNorm(w: FatExpression, g: FatExpression): FatExpression = binary(w, g, dn.weight_norm)

  /** Augment numbers so that they can do arithmetic with expressions. */
  implicit class ImplicitNumerics[T](x: T)(implicit n: Numeric[T]) {
    import n._
    def +(e: FatExpression): FatExpression = e.expressionFactory.exprPlus(x.toFloat, e)
    def *(e: FatExpression): FatExpression = e.expressionFactory.exprTimes(x.toFloat, e)
    def -(e: FatExpression): FatExpression = e.expressionFactory.exprMinus(x.toFloat, e)
  }
}
