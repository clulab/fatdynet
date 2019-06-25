package org.clulab.dynet

import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import scala.collection.JavaConverters.mapAsJavaMap
import scala.io.Source

class TestLoader extends FlatSpec with Matchers {
  val RNN_STATE_SIZE = 50
  val NONLINEAR_SIZE = 32
  val RNN_LAYERS = 1
  val CHAR_RNN_LAYERS = 1
  val CHAR_EMBEDDING_SIZE = 32
  val CHAR_RNN_STATE_SIZE = 16

  case class Sizeable(size: Int)

  val w2i = Sizeable(100)
  val t2i = Sizeable(230)
  val c2i = Sizeable(123)
  val embeddingDim = 300

  // See https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
  def zip(rawFilename: String, zipFilename: String): Unit = {
    val zipUri = new File(zipFilename).toURI.toString
    val jarUri = URI.create(s"jar:$zipUri")
    val env = mapAsJavaMap(Map("create" -> "true"))
    val zipFileSystem = FileSystems.newFileSystem(jarUri, env)
    val origPath = Paths.get(rawFilename)
    val zipPath = zipFileSystem.getPath(rawFilename)

    Files.copy(origPath, zipPath, StandardCopyOption.REPLACE_EXISTING)
    zipFileSystem.close()
  }

  def textFromFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val text = source.mkString

    source.close()
    text
  }
}
