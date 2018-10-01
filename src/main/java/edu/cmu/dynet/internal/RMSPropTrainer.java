/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class RMSPropTrainer extends Trainer {
  private transient long swigCPtr;

  protected RMSPropTrainer(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.RMSPropTrainer_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(RMSPropTrainer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_RMSPropTrainer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public RMSPropTrainer(ParameterCollection m, float learning_rate, float eps, float rho) {
    this(dynet_swigJNI.new_RMSPropTrainer__SWIG_0(ParameterCollection.getCPtr(m), m, learning_rate, eps, rho), true);
  }

  public RMSPropTrainer(ParameterCollection m, float learning_rate, float eps) {
    this(dynet_swigJNI.new_RMSPropTrainer__SWIG_1(ParameterCollection.getCPtr(m), m, learning_rate, eps), true);
  }

  public RMSPropTrainer(ParameterCollection m, float learning_rate) {
    this(dynet_swigJNI.new_RMSPropTrainer__SWIG_2(ParameterCollection.getCPtr(m), m, learning_rate), true);
  }

  public RMSPropTrainer(ParameterCollection m) {
    this(dynet_swigJNI.new_RMSPropTrainer__SWIG_3(ParameterCollection.getCPtr(m), m), true);
  }

  public void restart() {
    dynet_swigJNI.RMSPropTrainer_restart(swigCPtr, this);
  }

}