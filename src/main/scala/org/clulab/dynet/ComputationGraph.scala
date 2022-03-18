package org.clulab.dynet

import edu.cmu.dynet.internal

class ComputationGraph private[dynet](private[dynet] val cg: internal.ComputationGraph) {

  def addInput(s: Float): VariableIndex = new VariableIndex(cg.add_input(s, ComputationGraph.defaultDevice))
  def addInput(d: Dim, data: FloatVector): VariableIndex =
    new VariableIndex(cg.add_input(d.dim, data.vector, ComputationGraph.defaultDevice))
  def addInput(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0.0f) =
    new VariableIndex(cg.add_input(d.dim, ids.vector, data.vector, ComputationGraph.defaultDevice, defdata))

  def addParameters(p: Parameter): VariableIndex = new VariableIndex(cg.add_parameters(p.parameter))
  def addConstParameters(p: Parameter): VariableIndex =
    new VariableIndex(cg.add_const_parameters(p.parameter))

  def addLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
    new VariableIndex(cg.add_lookup(p.lookupParameter, pindex.uintp))
  def addLookup(p: LookupParameter, index: Long): VariableIndex =
    new VariableIndex(cg.add_lookup(p.lookupParameter, index))
  def addLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
    new VariableIndex(cg.add_lookup(p.lookupParameter, indices.vector))

  def addConstLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
    new VariableIndex(cg.add_const_lookup(p.lookupParameter, pindex.uintp))
  def addConstLookup(p: LookupParameter, index: Long): VariableIndex =
    new VariableIndex(cg.add_const_lookup(p.lookupParameter, index))
  def addConstLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
    new VariableIndex(cg.add_const_lookup(p.lookupParameter, indices.vector))

  def getDimension(index: VariableIndex): Dim = new Dim(cg.get_dimension(index.index))

  def clear(): Unit = cg.clear()
  def checkpoint(): Unit = cg.checkpoint()
  def revert(): Unit = cg.revert()

  def reset(): Unit = cg.reset(true)

  def close(): Unit = reset()

  def forward(last: Expression): Tensor = new Tensor(cg.forward(last.expr))
  def incrementalForward(last: Expression): Tensor = new Tensor(cg.incremental_forward(last.expr))
  def getValue(e: Expression): Tensor = new Tensor(cg.get_value(e.expr))

  def invalidate(): Unit = cg.invalidate()
  def backward(last: Expression): Unit = cg.backward(last.expr)

  def printGraphViz(): Unit = cg.print_graphviz()
}

/** The ComputationGraph object contains the singleton DyNet computation graph instance. Any C++
  * instance method is instead implemented as a static function here.*
  */
object ComputationGraph {
  private[dynet] var cgOpt: Option[ComputationGraph] = Some(new ComputationGraph(internal.ComputationGraph.getNew))
  var version: Long = 0L
  // We can't know the lifetime of the defaultDevice here, so it can't be deleted.
  private val defaultDevice: internal.Device = internal.dynet_swig.getDefault_device

  /** Gets rid of the singleton Computation Graph and replaces it with a fresh one. Increments
    * `version` to make sure we don't use any stale expressions.
    */
  def renew(ignoreStatic: Boolean = false): ComputationGraph = {
    if (ignoreStatic)
      new ComputationGraph(internal.ComputationGraph.getNew(true))
    else {
      cgOpt = {
        cgOpt.foreach(_.reset()) // We had better be done with it!
        Some(new ComputationGraph(internal.ComputationGraph.getNew))
      }
      version += 1
      cgOpt.get // This should not be used!
    }
  }

  def addInput(s: Float): VariableIndex = cgOpt.get.addInput(s)
  def addInput(d: Dim, data: FloatVector): VariableIndex = cgOpt.get.addInput(d, data)
  def addInput(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0.0f): VariableIndex =
    cgOpt.get.addInput(d, ids, data, defdata)

  def addParameters(p: Parameter): VariableIndex = cgOpt.get.addParameters(p)
  def addConstParameters(p: Parameter): VariableIndex = cgOpt.get.addConstParameters(p)

  def addLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
    cgOpt.get.addLookup(p, pindex)
  def addLookup(p: LookupParameter, index: Long): VariableIndex =
    cgOpt.get.addLookup(p, index)
  def addLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
    cgOpt.get.addLookup(p, indices)

  def addConstLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
    cgOpt.get.addConstLookup(p, pindex)
  def addConstLookup(p: LookupParameter, index: Long): VariableIndex =
    cgOpt.get.addConstLookup(p, index)
  def addConstLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
    cgOpt.get.addConstLookup(p, indices)

  def getDimension(index: VariableIndex): Dim = cgOpt.get.getDimension(index)

  def clear(): Unit = cgOpt.get.clear()
  def checkpoint(): Unit = cgOpt.get.checkpoint()
  def revert(): Unit = cgOpt.get.revert()

  def reset(): Unit = {
    cgOpt.get.reset()
    cgOpt = None
  }

  def close(): Unit = reset()

  def forward(last: Expression): Tensor = cgOpt.get.forward(last)
  def incrementalForward(last: Expression): Tensor = cgOpt.get.incrementalForward(last)
  def getValue(e: Expression): Tensor = cgOpt.get.getValue(e)

  def invalidate(): Unit = cgOpt.get.invalidate()
  def backward(last: Expression): Unit = cgOpt.get.backward(last)

  def printGraphViz(): Unit = cgOpt.get.printGraphViz()
}
