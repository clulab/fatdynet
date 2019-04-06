package org.clulab.fatdynet.utils

import java.io.BufferedReader
import java.io.IOException

class Header(val line: String, val lineNo: Int) {
  val Array(objectType, objectName, dimension, len, _) = line.split(' ')
  val length = len.toInt
  // Skip leading { and trailing }
  val dims = dimension.substring(1, dimension.length - 1).split(',').map(_.toInt)

  override def toString: String =
    s"lineNo: $lineNo, length: $length, line: $line"
}

object HeaderIterator {
  val head = '#'
  val cr = '\r'
  val nl = '\n'
}

class HeaderIterator(bufferedReader: BufferedReader) extends Iterator[Header] {
  var hasNextOpt: Option[Boolean] = None
  var lineNo: Int = -2

  protected def read(): Char = {
    val value = bufferedReader.read

    if (value == -1)
      throw new IOException("Unexpected EOF")
    value.toChar
  }

  override def hasNext: Boolean = {
    if (hasNextOpt.isEmpty) {
      var value = bufferedReader.read

      hasNextOpt = Some(value != -1 && value.toChar == HeaderIterator.head)
    }
    hasNextOpt.get
  }

  override def next(): Header = {
    if (hasNextOpt.isEmpty)
      hasNext
    if (hasNextOpt.get) {
      hasNextOpt = None
      val stringBuilder = new StringBuffer(HeaderIterator.head.toString)
      lineNo += 2

      var found = false
      while (!found) {
        val c = read()

        if (c == HeaderIterator.nl)
          found = true
        else if (c != HeaderIterator.cr)
          stringBuilder.append(c)
      }

      val line = stringBuilder.toString
      val header = new Header(line, lineNo)

      bufferedReader.skip(header.length - 1)
      var c = read()
      if (c == HeaderIterator.cr)
        c = read()
      require(c == HeaderIterator.nl)

      header
    }
    else
      throw new IOException("There is no next!")
  }
}
