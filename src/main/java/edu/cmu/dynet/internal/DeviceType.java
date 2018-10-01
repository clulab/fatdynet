/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.dynet.internal;

public final class DeviceType {
  public final static DeviceType CPU = new DeviceType("CPU");
  public final static DeviceType GPU = new DeviceType("GPU");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static DeviceType swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + DeviceType.class + " with value " + swigValue);
  }

  private DeviceType(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private DeviceType(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private DeviceType(String swigName, DeviceType swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static DeviceType[] swigValues = { CPU, GPU };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
