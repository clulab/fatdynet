package org.clulab.fatdynet

import edu.cmu.dynet._

import org.clulab.fatdynet.design.Artifact
import org.clulab.fatdynet.design.Design

class Model(val name: String, protected val parameterCollection: ParameterCollection,
    val artifacts: Seq[Artifact], val designs: Seq[Design]) {
  require(artifacts.size == designs.size)

  protected val artifactsAndDesigns: Seq[(Artifact, Design)] = artifacts.zip(designs)

  def getParameterCollection: ParameterCollection = parameterCollection

  def getParameter(index: Int = 0): Parameter = getParameterAndDesign(index)._1

  def getLookupParameter(index: Int = 0): LookupParameter = getLookupParameterAndDesign(index)._1

  def getRnnBuilder(index: Int = 0): RnnBuilder = getRnnBuilderAndDesign(index)._1

  def getParameterSize: Int = artifactsAndDesigns.count { case (artifact, _) => artifact.isParameter }

  def getLookupParameterSize: Int = artifactsAndDesigns.count { case (artifact, _) => artifact.isLookupParameter }

  def getRnnBuilderSize: Int = artifactsAndDesigns.count { case (artifact, _) => artifact.isRnnBuilder }

  def getParameterAndDesign(index: Int): (Parameter, Design) = {
    val (artifact, design) = artifactsAndDesigns
        .filter { case (artifact, _) => artifact.isParameter }
        .apply(index)

    (artifact.parameter.get, design)
  }

  def getLookupParameterAndDesign(index: Int): (LookupParameter, Design) = {
    val (artifact, design) = artifactsAndDesigns
        .filter { case (artifact, _) => artifact.isLookupParameter }
        .apply(index)

    (artifact.lookupParameter.get, design)
  }

  def getRnnBuilderAndDesign(index: Int): (RnnBuilder, Design) = {
    val (artifact, design) = artifactsAndDesigns
        .filter { case (artifact, _) => artifact.isRnnBuilder }
        .apply(index)

    (artifact.rnnBuilder.get, design)
  }
}
