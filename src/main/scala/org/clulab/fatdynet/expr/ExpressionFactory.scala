package org.clulab.fatdynet.expr

import edu.cmu.dynet.Dim
import edu.cmu.dynet.FloatPointer
import edu.cmu.dynet.FloatVector
import edu.cmu.dynet.Parameter

trait ExpressionFactory[T] {
  def input(s: Float): T
  def input(fp: FloatPointer): T
  def input(d: Dim, pdata: FloatVector): T

  def parameter(p: Parameter): T

  def exprMinus(e: T): T
  def exprPlus(e1: T, e2: T): T
  def exprPlus(e1: T, x: Float): T
  def exprPlus(x: Float, e2: T): T
  def exprMinus(e1: T, e2: T): T
  def exprMinus(e1: T, x: Float): T
  def exprMinus(x: Float, e2: T): T
  def exprTimes(e1: T, e2: T): T
  def exprTimes(e1: T, x: Float): T
  def exprTimes(x: Float, e2: T): T
  def exprDivide(e1: T, x: Float): T

  def tanh(e: T): T
  def squaredDistance(e1: T, e2: T): T
}
