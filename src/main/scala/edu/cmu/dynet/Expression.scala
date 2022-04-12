package edu.cmu.dynet

/** Represents an expression on the computation graph. Can only be constructed using the
  * functions contained in the companion object.
  */
class Expression private[dynet](
  private[dynet] val expr: internal.Expression,
  // Expressions sometimes rely on things (e.g. wrapped C++ vectors) that get deleted when the JVM
  // garbage collector runs. By explicitly grabbing references to them, we can prevent this
  // premature garbage collection.
  val reference: AnyRef = null
)(implicit val cg: ComputationGraph) {

  def close(): Unit = expr.delete()

  /** Get the tensor value of this expression */
  def value(): Tensor = {
    ensureFresh()
    new Tensor(expr.value)
  }

  /** Get the tensor dimension of this expression */
  def dim(): Dim = new Dim(expr.dim)

  /** Make sure that this expression is the latest version */
  private[dynet] def ensureFresh(cg: ComputationGraph): Unit = {
    if (!this.cg.eq(cg))
      throw new RuntimeException("stale expression")
  }

  // Sugar for doing expression arithmetic
  def +(e2: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprPlus(this, e2)(cg)
  def *(e2: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprTimes(this, e2)(cg)
  def -(e2: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprMinus(this, e2)(cg)
  def +(r: Float)(implicit cg: ComputationGraph): Expression = Expression.exprPlus(this, r)(cg)
  def *(r: Float)(implicit cg: ComputationGraph): Expression = Expression.exprTimes(this, r)(cg)
  def -(r: Float)(implicit cg: ComputationGraph): Expression = Expression.exprMinus(this, r)(cg)
  def /(r: Float)(implicit cg: ComputationGraph): Expression = Expression.exprDivide(this, r)(cg)
  def unary_-(implicit cg: ComputationGraph) : Expression = Expression.exprMinus(this)(cg)

  def debugString(): String = s"(Expression: ${dim().debugString()} ${value().toSeq()})"
}

/** Contains methods for creating [[edu.cmu.dynet.Expression]]s. There are several ways to create
  *  expressions:
  *
  *  * from explicit values (e.g. `input`)
  *  * randomly (e.g. `randomNormal`)
  *  * from [[edu.cmu.dynet.ParameterCollection]] parameters (e.g. `parameter`)
  *  * from other expressions (e.g. `softmax` and `pow`)
  */
object Expression {
  import edu.cmu.dynet.internal.{dynet_swig => dn}

  /** Private helper function for wrapping methods that get expressions from the computation
    * graph */
  private def makeExpr(
    f: ComputationGraph => internal.Expression,
    reference: AnyRef = null
  )(implicit cg: ComputationGraph): Expression = {
    val version = ComputationGraph.version
    val expr = f(cg)
    new Expression(expr, reference)
  }

  def input(s: Float)(implicit cg: ComputationGraph): Expression = makeExpr(cg => dn.input(cg.cg, s))(cg)
  def input(fp: FloatPointer)(implicit cg: ComputationGraph): Expression =
    makeExpr(cg => dn.input(cg.cg, fp.floatp), fp)(cg)
  def input(d: Dim, pdata: FloatVector)(implicit cg: ComputationGraph): Expression =
    makeExpr(cg => dn.input(cg.cg, d.dim, pdata.vector), Seq(d, pdata))(cg)
  def input(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0f)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.input(cg.cg, d.dim, ids.vector, data.vector, defdata), Seq(d, ids, data))(cg)

  def parameter(p: Parameter)(implicit cg: ComputationGraph): Expression = makeExpr(cg => dn.parameter(cg.cg, p.parameter), p)(cg)
  def parameter(lp: LookupParameter)(implicit cg: ComputationGraph): Expression = makeExpr(cg => dn.parameter(cg.cg, lp.lookupParameter), lp)(cg)
  def constParameter(p: Parameter)(implicit cg: ComputationGraph): Expression =
    makeExpr(cg => dn.const_parameter(cg.cg, p.parameter), p)(cg)
  def constParameter(lp: LookupParameter)(implicit cg: ComputationGraph): Expression =
    makeExpr(cg => dn.const_parameter(cg.cg, lp.lookupParameter), lp)(cg)

  def lookup(p: LookupParameter, index: Long)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.lookup(cg.cg, p.lookupParameter, index), p)(cg)
  def lookup(p: LookupParameter, pindex: UnsignedPointer)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.lookup(cg.cg, p.lookupParameter, pindex.uintp), Seq(p, pindex))(cg)
  def constLookup(p: LookupParameter, index: Long)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.const_lookup(cg.cg, p.lookupParameter, index), p)(cg)
  def constLookup(p: LookupParameter, pindex: UnsignedPointer)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.const_lookup(cg.cg, p.lookupParameter, pindex.uintp), Seq(p, pindex))(cg)
  def lookup(p: LookupParameter, indices: UnsignedVector)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.lookup(cg.cg, p.lookupParameter, indices.vector), Seq(p, indices))(cg)
  def constLookup(p: LookupParameter, indices: UnsignedVector)(implicit cg: ComputationGraph) =
    makeExpr(cg => dn.const_lookup(cg.cg, p.lookupParameter, indices.vector), Seq(p, indices))(cg)

  def zeros(d: Dim)(implicit cg: ComputationGraph) = makeExpr(cg => dn.zeros(cg.cg, d.dim), d)(cg)
  def zeroes(d: Dim)(implicit cg: ComputationGraph) = makeExpr(cg => dn.zeros(cg.cg, d.dim), d)(cg)
  def ones(d: Dim)(implicit cg: ComputationGraph) = makeExpr(cg => dn.ones(cg.cg, d.dim), d)(cg)
  def constant(d: Dim, v: Float)(implicit cg: ComputationGraph) = makeExpr(cg => dn.constant(cg.cg, d.dim, v), d)(cg)
  def randomNormal(d: Dim)(implicit cg: ComputationGraph) = makeExpr(cg => dn.random_normal(cg.cg, d.dim), d)(cg)
  def randomBernoulli(d: Dim, p: Float, scale: Float = 1.0f)(implicit cg: ComputationGraph) = makeExpr(
    cg => dn.random_bernoulli(cg.cg, d.dim, p, scale), d)(cg)
  def randomUniform(d: Dim, left: Float, right: Float)(implicit cg: ComputationGraph) = makeExpr(
    cg => dn.random_uniform(cg.cg, d.dim, left, right), d)(cg)
  def randomGumbel(d: Dim, mu: Float, beta: Float)(implicit cg: ComputationGraph) = makeExpr(
    cg => dn.random_gumbel(cg.cg, d.dim, mu, beta), d)(cg)

  /* ARITHMETIC OPERATIONS */

  private type BinaryTransform = (internal.Expression, internal.Expression) => internal.Expression
  private def binary(e1: Expression, e2: Expression, combiner: BinaryTransform)(implicit cg: ComputationGraph) = {
    // TODO: Check that the computation graph of cg, e1, and e2 are all the same
    e1.ensureFresh(cg)
    e2.ensureFresh(cg)
    val expr = combiner(e1.expr, e2.expr)
    // Specify e1 and e2 as references so they can't get prematurely garbage collected.
    new Expression(expr, Seq(e1, e2))
  }

  private type UnaryTransform = internal.Expression => internal.Expression
  private def unary(e: Expression, transformer: UnaryTransform)(implicit cg: ComputationGraph) = {
    // Check that the computation graph of cg and e are the same.
    e.ensureFresh(cg)
    // Specify e as reference so it can't get prematurely garbage collected.
    new Expression(transformer(e.expr), e)
  }

  def exprMinus(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.exprMinus)(cg)
  def exprPlus(e1: Expression, e2: Expression)(implicit cg: ComputationGraph): Expression = binary(e1, e2, dn.exprPlus)(cg)
  def exprPlus(e1: Expression, x: Float)(implicit cg: ComputationGraph): Expression = unary(e1, e1 => dn.exprPlus(e1, x))(cg)
  def exprPlus(x: Float, e2: Expression)(implicit cg: ComputationGraph): Expression = unary(e2, e2 => dn.exprPlus(x, e2))(cg)
  def exprMinus(e1: Expression, e2: Expression)(implicit cg: ComputationGraph): Expression = binary(e1, e2, dn.exprMinus)(cg)
  def exprMinus(e1: Expression, x: Float)(implicit cg: ComputationGraph): Expression = unary(e1, e1 => dn.exprMinus(e1, x))(cg)
  def exprMinus(x: Float, e2: Expression)(implicit cg: ComputationGraph): Expression = unary(e2, e2 => dn.exprMinus(x, e2))(cg)
  def exprTimes(e1: Expression, e2: Expression)(implicit cg: ComputationGraph): Expression = binary(e1, e2, dn.exprTimes)(cg)
  def exprTimes(e1: Expression, x: Float)(implicit cg: ComputationGraph): Expression = unary(e1, e1 => dn.exprTimes(e1, x))(cg)
  def exprTimes(x: Float, e2: Expression)(implicit cg: ComputationGraph): Expression = unary(e2, e2 => dn.exprTimes(x, e2))(cg)
  def exprDivide(e1: Expression, x: Float)(implicit cg: ComputationGraph): Expression = unary(e1, e1 => dn.exprDivide(e1, x))(cg)

  private type VectorTransform = internal.ExpressionVector => internal.Expression
  private def vectory(v: ExpressionVector, transformer: VectorTransform)(implicit cg: ComputationGraph) = {
    // DyNet segfaults if we pass a zero-length vector.
    // This check results in a nicer error message.
    assert(v.nonEmpty, "Operation requires > 0 expression arguments")
    v.ensureFresh()
    // Specify v as reference so it can't get prematurely garbage collected.
    // TODO: Use the cg somewhere
    new Expression(transformer(v.vector), v)
  }

  def affineTransform(ev: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(ev, dn.affine_transform)(cg)
  def affineTransform(exprs: Expression*)(implicit cg: ComputationGraph): Expression = affineTransform(new ExpressionVector(exprs))(cg)

  def sum(ev: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(ev, dn.sum)(cg)
  def sum(exprs: Expression*)(implicit cg: ComputationGraph): Expression = sum(new ExpressionVector(exprs)(cg))(cg)

  def sumElems(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.sum_elems)(cg)
  def momentElems(e: Expression, r: Long)(implicit cg: ComputationGraph) = unary(e, e => dn.moment_elems(e, r))(cg)
  def meanElems(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.mean_elems)(cg)
  def stdElems(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.std_elems)(cg)

  def average(ev: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(ev, dn.average)(cg)
  def average(exprs: Expression*)(implicit cg: ComputationGraph): Expression = average(new ExpressionVector(exprs))(cg)

  def sqrt(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.sqrt)(cg)
  def abs(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.abs)(cg)
  def erf(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.erf)(cg)
  def tanh(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.tanh)(cg)
  def exp(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.exp)(cg)
  def square(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.square)(cg)
  def cube(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.cube)(cg)
  def lgamma(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.lgamma)(cg)
  def log(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.log)(cg)
  def logistic(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.logistic)(cg)
  def rectify(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.rectify)(cg)
  def elu(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.elu)(cg)
  def selu(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.selu)(cg)
  def softsign(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.softsign)(cg)
  def pow(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.pow)(cg)

  def min(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.min)(cg)

  def max(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.max)(cg)
  def max(v: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(v, dn.max)(cg)
  def dotProduct(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.dot_product)(cg)
  def cmult(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.cmult)(cg)
  def cdiv(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.cdiv)(cg)
  def colwiseAdd(x: Expression, bias: Expression)(implicit cg: ComputationGraph): Expression = binary(x, bias, dn.colwise_add)(cg)

  /* PROBABILITY / LOSS OPERATIONS */

  def softmax(e: Expression, d: Long = 0L)(implicit cg: ComputationGraph): Expression = unary(e, e => dn.softmax(e, d))(cg)
  def logSoftmax(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.log_softmax)(cg)
  def logSoftmax(e: Expression, restriction: UnsignedVector)(implicit cg: ComputationGraph) =
    unary(e, e => dn.log_softmax(e, restriction.vector))(cg)

  def logSumExp(v: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(v, dn.logsumexp)(cg)

  def pickNegLogSoftmax(e: Expression, v: Long)(implicit cg: ComputationGraph): Expression = unary(e, e => dn.pickneglogsoftmax(e, v))(cg)
  def pickNegLogSoftmax(e: Expression, v: UnsignedPointer)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.pickneglogsoftmax(e, v.uintp))(cg)
  def pickNegLogSoftmax(e: Expression, v: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.pickneglogsoftmax(e, v.vector))(cg)

  def hinge(e: Expression, index: Long, m: Float = 1.0f)(implicit cg: ComputationGraph): Expression = unary(e, e => dn.hinge(e, index, m))(cg)
  def hinge(e: Expression, index: UnsignedPointer, m: Float)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge(e, index.uintp, m))(cg)
  def hinge(e: Expression, indices: UnsignedVector, m: Float)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge(e, indices.vector, m))(cg)

  def hinge(e: Expression, index: UnsignedPointer)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge(e, index.uintp, 1.0f))(cg)
  def hinge(e: Expression, indices: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge(e, indices.vector, 1.0f))(cg)

  def hingeDim(e: Expression, indices: UnsignedVector, d: Long = 0L, m: Float = 1.0f)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge_dim(e, indices.vector, d, m))(cg)
  def hingeDimBatch(e: Expression, indices: UnsignedVectorVector, d: Long = 0L, m: Float = 1.0f)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.hinge_dim(e, indices.vector, d, m))(cg)

  def sparsemax(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.sparsemax)(cg)
  def sparsemaxLoss(e: Expression, targetSupport: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(e, e => dn.sparsemax_loss(e, targetSupport.vector))(cg)

  def squaredNorm(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.squared_norm)(cg)
  def l2Norm(e: Expression)(implicit cg: ComputationGraph): Expression = unary(e, dn.l2_norm)(cg)
  def squaredDistance(e1: Expression, e2: Expression)(implicit cg: ComputationGraph): Expression = binary(e1, e2, dn.squared_distance)(cg)
  def l1Distance(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.l1_distance)(cg)
  def huberDistance(x: Expression, y: Expression, c: Float = 1.345f)(implicit cg: ComputationGraph) = {
    binary(x, y, (x, y) => dn.huber_distance(x, y, c))(cg)
  }
  def binaryLogLoss(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.binary_log_loss)(cg)
  def pairwiseRankLoss(x: Expression, y: Expression, m: Float = 1.0f)(implicit cg: ComputationGraph) =
    binary(x, y, (x, y) => dn.pairwise_rank_loss(x, y, m))(cg)
  def poissonLoss(x: Expression, y: Long)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.poisson_loss(x, y))(cg)
  def poissonLoss(x: Expression, y: UnsignedPointer)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.poisson_loss(x, y.uintp))(cg)

  /* FLOW / SHAPING OPERATIONS */

  def noBackProp(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.nobackprop)(cg)
  def flipGradient(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.flip_gradient)(cg)
  def reshape(x: Expression, d: Dim)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.reshape(x, d.dim))(cg)
  def transpose(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.transpose)(cg)
  def selectRows(x: Expression, rows: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.select_rows(x, rows.vector))(cg)
  def selectCols(x: Expression, rows: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.select_cols(x, rows.vector))(cg)
  def sumBatches(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.sum_batches)(cg)
  def momentBatches(x: Expression, r: Long)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.moment_batches(x, r))(cg)
  def stdBatches(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.std_batches)(cg)
  def momentDim(x: Expression, v: UnsignedVector, r: Long, b: Boolean = false, n: Long = 0L)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.moment_dim(x, v.vector, r, b, n))(cg)
  def meanDim(x: Expression, v: UnsignedVector, b: Boolean = false, n: Long = 0L)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.mean_dim(x, v.vector, b, n))(cg)
  def stdDim(x: Expression, v: UnsignedVector, b: Boolean = false, n: Long = 0L)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.std_dim(x, v.vector, b, n))(cg)

  def pick(x: Expression, v: Long, d: Long = 0L)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.pick(x, v, d))(cg)
  def pick(x: Expression, v: UnsignedVector, d: Long)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.pick(x, v.vector, d))(cg)
  def pick(x: Expression, v: UnsignedPointer, d: Long)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.pick(x, v.uintp, d))(cg)
  def pickrange(x: Expression, v: Long, u: Long, d: Long = 0L)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.pick_range(x, v, u, d))(cg)
  def pickBatchElem(x: Expression, v: Long)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.pick_batch_elem(x, v))(cg)
  def pickBatchElems(x: Expression, v: UnsignedVector)(implicit cg: ComputationGraph): Expression =
    unary(x, x => dn.pick_batch_elems(x, v.vector))(cg)

  def concatenateToBatch(v: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(v, dn.concatenate_to_batch)(cg)
  def concatenateToBatch(exprs: Expression*)(implicit cg: ComputationGraph): Expression = concatenateToBatch(new ExpressionVector(exprs))(cg)

  def stridedSelect(x:Expression, strides:IntVector, from:IntVector, to:IntVector)(implicit cg: ComputationGraph): Expression = {
    unary(x, x => dn.strided_select(x, strides.vector, from.vector, to.vector))(cg)
  }
  def stridedSelect(x:Expression, strides:Seq[Int], from:Seq[Int], to:Seq[Int])(implicit cg: ComputationGraph): Expression =
    stridedSelect(x, new IntVector(strides), new IntVector(from), new IntVector(to))(cg)

  def concatenateCols(v: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(v, dn.concatenate_cols)(cg)
  def concatenateCols(exprs: Expression*)(implicit cg: ComputationGraph): Expression = concatenateCols(new ExpressionVector(exprs))(cg)

  def concatenate(v: ExpressionVector)(implicit cg: ComputationGraph): Expression = vectory(v, dn.concatenate)(cg)
  def concatenate(exprs: Expression*)(implicit cg: ComputationGraph): Expression = concatenate(new ExpressionVector(exprs))(cg)

  /* NOISE OPERATIONS */

  def noise(x: Expression, stddev: Float)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.noise(x, stddev))(cg)
  def dropout(x: Expression, p: Float)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.dropout(x, p))(cg)
  def dropoutDim(x: Expression, d: Long, p: Float)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.dropout_dim(x, d, p))(cg)
  def dropoutBatch(x: Expression, p: Float)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.dropout_batch(x, p))(cg)
  def blockDropout(x: Expression, p: Float)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.block_dropout(x, p))(cg)

  /* CONVOLUTION OPERATIONS */

  // These were commented out in the C++ code.
  //def conv1dNarrow(x: Expression, f: Expression): Expression = binary(x, f, dn.conv1d_narrow)
  //def conv1dWide(x: Expression, f: Expression): Expression = binary(x, f, dn.conv1d_wide)
  def filter1DNarrow(x: Expression, f: Expression)(implicit cg: ComputationGraph): Expression = binary(x, f, dn.filter1d_narrow)(cg)
  def kMaxPooling(x: Expression, k: Long)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.kmax_pooling(x, k))(cg)
  def foldRows(x: Expression, nRows: Long = 2L)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.fold_rows(x, nRows))(cg)
  def sumDim(x: Expression, dims: UnsignedVector, b: Boolean = false)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.sum_dim(x, dims.vector, b))(cg)
  def sumCols(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.sum_cols)(cg)
  def sumRows(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.sum_rows)(cg)
  def averageCols(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.average_cols)(cg)
  def kmhNgram(x: Expression, n: Long)(implicit cg: ComputationGraph): Expression = unary(x, x => dn.kmh_ngram(x, n))(cg)

  // In the C++ code, is_valid has a default value of true. Scala won't let you have two overloaded
  // methods with default values, so I just got rid of the default value here.
  // TODO(joelgrus): write tests for these
  def conv2d(x: Expression, f: Expression, stride: UnsignedVector, isValid: Boolean)(implicit cg: ComputationGraph) =
    new Expression(dn.conv2d(x.expr, f.expr, stride.vector, isValid), Seq(x, f, stride))(cg)
  def conv2d(x: Expression, f: Expression, stride: UnsignedVector)(implicit cg: ComputationGraph) =
    new Expression(dn.conv2d(x.expr, f.expr, stride.vector, true), Seq(x, f, stride))(cg)

  def conv2d(x: Expression, f: Expression, b: Expression, stride: UnsignedVector, isValid: Boolean)(implicit cg: ComputationGraph) =
    new Expression(dn.conv2d(x.expr, f.expr, b.expr, stride.vector, isValid), Seq(x, f, b, stride))(cg)
  def conv2d(x: Expression, f: Expression, b: Expression, stride: UnsignedVector)(implicit cg: ComputationGraph) =
    new Expression(dn.conv2d(x.expr, f.expr, b.expr, stride.vector, true), Seq(x, f, b, stride))(cg)

  /* TENSOR OPERATIONS */

  def contract3d1d(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.contract3d_1d)(cg)
  def contract3d1d1d(x: Expression, y: Expression, z: Expression)(implicit cg: ComputationGraph): Expression = {
    Seq(x, y, z).foreach(_.ensureFresh())
    new Expression(dn.contract3d_1d_1d(x.expr, y.expr, z.expr), Seq(x, y, z))(cg)
  }
  def contract3d1d1d(x: Expression, y: Expression, z: Expression, b: Expression)(implicit cg: ComputationGraph): Expression = {
    Seq(x, y, z, b).foreach(_.ensureFresh())
    new Expression(dn.contract3d_1d_1d(x.expr, y.expr, z.expr, b.expr), Seq(x, y, z, b))(cg)
  }
  def contract3d1d(x: Expression, y: Expression, b: Expression)(implicit cg: ComputationGraph): Expression = {
    Seq(x, y, b).foreach(_.ensureFresh())
    new Expression(dn.contract3d_1d(x.expr, y.expr, b.expr), Seq(x, y, b))(cg)
  }

  /* LINEAR ALGEBRA OPERATIONS */

  def inverse(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.inverse)(cg)
  def logdet(x: Expression)(implicit cg: ComputationGraph): Expression = unary(x, dn.logdet)(cg)
  def traceOfProduct(x: Expression, y: Expression)(implicit cg: ComputationGraph): Expression = binary(x, y, dn.trace_of_product)(cg)

  /* NORMALIZATION OPERATIONS */

  def layerNorm(x: Expression, g: Expression, b: Expression)(implicit cg: ComputationGraph): Expression = {
    Seq(x, g, b).foreach(_.ensureFresh())
    new Expression(dn.layer_norm(x.expr, g.expr, b.expr), Seq(x, g, b))(cg)
  }
  def weightNorm(w: Expression, g: Expression)(implicit cg: ComputationGraph): Expression = binary(w, g, dn.weight_norm)(cg)

  /** Augment numbers so that they can do arithmetic with expressions. */
  implicit class ImplicitNumerics[T](x: T)(implicit n: Numeric[T]) {
    import n._
    def +(e: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprPlus(x.toFloat, e)(cg)
    def *(e: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprTimes(x.toFloat, e)(cg)
    def -(e: Expression)(implicit cg: ComputationGraph): Expression = Expression.exprMinus(x.toFloat, e)(cg)
  }
}
