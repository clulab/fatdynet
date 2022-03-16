// Because this class must have access to model.model, which is marked
// private[dynet], it be packaged here, even though it comes from clulab.
package org.clulab.dynet

class ZipModelLoader private[dynet](loader: internal.ZipFileLoader) {
  def this(filename: String, zipname: String) = { this(new internal.ZipFileLoader(filename, zipname))}

  def close(): Unit = done()

  def populateModel(model: ParameterCollection, key: String = ""): Unit = loader.populate(model.model, key)
  def populateParameter(p: Parameter, key: String = ""): Unit = loader.populate(p.parameter, key)
  def populateLookupParameter(p: LookupParameter, key: String = ""): Unit = loader.populate(p.lookupParameter, key)

  def done(): Unit = loader.delete()
}
