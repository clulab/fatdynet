/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ExpressionVectorVector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ExpressionVectorVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ExpressionVectorVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ExpressionVectorVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public ExpressionVectorVector(java.util.Collection<ExpressionVector> values) {
     this(values.size());
     int i = 0;
     for (java.util.Iterator<ExpressionVector> it = values.iterator(); it.hasNext(); i++) {
         ExpressionVector value = it.next();
         this.set(i, value);
     }
  }

  public ExpressionVectorVector() {
    this(dynet_swigJNI.new_ExpressionVectorVector__SWIG_0(), true);
  }

  public ExpressionVectorVector(long n) {
    this(dynet_swigJNI.new_ExpressionVectorVector__SWIG_1(n), true);
  }

  public long size() {
    return dynet_swigJNI.ExpressionVectorVector_size(swigCPtr, this);
  }

  public long capacity() {
    return dynet_swigJNI.ExpressionVectorVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    dynet_swigJNI.ExpressionVectorVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return dynet_swigJNI.ExpressionVectorVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    dynet_swigJNI.ExpressionVectorVector_clear(swigCPtr, this);
  }

  public void add(ExpressionVector x) {
    dynet_swigJNI.ExpressionVectorVector_add(swigCPtr, this, ExpressionVector.getCPtr(x), x);
  }

  public ExpressionVector get(int i) {
    return new ExpressionVector(dynet_swigJNI.ExpressionVectorVector_get(swigCPtr, this, i), false);
  }

  public void set(int i, ExpressionVector val) {
    dynet_swigJNI.ExpressionVectorVector_set(swigCPtr, this, i, ExpressionVector.getCPtr(val), val);
  }

}
