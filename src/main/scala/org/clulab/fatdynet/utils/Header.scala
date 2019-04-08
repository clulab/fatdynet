package org.clulab.fatdynet.utils

import java.io.BufferedReader
import java.io.IOException

class Header(val line: String, val lineNo: Int) {
  val Array(objectType, objectName, dimension, len, _) = line.split(' ')
  // Skip leading { and trailing }
  val dims = dimension.substring(1, dimension.length - 1).split(',').map(_.toInt)
  val length = {
    val size = len.toLong
    val length = dims.foldLeft(16L){ (product, next) => product * next } + 1

//    if (size != length)
//      println(s"Size $size doesn't equal length $length!  This is a bug!")
    length
  }

  override def toString: String =
    s"lineNo: $lineNo, length: $length, line: $line"
}

object HeaderIterator {
  val head = '#'
  // Some of this is just in case the line endings were somehow mangled.
  val crChar = '\r'
  val lfChar = '\n'
  val crString = crChar.toString
  val lfString = lfChar.toString
  val crlf = crString + lfString
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

  protected def read(expected: String, line: String): Char = {
    val value = bufferedReader.read

    if (value == -1)
      throw new IOException("Unexpected EOF")
    if (!expected.contains(value))
      throw new IOException(s"Expected one of '$expected' buf found '${value.toChar}' after $line")
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

        if (c == HeaderIterator.lfChar)
          found = true
        else if (c != HeaderIterator.crChar)
          stringBuilder.append(c)
      }

      val line = stringBuilder.toString
      val header = new Header(line, lineNo)

      bufferedReader.skip(header.length - 1)
      var c = read(HeaderIterator.crlf, line)
      if (c == HeaderIterator.crChar)
        c = read(HeaderIterator.lfString, line)

      header
    }
    else
      throw new IOException("There is no next!")
  }
}
