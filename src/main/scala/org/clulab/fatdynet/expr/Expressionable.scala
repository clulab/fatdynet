package org.clulab.fatdynet.expr

import edu.cmu.dynet.Dim
import edu.cmu.dynet.Tensor

// A ScalaExpression, edu.cmu.dynet.Expression, should be an Expressionable[ScalaExpression].
trait Expressionable[T] {
  def value(): Tensor
  def dim(): Dim

  def +(e2: T): T
  def *(e2: T): T
  def -(e2: T): T
  def +(r: Float): T
  def *(r: Float): T
  def -(r: Float): T
  def /(r: Float): T
  def unary_-(): T

  def debugString(): String
}
