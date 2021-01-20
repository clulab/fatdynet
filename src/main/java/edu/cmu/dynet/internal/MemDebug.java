/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class MemDebug {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected MemDebug(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MemDebug obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_MemDebug(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public MemDebug(boolean atExit) {
    this(dynet_swigJNI.new_MemDebug__SWIG_0(atExit), true);
  }

  public MemDebug() {
    this(dynet_swigJNI.new_MemDebug__SWIG_1(), true);
  }

  public void debug() {
    dynet_swigJNI.MemDebug_debug(swigCPtr, this);
  }

  public void leak_malloc() {
    dynet_swigJNI.MemDebug_leak_malloc(swigCPtr, this);
  }

  public void leak_new() {
    dynet_swigJNI.MemDebug_leak_new(swigCPtr, this);
  }

  public void leak_mm_malloc() {
    dynet_swigJNI.MemDebug_leak_mm_malloc(swigCPtr, this);
  }

  public void set_break(int index) {
    dynet_swigJNI.MemDebug_set_break(swigCPtr, this, index);
  }

}
