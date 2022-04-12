package edu.cmu.dynet

abstract class RnnBuilder(private[dynet] val _builder: internal.RNNBuilder) {

  var version: Long = ComputationGraph.version
  var cgOpt: Option[ComputationGraph] = None

  def close(): Unit = _builder.delete()

  def state(): Int = _builder.state
  def newGraph(update:Boolean = true)(implicit cg: ComputationGraph): Unit = {
    version = ComputationGraph.version // TODO: Update this as well
    cgOpt = Some(cg)
    _builder.new_graph(cg.cg, update)
  }

  def startNewSequence(ev: ExpressionVector): Unit = _builder.start_new_sequence(ev.vector)
  def startNewSequence(): Unit = _builder.start_new_sequence()

  def setH(prev: Int, hNew: ExpressionVector) = _builder.set_h(prev, hNew.vector)
  // and others
  def setS(prev: Int, sNew: ExpressionVector) = _builder.set_s(prev, sNew.vector)
  // and others

  def addInput(x: Expression): Expression = {
    val expr = _builder.add_input(x.expr)
    new Expression(expr)(cgOpt.get)
  }

  def addInput(prev: Int, x: Expression): Expression = {
    val expr = _builder.add_input(prev, x.expr)
    new Expression(expr)(cgOpt.get)
  }

  def rewindOneStep(): Unit = _builder.rewind_one_step()
  def getHead(p: Int): Int = _builder.get_head(p)
  def setDropout(d: Float): Unit = _builder.set_dropout(d)
  def disableDropout(): Unit = _builder.disable_dropout()

  def back(): Expression = new Expression(_builder.back)(cgOpt.get)
  def finalH(): ExpressionVector = new ExpressionVector(_builder.final_h())(cgOpt.get)
  def getH(i: Int): ExpressionVector = new ExpressionVector(_builder.get_h(i))(cgOpt.get)

  def finalS(): ExpressionVector = new ExpressionVector(_builder.final_s)(cgOpt.get)
  def getS(i: Int): ExpressionVector = new ExpressionVector(_builder.get_s(i))(cgOpt.get)

  def numH0Components(): Long = _builder.num_h0_components()
  def copy(params: RnnBuilder): Unit = _builder.copy(params._builder)
  // save and load
}

class SimpleRnnBuilder private[dynet](private[dynet] val builder: internal.SimpleRNNBuilder)
    extends RnnBuilder(builder) {
  def this() = { this(new internal.SimpleRNNBuilder()) }

  def this(layers: Long, inputDim: Long, hiddenDim: Long, model: ParameterCollection, supportLags: Boolean = false) = {
    this(new internal.SimpleRNNBuilder(layers, inputDim, hiddenDim, model.model, supportLags))
  }

  def addAuxiliaryInput(x: Expression, aux: Expression): Expression = {
    x.ensureFresh(cgOpt.get)
    aux.ensureFresh(cgOpt.get)
    new Expression(builder.add_auxiliary_input(x.expr, aux.expr))(cgOpt.get)
  }
}
