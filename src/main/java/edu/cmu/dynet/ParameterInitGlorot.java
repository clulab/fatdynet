/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ParameterInitGlorot extends ParameterInit {
  private transient long swigCPtr;

  protected ParameterInitGlorot(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.ParameterInitGlorot_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ParameterInitGlorot obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ParameterInitGlorot(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ParameterInitGlorot(boolean is_lookup) {
    this(dynet_swigJNI.new_ParameterInitGlorot__SWIG_0(is_lookup), true);
  }

  public ParameterInitGlorot() {
    this(dynet_swigJNI.new_ParameterInitGlorot__SWIG_1(), true);
  }

  public void initialize_params(Tensor values) {
    dynet_swigJNI.ParameterInitGlorot_initialize_params(swigCPtr, this, Tensor.getCPtr(values), values);
  }

}
