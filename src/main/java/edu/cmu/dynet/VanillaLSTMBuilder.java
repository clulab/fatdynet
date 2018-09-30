/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class VanillaLSTMBuilder extends RNNBuilder {
  private transient long swigCPtr;

  protected VanillaLSTMBuilder(long cPtr, boolean cMemoryOwn) {
    super(dynet_swigJNI.VanillaLSTMBuilder_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(VanillaLSTMBuilder obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_VanillaLSTMBuilder(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public VanillaLSTMBuilder() {
    this(dynet_swigJNI.new_VanillaLSTMBuilder__SWIG_0(), true);
  }

  public VanillaLSTMBuilder(long layers, long input_dim, long hidden_dim, ParameterCollection model, boolean ln_lstm, float forget_bias) {
    this(dynet_swigJNI.new_VanillaLSTMBuilder__SWIG_1(layers, input_dim, hidden_dim, ParameterCollection.getCPtr(model), model, ln_lstm, forget_bias), true);
  }

  public VanillaLSTMBuilder(long layers, long input_dim, long hidden_dim, ParameterCollection model, boolean ln_lstm) {
    this(dynet_swigJNI.new_VanillaLSTMBuilder__SWIG_2(layers, input_dim, hidden_dim, ParameterCollection.getCPtr(model), model, ln_lstm), true);
  }

  public VanillaLSTMBuilder(long layers, long input_dim, long hidden_dim, ParameterCollection model) {
    this(dynet_swigJNI.new_VanillaLSTMBuilder__SWIG_3(layers, input_dim, hidden_dim, ParameterCollection.getCPtr(model), model), true);
  }

  public Expression back() {
    return new Expression(dynet_swigJNI.VanillaLSTMBuilder_back(swigCPtr, this), true);
  }

  public ExpressionVector final_h() {
    return new ExpressionVector(dynet_swigJNI.VanillaLSTMBuilder_final_h(swigCPtr, this), true);
  }

  public ExpressionVector final_s() {
    return new ExpressionVector(dynet_swigJNI.VanillaLSTMBuilder_final_s(swigCPtr, this), true);
  }

  public long num_h0_components() {
    return dynet_swigJNI.VanillaLSTMBuilder_num_h0_components(swigCPtr, this);
  }

  public ExpressionVector get_h(int i) {
    return new ExpressionVector(dynet_swigJNI.VanillaLSTMBuilder_get_h(swigCPtr, this, i), true);
  }

  public ExpressionVector get_s(int i) {
    return new ExpressionVector(dynet_swigJNI.VanillaLSTMBuilder_get_s(swigCPtr, this, i), true);
  }

  public void copy(RNNBuilder params) {
    dynet_swigJNI.VanillaLSTMBuilder_copy(swigCPtr, this, RNNBuilder.getCPtr(params), params);
  }

  public void set_dropout(float d) {
    dynet_swigJNI.VanillaLSTMBuilder_set_dropout__SWIG_0(swigCPtr, this, d);
  }

  public void set_dropout(float d, float d_r) {
    dynet_swigJNI.VanillaLSTMBuilder_set_dropout__SWIG_1(swigCPtr, this, d, d_r);
  }

  public void disable_dropout() {
    dynet_swigJNI.VanillaLSTMBuilder_disable_dropout(swigCPtr, this);
  }

  public void set_dropout_masks(long batch_size) {
    dynet_swigJNI.VanillaLSTMBuilder_set_dropout_masks__SWIG_0(swigCPtr, this, batch_size);
  }

  public void set_dropout_masks() {
    dynet_swigJNI.VanillaLSTMBuilder_set_dropout_masks__SWIG_1(swigCPtr, this);
  }

  public ParameterCollection get_parameter_collection() {
    return new ParameterCollection(dynet_swigJNI.VanillaLSTMBuilder_get_parameter_collection(swigCPtr, this), false);
  }

  public void setLocal_model(ParameterCollection value) {
    dynet_swigJNI.VanillaLSTMBuilder_local_model_set(swigCPtr, this, ParameterCollection.getCPtr(value), value);
  }

  public ParameterCollection getLocal_model() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_local_model_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ParameterCollection(cPtr, false);
  }

  public void setParams(ParameterVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_params_set(swigCPtr, this, ParameterVectorVector.getCPtr(value), value);
  }

  public ParameterVectorVector getParams() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_params_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ParameterVectorVector(cPtr, false);
  }

  public void setLn_params(ParameterVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_ln_params_set(swigCPtr, this, ParameterVectorVector.getCPtr(value), value);
  }

  public ParameterVectorVector getLn_params() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_ln_params_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ParameterVectorVector(cPtr, false);
  }

  public void setParam_vars(ExpressionVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_param_vars_set(swigCPtr, this, ExpressionVectorVector.getCPtr(value), value);
  }

  public ExpressionVectorVector getParam_vars() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_param_vars_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVectorVector(cPtr, false);
  }

  public void setLn_param_vars(ExpressionVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_ln_param_vars_set(swigCPtr, this, ExpressionVectorVector.getCPtr(value), value);
  }

  public ExpressionVectorVector getLn_param_vars() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_ln_param_vars_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVectorVector(cPtr, false);
  }

  public void setMasks(ExpressionVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_masks_set(swigCPtr, this, ExpressionVectorVector.getCPtr(value), value);
  }

  public ExpressionVectorVector getMasks() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_masks_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVectorVector(cPtr, false);
  }

  public void setH(ExpressionVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_h_set(swigCPtr, this, ExpressionVectorVector.getCPtr(value), value);
  }

  public ExpressionVectorVector getH() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_h_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVectorVector(cPtr, false);
  }

  public void setC(ExpressionVectorVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_c_set(swigCPtr, this, ExpressionVectorVector.getCPtr(value), value);
  }

  public ExpressionVectorVector getC() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_c_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVectorVector(cPtr, false);
  }

  public void setHas_initial_state(boolean value) {
    dynet_swigJNI.VanillaLSTMBuilder_has_initial_state_set(swigCPtr, this, value);
  }

  public boolean getHas_initial_state() {
    return dynet_swigJNI.VanillaLSTMBuilder_has_initial_state_get(swigCPtr, this);
  }

  public void setH0(ExpressionVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_h0_set(swigCPtr, this, ExpressionVector.getCPtr(value), value);
  }

  public ExpressionVector getH0() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_h0_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVector(cPtr, false);
  }

  public void setC0(ExpressionVector value) {
    dynet_swigJNI.VanillaLSTMBuilder_c0_set(swigCPtr, this, ExpressionVector.getCPtr(value), value);
  }

  public ExpressionVector getC0() {
    long cPtr = dynet_swigJNI.VanillaLSTMBuilder_c0_get(swigCPtr, this);
    return (cPtr == 0) ? null : new ExpressionVector(cPtr, false);
  }

  public void setLayers(long value) {
    dynet_swigJNI.VanillaLSTMBuilder_layers_set(swigCPtr, this, value);
  }

  public long getLayers() {
    return dynet_swigJNI.VanillaLSTMBuilder_layers_get(swigCPtr, this);
  }

  public void setInput_dim(long value) {
    dynet_swigJNI.VanillaLSTMBuilder_input_dim_set(swigCPtr, this, value);
  }

  public long getInput_dim() {
    return dynet_swigJNI.VanillaLSTMBuilder_input_dim_get(swigCPtr, this);
  }

  public void setHid(long value) {
    dynet_swigJNI.VanillaLSTMBuilder_hid_set(swigCPtr, this, value);
  }

  public long getHid() {
    return dynet_swigJNI.VanillaLSTMBuilder_hid_get(swigCPtr, this);
  }

  public void setDropout_rate_h(float value) {
    dynet_swigJNI.VanillaLSTMBuilder_dropout_rate_h_set(swigCPtr, this, value);
  }

  public float getDropout_rate_h() {
    return dynet_swigJNI.VanillaLSTMBuilder_dropout_rate_h_get(swigCPtr, this);
  }

  public void setLn_lstm(boolean value) {
    dynet_swigJNI.VanillaLSTMBuilder_ln_lstm_set(swigCPtr, this, value);
  }

  public boolean getLn_lstm() {
    return dynet_swigJNI.VanillaLSTMBuilder_ln_lstm_get(swigCPtr, this);
  }

  public void setForget_bias(float value) {
    dynet_swigJNI.VanillaLSTMBuilder_forget_bias_set(swigCPtr, this, value);
  }

  public float getForget_bias() {
    return dynet_swigJNI.VanillaLSTMBuilder_forget_bias_get(swigCPtr, this);
  }

  public void setDropout_masks_valid(boolean value) {
    dynet_swigJNI.VanillaLSTMBuilder_dropout_masks_valid_set(swigCPtr, this, value);
  }

  public boolean getDropout_masks_valid() {
    return dynet_swigJNI.VanillaLSTMBuilder_dropout_masks_valid_get(swigCPtr, this);
  }

}
