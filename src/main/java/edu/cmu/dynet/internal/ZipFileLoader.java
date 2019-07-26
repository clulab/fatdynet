/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ZipFileLoader extends BaseFileLoader {
  private transient long swigCPtr;

  protected ZipFileLoader(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.ZipFileLoader_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ZipFileLoader obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ZipFileLoader(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public ZipFileLoader(String filename, String zipname) {
    this(dynet_swigJNI.new_ZipFileLoader(filename, zipname), true);
  }

  public void populate(ParameterCollection model, String key) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_0(swigCPtr, this, ParameterCollection.getCPtr(model), model, key);
  }

  public void populate(ParameterCollection model) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_1(swigCPtr, this, ParameterCollection.getCPtr(model), model);
  }

  public void populate(Parameter param, String key) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_2(swigCPtr, this, Parameter.getCPtr(param), param, key);
  }

  public void populate(Parameter param) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_3(swigCPtr, this, Parameter.getCPtr(param), param);
  }

  public void populate(LookupParameter lookup_param, String key) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_4(swigCPtr, this, LookupParameter.getCPtr(lookup_param), lookup_param, key);
  }

  public void populate(LookupParameter lookup_param) {
    dynet_swigJNI.ZipFileLoader_populate__SWIG_5(swigCPtr, this, LookupParameter.getCPtr(lookup_param), lookup_param);
  }

  public Parameter load_param(ParameterCollection model, String key) {
    return new Parameter(dynet_swigJNI.ZipFileLoader_load_param(swigCPtr, this, ParameterCollection.getCPtr(model), model, key), true);
  }

  public LookupParameter load_lookup_param(ParameterCollection model, String key) {
    return new LookupParameter(dynet_swigJNI.ZipFileLoader_load_lookup_param(swigCPtr, this, ParameterCollection.getCPtr(model), model, key), true);
  }

}
