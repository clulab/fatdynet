package org.clulab.fatdynet.test

import edu.cmu.dynet.Dim
import edu.cmu.dynet.ParameterCollection
import org.clulab.fatdynet.FatdynetTest
import org.clulab.fatdynet.Repo
import org.clulab.fatdynet.utils.BaseTextLoader
import org.clulab.fatdynet.utils.BaseTextModelLoader
import org.clulab.fatdynet.utils.Initializer
import org.clulab.fatdynet.utils.RawTextLoader
import org.clulab.fatdynet.utils.RawTextModelLoader
import org.clulab.fatdynet.utils.ZipTextLoader
import org.clulab.fatdynet.utils.ZipTextModelLoader

class TestLoader extends FatdynetTest {
  val name = "/name"
  val expectedValue = "-0.14423102"

  Initializer.cluInitialize()

  def loadParameter(textModelLoader: BaseTextModelLoader): Unit = {
    // This is placed here so that it gets garbage collected timely.
    val dim: Dim = Dim(51)
    val parameterCollection = new ParameterCollection()
    val parameter = parameterCollection.addParameters(dim)

    textModelLoader.populateParameter(parameter, name)

    val values = parameter.values().toSeq()

    values.head.toString should be (expectedValue)
  }

  def loadParameter(textModelLoader: BaseTextLoader): Unit = {
    val repo = new Repo(textModelLoader)
    val designs = repo.getDesigns()
    val model = repo.getModel(designs, name)
    val parameter = model.getParameter()
    val values = parameter.values().toSeq()

    values.head.toString should be (expectedValue)
  }

  behavior of "Loader"

  it should "work on a plain file in the DyNet way" in {
    val textModelLoader = BaseTextModelLoader.newTextModelLoader("./src/test/resources/parameter.dat")

    textModelLoader.isInstanceOf[RawTextModelLoader] should be (true)
    loadParameter(textModelLoader)
  }

  it should "work on a resource file in the DyNet way" in {
    val textModelLoader = BaseTextModelLoader.newTextModelLoader("parameter.dat")

    textModelLoader.isInstanceOf[RawTextModelLoader] should be (true)
    loadParameter(textModelLoader)
  }

  it should "work on a zip standin for a resource file in the DyNet way" in {
    // It seems impossible to make this except by hand.
    val textModelLoader = new ZipTextModelLoader("directory/parameter.dat", "./src/test/resources/parameter.zip")

    textModelLoader.isInstanceOf[ZipTextModelLoader] should be (true)
    loadParameter(textModelLoader)
  }

  it should "work on a plain file in the FatDynet way" in {
    val textModelLoader = BaseTextLoader.newTextLoader("./src/test/resources/parameter.dat")

    textModelLoader.isInstanceOf[RawTextLoader] should be (true)
    loadParameter(textModelLoader)
  }

  it should "work on a resource file in the FatDynet way" in {
    val textModelLoader = BaseTextLoader.newTextLoader("parameter.dat")

    textModelLoader.isInstanceOf[RawTextLoader] should be (true)
    loadParameter(textModelLoader)
  }

  it should "work on a zip standin for a resource file in the FatDynet way" in {
    // It seems impossible to make this except by hand.
    val textModelLoader = new ZipTextLoader("directory/parameter.dat", "./src/test/resources/parameter.zip")

    textModelLoader.isInstanceOf[ZipTextLoader] should be (true)
    loadParameter(textModelLoader)
  }
}
