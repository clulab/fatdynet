/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class UnsignedVector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected UnsignedVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(UnsignedVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_UnsignedVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public UnsignedVector(java.util.Collection<Integer> values) {
     this(values.size());
     int i = 0;
     for (java.util.Iterator<Integer> it = values.iterator(); it.hasNext(); i++) {
         Integer value = it.next();
         this.set(i, value);
     }
  }

  public UnsignedVector() {
    this(dynet_swigJNI.new_UnsignedVector__SWIG_0(), true);
  }

  public UnsignedVector(long n) {
    this(dynet_swigJNI.new_UnsignedVector__SWIG_1(n), true);
  }

  public long size() {
    return dynet_swigJNI.UnsignedVector_size(swigCPtr, this);
  }

  public long capacity() {
    return dynet_swigJNI.UnsignedVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    dynet_swigJNI.UnsignedVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return dynet_swigJNI.UnsignedVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    dynet_swigJNI.UnsignedVector_clear(swigCPtr, this);
  }

  public void add(long x) {
    dynet_swigJNI.UnsignedVector_add(swigCPtr, this, x);
  }

  public long get(int i) {
    return dynet_swigJNI.UnsignedVector_get(swigCPtr, this, i);
  }

  public void set(int i, long val) {
    dynet_swigJNI.UnsignedVector_set(swigCPtr, this, i, val);
  }

}
