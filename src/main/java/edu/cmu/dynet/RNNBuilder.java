/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class RNNBuilder {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected RNNBuilder(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(RNNBuilder obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_RNNBuilder(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int state() {
    return dynet_swigJNI.RNNBuilder_state(swigCPtr, this);
  }

  public void new_graph(ComputationGraph cg, boolean update) {
    dynet_swigJNI.RNNBuilder_new_graph__SWIG_0(swigCPtr, this, ComputationGraph.getCPtr(cg), cg, update);
  }

  public void new_graph(ComputationGraph cg) {
    dynet_swigJNI.RNNBuilder_new_graph__SWIG_1(swigCPtr, this, ComputationGraph.getCPtr(cg), cg);
  }

  public void start_new_sequence(ExpressionVector h_0) {
    dynet_swigJNI.RNNBuilder_start_new_sequence__SWIG_0(swigCPtr, this, ExpressionVector.getCPtr(h_0), h_0);
  }

  public void start_new_sequence() {
    dynet_swigJNI.RNNBuilder_start_new_sequence__SWIG_1(swigCPtr, this);
  }

  public Expression set_h(int prev, ExpressionVector h_new) {
    return new Expression(dynet_swigJNI.RNNBuilder_set_h__SWIG_0(swigCPtr, this, prev, ExpressionVector.getCPtr(h_new), h_new), true);
  }

  public Expression set_h(int prev) {
    return new Expression(dynet_swigJNI.RNNBuilder_set_h__SWIG_1(swigCPtr, this, prev), true);
  }

  public Expression set_s(int prev, ExpressionVector s_new) {
    return new Expression(dynet_swigJNI.RNNBuilder_set_s__SWIG_0(swigCPtr, this, prev, ExpressionVector.getCPtr(s_new), s_new), true);
  }

  public Expression set_s(int prev) {
    return new Expression(dynet_swigJNI.RNNBuilder_set_s__SWIG_1(swigCPtr, this, prev), true);
  }

  public Expression add_input(Expression x) {
    return new Expression(dynet_swigJNI.RNNBuilder_add_input__SWIG_0(swigCPtr, this, Expression.getCPtr(x), x), true);
  }

  public Expression add_input(int prev, Expression x) {
    return new Expression(dynet_swigJNI.RNNBuilder_add_input__SWIG_1(swigCPtr, this, prev, Expression.getCPtr(x), x), true);
  }

  public void rewind_one_step() {
    dynet_swigJNI.RNNBuilder_rewind_one_step(swigCPtr, this);
  }

  public int get_head(int p) {
    return dynet_swigJNI.RNNBuilder_get_head(swigCPtr, this, p);
  }

  public void set_dropout(float d) {
    dynet_swigJNI.RNNBuilder_set_dropout(swigCPtr, this, d);
  }

  public void disable_dropout() {
    dynet_swigJNI.RNNBuilder_disable_dropout(swigCPtr, this);
  }

  public Expression back() {
    return new Expression(dynet_swigJNI.RNNBuilder_back(swigCPtr, this), true);
  }

  public ExpressionVector final_h() {
    return new ExpressionVector(dynet_swigJNI.RNNBuilder_final_h(swigCPtr, this), true);
  }

  public ExpressionVector get_h(int i) {
    return new ExpressionVector(dynet_swigJNI.RNNBuilder_get_h(swigCPtr, this, i), true);
  }

  public ExpressionVector final_s() {
    return new ExpressionVector(dynet_swigJNI.RNNBuilder_final_s(swigCPtr, this), true);
  }

  public ExpressionVector get_s(int i) {
    return new ExpressionVector(dynet_swigJNI.RNNBuilder_get_s(swigCPtr, this, i), true);
  }

  public long num_h0_components() {
    return dynet_swigJNI.RNNBuilder_num_h0_components(swigCPtr, this);
  }

  public void copy(RNNBuilder params) {
    dynet_swigJNI.RNNBuilder_copy(swigCPtr, this, RNNBuilder.getCPtr(params), params);
  }

  public ParameterCollection get_parameter_collection() {
    return new ParameterCollection(dynet_swigJNI.RNNBuilder_get_parameter_collection(swigCPtr, this), false);
  }

}
