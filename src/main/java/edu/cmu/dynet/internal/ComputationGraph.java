/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public class ComputationGraph {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ComputationGraph(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ComputationGraph obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        dynet_swigJNI.delete_ComputationGraph(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public synchronized void reset() {
    delete();
    // Make sure this is updated as well!
    singletonInstance = null;
  }

  // DyNet only allows one ComputationGraph at a time. This means that if you construct them
  // manually you have to remember to delete each one before you construct a new one, or your
  // program will crash. `getNew` will handle that deletion for you.
  private static ComputationGraph singletonInstance = null;

  public synchronized static ComputationGraph getNew() {
    if (singletonInstance != null) {
      singletonInstance.delete();
    }

    singletonInstance = new ComputationGraph();
    return singletonInstance;
  }

  private ComputationGraph() {
    this(dynet_swigJNI.new_ComputationGraph(), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_input(float s, Device device) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_input__SWIG_0(swigCPtr, this, s, Device.getCPtr(device), device), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_input(Dim d, FloatVector data, Device device) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_input__SWIG_1(swigCPtr, this, Dim.getCPtr(d), d, FloatVector.getCPtr(data), data, Device.getCPtr(device), device), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_input(Dim d, UnsignedVector ids, FloatVector data, Device device, float defdata) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_input__SWIG_2(swigCPtr, this, Dim.getCPtr(d), d, UnsignedVector.getCPtr(ids), ids, FloatVector.getCPtr(data), data, Device.getCPtr(device), device, defdata), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_input(Dim d, UnsignedVector ids, FloatVector data, Device device) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_input__SWIG_3(swigCPtr, this, Dim.getCPtr(d), d, UnsignedVector.getCPtr(ids), ids, FloatVector.getCPtr(data), data, Device.getCPtr(device), device), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_parameters(Parameter p) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_parameters(swigCPtr, this, Parameter.getCPtr(p), p), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_const_parameters(Parameter p) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_const_parameters(swigCPtr, this, Parameter.getCPtr(p), p), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_lookup(LookupParameter p, SWIGTYPE_p_unsigned_int pindex) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_lookup__SWIG_0(swigCPtr, this, LookupParameter.getCPtr(p), p, SWIGTYPE_p_unsigned_int.getCPtr(pindex)), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_lookup(LookupParameter p, long index) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_lookup__SWIG_1(swigCPtr, this, LookupParameter.getCPtr(p), p, index), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_lookup(LookupParameter p, UnsignedVector pindices) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_lookup__SWIG_2(swigCPtr, this, LookupParameter.getCPtr(p), p, UnsignedVector.getCPtr(pindices), pindices), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_const_lookup(LookupParameter p, SWIGTYPE_p_unsigned_int pindex) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_const_lookup__SWIG_0(swigCPtr, this, LookupParameter.getCPtr(p), p, SWIGTYPE_p_unsigned_int.getCPtr(pindex)), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_const_lookup(LookupParameter p, long index) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_const_lookup__SWIG_1(swigCPtr, this, LookupParameter.getCPtr(p), p, index), true);
  }

  public SWIGTYPE_p_dynet__VariableIndex add_const_lookup(LookupParameter p, UnsignedVector pindices) {
    return new SWIGTYPE_p_dynet__VariableIndex(dynet_swigJNI.ComputationGraph_add_const_lookup__SWIG_2(swigCPtr, this, LookupParameter.getCPtr(p), p, UnsignedVector.getCPtr(pindices), pindices), true);
  }

  public void clear() {
    dynet_swigJNI.ComputationGraph_clear(swigCPtr, this);
  }

  public void checkpoint() {
    dynet_swigJNI.ComputationGraph_checkpoint(swigCPtr, this);
  }

  public void revert() {
    dynet_swigJNI.ComputationGraph_revert(swigCPtr, this);
  }

  public Dim get_dimension(SWIGTYPE_p_dynet__VariableIndex index) {
    return new Dim(dynet_swigJNI.ComputationGraph_get_dimension(swigCPtr, this, SWIGTYPE_p_dynet__VariableIndex.getCPtr(index)), false);
  }

  public Tensor forward(Expression last) {
    return new Tensor(dynet_swigJNI.ComputationGraph_forward(swigCPtr, this, Expression.getCPtr(last), last), false);
  }

  public Tensor incremental_forward(Expression last) {
    return new Tensor(dynet_swigJNI.ComputationGraph_incremental_forward(swigCPtr, this, Expression.getCPtr(last), last), false);
  }

  public Tensor get_value(Expression e) {
    return new Tensor(dynet_swigJNI.ComputationGraph_get_value(swigCPtr, this, Expression.getCPtr(e), e), false);
  }

  public void invalidate() {
    dynet_swigJNI.ComputationGraph_invalidate(swigCPtr, this);
  }

  public void backward(Expression last) {
    dynet_swigJNI.ComputationGraph_backward(swigCPtr, this, Expression.getCPtr(last), last);
  }

  public void print_graphviz() {
    dynet_swigJNI.ComputationGraph_print_graphviz(swigCPtr, this);
  }

  public void setNodes(SWIGTYPE_p_std__vectorT_dynet__Node_p_t value) {
    dynet_swigJNI.ComputationGraph_nodes_set(swigCPtr, this, SWIGTYPE_p_std__vectorT_dynet__Node_p_t.getCPtr(value));
  }

  public SWIGTYPE_p_std__vectorT_dynet__Node_p_t getNodes() {
    long cPtr = dynet_swigJNI.ComputationGraph_nodes_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_std__vectorT_dynet__Node_p_t(cPtr, false);
  }

  public void setParameter_nodes(SWIGTYPE_p_std__vectorT_dynet__VariableIndex_t value) {
    dynet_swigJNI.ComputationGraph_parameter_nodes_set(swigCPtr, this, SWIGTYPE_p_std__vectorT_dynet__VariableIndex_t.getCPtr(value));
  }

  public SWIGTYPE_p_std__vectorT_dynet__VariableIndex_t getParameter_nodes() {
    long cPtr = dynet_swigJNI.ComputationGraph_parameter_nodes_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_std__vectorT_dynet__VariableIndex_t(cPtr, false);
  }

}
