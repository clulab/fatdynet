/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class DoubleVector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected DoubleVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(DoubleVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_DoubleVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public DoubleVector(java.util.Collection<Double> values) {
     this(values.size());
     int i = 0;
     for (java.util.Iterator<Double> it = values.iterator(); it.hasNext(); i++) {
         Double value = it.next();
         this.set(i, value);
     }
  }

  public DoubleVector() {
    this(dynet_swigJNI.new_DoubleVector__SWIG_0(), true);
  }

  public DoubleVector(long n) {
    this(dynet_swigJNI.new_DoubleVector__SWIG_1(n), true);
  }

  public long size() {
    return dynet_swigJNI.DoubleVector_size(swigCPtr, this);
  }

  public long capacity() {
    return dynet_swigJNI.DoubleVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    dynet_swigJNI.DoubleVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return dynet_swigJNI.DoubleVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    dynet_swigJNI.DoubleVector_clear(swigCPtr, this);
  }

  public void add(double x) {
    dynet_swigJNI.DoubleVector_add(swigCPtr, this, x);
  }

  public double get(int i) {
    return dynet_swigJNI.DoubleVector_get(swigCPtr, this, i);
  }

  public void set(int i, double val) {
    dynet_swigJNI.DoubleVector_set(swigCPtr, this, i, val);
  }

}
