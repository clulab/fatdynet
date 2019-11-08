package org.clulab.fatdynet.utils

import collection.JavaConverters._

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.net.URI

import org.clulab.fatdynet.utils.Closer.AutoCloser

object Zipper {

  def zip(filename: String, zipname: String): Unit = {
    // See https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
    val zipUri = new File(zipname).toURI.toString
    val jarUri = URI.create(s"jar:$zipUri")
    val env = Map("create" -> "true").asJava

    FileSystems.newFileSystem(jarUri, env).autoClose { zipFileSystem =>
      val origPath = Paths.get(filename)
      val zipPath = zipFileSystem.getPath(filename)

      Files.copy(origPath, zipPath, StandardCopyOption.REPLACE_EXISTING)
    }
  }
}
