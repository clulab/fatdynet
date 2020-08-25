package org.clulab.fatdynet.cg

import edu.cmu.dynet.Dim
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.Tensor
import edu.cmu.dynet.UnsignedPointer
import edu.cmu.dynet.UnsignedVector
import edu.cmu.dynet.VariableIndex
import edu.cmu.dynet.internal
import edu.cmu.dynet.internal.{ComputationGraph => JavaComputationGraph}
import edu.cmu.dynet.internal.dynet_swigJNI
import org.clulab.fatdynet.expr.FatExpression
import org.clulab.fatdynet.expr.ExpressionFactory
import org.clulab.fatdynet.expr.DynamicExpressionFactory

class DynamicComputationGraph protected (cPtrAndCMemoryOwn: (Long, Boolean))
    extends JavaComputationGraph(cPtrAndCMemoryOwn._1, cPtrAndCMemoryOwn._2) with ComputationGraphable {

  // The main reason for this class is to provide an accessible default constructor.
  // A secondary reason is to implement all of ComputationGraph.
  def this() = this(DynamicComputationGraph.getCPtrAndCMemoryOwn)

  var defaultDevice: internal.Device = internal.dynet_swig.getDefault_device

  // These are CLU Lab additions.
  def close(): Unit = delete()

  def getExpressionFactory(): ExpressionFactory[FatExpression] = new DynamicExpressionFactory(this)

  def addInput(s: Float): VariableIndex = new VariableIndex(this.add_input(s, defaultDevice))
  def addInput(d: Dim, data: FloatVector): VariableIndex =
      new VariableIndex(this.add_input(d.dim, data.vector, defaultDevice))
  def addInput(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0.0f): VariableIndex =
      new VariableIndex(this.add_input(d.dim, ids.vector, data.vector, defaultDevice, defdata))

  def addParameters(p: Parameter): VariableIndex = new VariableIndex(this.add_parameters(p.parameter))
  def addConstParameters(p: Parameter): VariableIndex =
      new VariableIndex(this.add_const_parameters(p.parameter))

  def addLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
      new VariableIndex(this.add_lookup(p.lookupParameter, pindex.uintp))
  def addLookup(p: LookupParameter, index: Long): VariableIndex =
      new VariableIndex(this.add_lookup(p.lookupParameter, index))
  def addLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
      new VariableIndex(this.add_lookup(p.lookupParameter, indices.vector))

  def addConstLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex =
      new VariableIndex(this.add_const_lookup(p.lookupParameter, pindex.uintp))
  def addConstLookup(p: LookupParameter, index: Long): VariableIndex =
      new VariableIndex(this.add_const_lookup(p.lookupParameter, index))
  def addConstLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex =
      new VariableIndex(this.add_const_lookup(p.lookupParameter, indices.vector))

  def getDimension(index: VariableIndex): Dim = new Dim(this.get_dimension(index.index))

  def forward(last: FatExpression): Tensor = new Tensor(this.forward(last.javaExpression))
  def incrementalForward(last: FatExpression): Tensor = new Tensor(this.incremental_forward(last.javaExpression))
  def getValue(e: FatExpression): Tensor =  new Tensor(this.get_value(e.javaExpression))

  def backward(last: FatExpression): Unit = this.backward(last.javaExpression)

  def printGraphViz(): Unit = this.print_graphviz()
}

object DynamicComputationGraph {

  def getCPtrAndCMemoryOwn: (Long, Boolean) = {
    val cPtr: Long = dynet_swigJNI.new_ComputationGraph
    val cMemoryOwn: Boolean = true

    (cPtr, cMemoryOwn)
  }
}
