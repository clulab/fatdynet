package org.clulab.fatdynet

import edu.cmu.dynet._

//import org.clulab.fatdynet.utils.specs._

//class SpecAndData[SpecType <: Spec, DataType](val spec: SpecType, val data: DataType)
//
//class ParameterSpecAndData(spec: ParameterSpec, data: Parameter) extends SpecAndData[ParameterSpec, Parameter](spec, data)
//
//class LookupParameterSpecAndData(spec: LookupParameterSpec, data: LookupParameter) extends SpecAndData[LookupParameterSpec, LookupParameter](spec, data)
//
//class ModelSpecAndData(spec: ModelSpec, data: RnnBuilder) extends SpecAndData[ModelSpec, RnnBuilder](spec, data)

// TODO ModelData can have multiple Rnns

class Model(val parameterCollection: ParameterCollection) {
//  var parameters: Seq[ParameterSpecAndData] = List.empty
//  var lookupParameters: Seq[LookupParameterSpecAndData] = List.empty
//  val rnnBuilders: Seq[ModelSpecAndData] = List.empty
//
//  def addSpec(spec: ParameterSpec, parameter: Parameter): Unit = ()
//
//  def addSpec(spec: LookupParameterSpec, lookupParameter: LookupParameter): Unit = ()
//
//  def addSpec(spec: RnnBuilderSpec, rnnBuilder: RnnBuilder): Unit = ()
//
//  def get[ParameterSpecType <: Spec, ParameterType](name: String, values: Seq[(ParameterSpecType, ParameterType)]): Option[ParameterType] = {
//    values.find { case (spec, _) =>
//      spec.name == name
//    }.map { case (_, parameter) =>
//      parameter
//    }
//  }
//
//  def get[ParameterSpecType <: Spec, ParameterType](index: Int, values: Seq[(ParameterSpecType, ParameterType)]): Option[ParameterType] = {
//    values.find { case (spec, _) =>
//      spec.localIndex == index
//    }.map { case (_, parameter) =>
//      parameter
//    }
//  }

//  def getParameter(name: String): Option[Parameter] = get(name, parameters)
//
//  def getParameter(index: Int): Option[Parameter] = get(index, parameters)
//
//  def getLookupParameter(name: String): Option[LookupParameter] = get(name, lookupParameters)
//
//  def getLookupParameter(index: Int): Option[LookupParameter] = get(index, lookupParameters)
//
//  def getBuilder(name: String): Option[RnnBuilder] = get(name, rnnBuilders)
//
//  def getBuilder(index: Int): Option[RnnBuilder] = get(index, rnnBuilders)

  // TODO add getSpec so that can query name, dimensions, etc.
}

