/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class Parameter {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Parameter(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Parameter obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_Parameter(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Parameter() {
    this(dynet_swigJNI.new_Parameter(), true);
  }

  public void zero() {
    dynet_swigJNI.Parameter_zero(swigCPtr, this);
  }

  public Dim dim() {
    return new Dim(dynet_swigJNI.Parameter_dim(swigCPtr, this), true);
  }

  public Tensor values() {
    long cPtr = dynet_swigJNI.Parameter_values(swigCPtr, this);
    return (cPtr == 0) ? null : new Tensor(cPtr, false);
  }

  public void set_updated(boolean b) {
    dynet_swigJNI.Parameter_set_updated(swigCPtr, this, b);
  }

  public boolean is_updated() {
    return dynet_swigJNI.Parameter_is_updated(swigCPtr, this);
  }

}
