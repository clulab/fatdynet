/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class SignalHandler {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SignalHandler(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SignalHandler obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_SignalHandler(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    dynet_swigJNI.SignalHandler_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    dynet_swigJNI.SignalHandler_change_ownership(this, swigCPtr, true);
  }

  public SignalHandler() {
    this(dynet_swigJNI.new_SignalHandler(), true);
    dynet_swigJNI.SignalHandler_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public int run(int signal) {
    return (getClass() == SignalHandler.class) ? dynet_swigJNI.SignalHandler_run(swigCPtr, this, signal) : dynet_swigJNI.SignalHandler_runSwigExplicitSignalHandler(swigCPtr, this, signal);
  }

}