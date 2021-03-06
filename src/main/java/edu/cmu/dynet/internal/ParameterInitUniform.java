/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ParameterInitUniform extends ParameterInit {
  private transient long swigCPtr;

  protected ParameterInitUniform(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.ParameterInitUniform_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ParameterInitUniform obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ParameterInitUniform(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ParameterInitUniform(float scale) {
    this(dynet_swigJNI.new_ParameterInitUniform__SWIG_0(scale), true);
  }

  public ParameterInitUniform(float l, float r) {
    this(dynet_swigJNI.new_ParameterInitUniform__SWIG_1(l, r), true);
  }

  public void initialize_params(Tensor values) {
    dynet_swigJNI.ParameterInitUniform_initialize_params(swigCPtr, this, Tensor.getCPtr(values), values);
  }

}
