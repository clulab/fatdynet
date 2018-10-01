/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class LongVector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected LongVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(LongVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_LongVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public LongVector() {
    this(dynet_swigJNI.new_LongVector__SWIG_0(), true);
  }

  public LongVector(long n) {
    this(dynet_swigJNI.new_LongVector__SWIG_1(n), true);
  }

  public long size() {
    return dynet_swigJNI.LongVector_size(swigCPtr, this);
  }

  public long capacity() {
    return dynet_swigJNI.LongVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    dynet_swigJNI.LongVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return dynet_swigJNI.LongVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    dynet_swigJNI.LongVector_clear(swigCPtr, this);
  }

  public void add(int x) {
    dynet_swigJNI.LongVector_add(swigCPtr, this, x);
  }

  public int get(int i) {
    return dynet_swigJNI.LongVector_get(swigCPtr, this, i);
  }

  public void set(int i, int val) {
    dynet_swigJNI.LongVector_set(swigCPtr, this, i, val);
  }

}