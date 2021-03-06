/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ParameterInitFromFile extends ParameterInit {
  private transient long swigCPtr;

  protected ParameterInitFromFile(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.ParameterInitFromFile_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ParameterInitFromFile obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ParameterInitFromFile(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ParameterInitFromFile(String f) {
    this(dynet_swigJNI.new_ParameterInitFromFile(f), true);
  }

  public void initialize_params(Tensor values) {
    dynet_swigJNI.ParameterInitFromFile_initialize_params(swigCPtr, this, Tensor.getCPtr(values), values);
  }

}
