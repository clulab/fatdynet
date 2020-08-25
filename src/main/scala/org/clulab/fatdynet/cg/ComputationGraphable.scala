package org.clulab.fatdynet.cg

import edu.cmu.dynet.Dim
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.LookupParameter
import edu.cmu.dynet.Parameter
import edu.cmu.dynet.Tensor
import edu.cmu.dynet.UnsignedPointer
import edu.cmu.dynet.UnsignedVector
import edu.cmu.dynet.VariableIndex
import org.clulab.fatdynet.expr.FatExpression
import org.clulab.fatdynet.expr.ExpressionFactory

// Except for the CLU Lab additions, the ScalaComputationGraph, edu.cmu.dynet.ComputationGraph,
// should be a ComputationGraphable.
trait ComputationGraphable {
  // These are CLU Lab additions.
  def close(): Unit
  def getExpressionFactory(): ExpressionFactory[FatExpression]

  def addInput(s: Float): VariableIndex
  def addInput(d: Dim, data: FloatVector): VariableIndex
  def addInput(d: Dim, ids: UnsignedVector, data: FloatVector, defdata: Float = 0.0f): VariableIndex

  def addParameters(p: Parameter): VariableIndex
  def addConstParameters(p: Parameter): VariableIndex

  def addLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex
  def addLookup(p: LookupParameter, index: Long): VariableIndex
  def addLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex

  def addConstLookup(p: LookupParameter, pindex: UnsignedPointer): VariableIndex
  def addConstLookup(p: LookupParameter, index: Long): VariableIndex
  def addConstLookup(p: LookupParameter, indices: UnsignedVector): VariableIndex

  def getDimension(index: VariableIndex): Dim

  def clear(): Unit
  def checkpoint(): Unit
  def revert(): Unit

  // Be careful when they return expressions!
  def forward(last: FatExpression): Tensor
  def incrementalForward(last: FatExpression): Tensor
  def getValue(e: FatExpression): Tensor

  def invalidate(): Unit
  def backward(last: FatExpression): Unit

  def printGraphViz(): Unit
}
