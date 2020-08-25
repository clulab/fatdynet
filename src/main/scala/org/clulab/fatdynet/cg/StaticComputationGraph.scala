package org.clulab.fatdynet.cg

import edu.cmu.dynet.Dim
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.Tensor
import edu.cmu.dynet.UnsignedPointer
import edu.cmu.dynet.UnsignedVector
import edu.cmu.dynet.VariableIndex
import edu.cmu.dynet.{ComputationGraph => CmuComputationGraph}
import org.clulab.fatdynet.expr.FatExpression
import org.clulab.fatdynet.expr.ExpressionFactory
import org.clulab.fatdynet.expr.StaticExpressionFactory

class StaticComputationGraph extends ComputationGraphable {
  // Renew it in the constructor.
  CmuComputationGraph.renew()

  // These are CLU Lab additions.
  def close(): Unit = { }

  def getExpressionFactory(): ExpressionFactory[FatExpression] = new StaticExpressionFactory()

  def addInput(s: Float): VariableIndex = CmuComputationGraph.addInput(s)
  def addInput(d: Dim, data: FloatVector): VariableIndex = CmuComputationGraph.addInput(d, data)
  def addInput(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0.0f): VariableIndex = CmuComputationGraph.addInput(d, ids, data)

  def addParameters(p: Parameter): VariableIndex = CmuComputationGraph.addParameters(p)
  def addConstParameters(p: Parameter): VariableIndex = CmuComputationGraph.addConstParameters(p)

  def addLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex = CmuComputationGraph.addLookup(p, pindex)
  def addLookup(p: LookupParameter, index: Long): VariableIndex = CmuComputationGraph.addLookup(p, index)
  def addLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex = CmuComputationGraph.addLookup(p, indices)

  def addConstLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex = CmuComputationGraph.addConstLookup(p, pindex)
  def addConstLookup(p: LookupParameter, index: Long): VariableIndex = CmuComputationGraph.addConstLookup(p, index)
  def addConstLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex = CmuComputationGraph.addConstLookup(p, indices)

  def getDimension(index: VariableIndex): Dim = CmuComputationGraph.getDimension(index)

  def clear(): Unit = CmuComputationGraph.clear()
  def checkpoint(): Unit = CmuComputationGraph.checkpoint()
  def revert(): Unit = CmuComputationGraph.revert()

  // Be careful when they return expressions!
  def forward(last: FatExpression): Tensor = CmuComputationGraph.forward(last.scalaExpression)
  def incrementalForward(last: FatExpression): Tensor = CmuComputationGraph.incrementalForward(last.scalaExpression)
  def getValue(e: FatExpression): Tensor = CmuComputationGraph.getValue(e.scalaExpression)

  def invalidate(): Unit = CmuComputationGraph.invalidate()
  def backward(last: FatExpression): Unit = CmuComputationGraph.backward(last.scalaExpression)

  def printGraphViz(): Unit = CmuComputationGraph.printGraphViz()
}
