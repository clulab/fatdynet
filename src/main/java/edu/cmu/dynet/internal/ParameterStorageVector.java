/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ParameterStorageVector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ParameterStorageVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ParameterStorageVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ParameterStorageVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public ParameterStorageVector() {
    this(dynet_swigJNI.new_ParameterStorageVector__SWIG_0(), true);
  }

  public ParameterStorageVector(long n) {
    this(dynet_swigJNI.new_ParameterStorageVector__SWIG_1(n), true);
  }

  public long size() {
    return dynet_swigJNI.ParameterStorageVector_size(swigCPtr, this);
  }

  public long capacity() {
    return dynet_swigJNI.ParameterStorageVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    dynet_swigJNI.ParameterStorageVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return dynet_swigJNI.ParameterStorageVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    dynet_swigJNI.ParameterStorageVector_clear(swigCPtr, this);
  }

  public void add(ParameterStorage x) {
    dynet_swigJNI.ParameterStorageVector_add(swigCPtr, this, ParameterStorage.getCPtr(x), x);
  }

  public ParameterStorage get(int i) {
    long cPtr = dynet_swigJNI.ParameterStorageVector_get(swigCPtr, this, i);
    return (cPtr == 0) ? null : new ParameterStorage(cPtr, true);
  }

  public void set(int i, ParameterStorage val) {
    dynet_swigJNI.ParameterStorageVector_set(swigCPtr, this, i, ParameterStorage.getCPtr(val), val);
  }

}
