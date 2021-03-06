/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class CyclicalSGDTrainer extends Trainer {
  private transient long swigCPtr;

  protected CyclicalSGDTrainer(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.CyclicalSGDTrainer_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CyclicalSGDTrainer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_CyclicalSGDTrainer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public CyclicalSGDTrainer(ParameterCollection m, float learning_rate_min, float learning_rate_max, float step_size, float gamma, float edecay) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_0(ParameterCollection.getCPtr(m), m, learning_rate_min, learning_rate_max, step_size, gamma, edecay), true);
  }

  public CyclicalSGDTrainer(ParameterCollection m, float learning_rate_min, float learning_rate_max, float step_size, float gamma) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_1(ParameterCollection.getCPtr(m), m, learning_rate_min, learning_rate_max, step_size, gamma), true);
  }

  public CyclicalSGDTrainer(ParameterCollection m, float learning_rate_min, float learning_rate_max, float step_size) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_2(ParameterCollection.getCPtr(m), m, learning_rate_min, learning_rate_max, step_size), true);
  }

  public CyclicalSGDTrainer(ParameterCollection m, float learning_rate_min, float learning_rate_max) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_3(ParameterCollection.getCPtr(m), m, learning_rate_min, learning_rate_max), true);
  }

  public CyclicalSGDTrainer(ParameterCollection m, float learning_rate_min) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_4(ParameterCollection.getCPtr(m), m, learning_rate_min), true);
  }

  public CyclicalSGDTrainer(ParameterCollection m) {
    this(dynet_swigJNI.new_CyclicalSGDTrainer__SWIG_5(ParameterCollection.getCPtr(m), m), true);
  }

  public void restart() {
    dynet_swigJNI.CyclicalSGDTrainer_restart(swigCPtr, this);
  }

  public void update() {
    dynet_swigJNI.CyclicalSGDTrainer_update(swigCPtr, this);
  }

}
