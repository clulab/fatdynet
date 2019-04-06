package org.clulab.fatdynet

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

import edu.cmu.dynet._
import org.clulab.fatdynet.design._
import org.clulab.fatdynet.parser._
import org.clulab.fatdynet.utils.ClosableModelLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Header

import scala.collection.mutable.ArrayBuffer
import scala.io.BufferedSource
import scala.io.Codec
import scala.io.Source
import scala.io.Source.DefaultBufSize

class Repo(val filename: String) {



  object HeaderIterator {
    val head = '#'
    val cr = '\r'
    val nl = '\n'
  }

  class HeaderIterator(bufferedReader: InputStreamReader) extends Iterator[Header] {
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
        throw new IOException()
    }
  }

  class ParseException(cause: Exception, line: Option[String] = None, lineNo: Option[Int] = None) extends RuntimeException {

    def this(cause: Exception, line: String, lineNo: Int) = this(cause, Option(line), Option(lineNo))

    override def toString(): String = {
      cause.getMessage
    }
  }

  protected def getDesigns(parserFactories: Seq[Repo.ParserFactory], designs: ArrayBuffer[Design], optionParser: Option[Parser], header: Header): Option[Parser] = {
    var currentOptionsParser = optionParser

    try {
      val parsed = currentOptionsParser.isDefined && currentOptionsParser.get.parse(header)

      if (currentOptionsParser.isDefined && !parsed) {
        // !parsed implies that the parser has completed its job and needs to finish().
        designs += currentOptionsParser.get.finish()
        currentOptionsParser = None
      }
      if (currentOptionsParser.isEmpty) {
        currentOptionsParser = parserFactories.foldLeft(currentOptionsParser) { case (optionParser, parserFactory) =>
          optionParser.orElse(parserFactory(header))
        }
        currentOptionsParser.map(_.parse(header)).getOrElse(throw new Exception("Parser is not defined."))
      }
    }
    catch {
      case exception: Exception => throw new ParseException(exception, header.line, header.lineNo)
    }
    currentOptionsParser
  }

  def getDesigns(parserFactories: Seq[Repo.ParserFactory] = Repo.parserFactories): Seq[Design] = {
    val designs: ArrayBuffer[Design] = new ArrayBuffer

    def getHeadersSlowly(source: Source): Iterator[Header] = {
      source
          .getLines
          .zipWithIndex
          .filter { case (line, _) => line.startsWith("#") }
          .map { case (line, lineNo) => new Header(line, lineNo) }
    }

    def newBufferedReader(filename: String): InputStreamReader = {
      val file = new File(filename)
      val fileInputStream = new FileInputStream(file)
      val inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8.toString)
  //    val bufferedReader = new BufferedReader(inputStreamReader, 2048)

      inputStreamReader
    }

    def getHeadersQuickly(bufferedReader: InputStreamReader): Iterator[Header] = new HeaderIterator(bufferedReader)

    try {
      var currentParser: Option[Parser] = None
//      Source.fromFile(filename).autoClose { source =>
//        getHeadersSlowly(source)
      newBufferedReader(filename).autoClose { bufferedReader =>
        getHeadersQuickly(bufferedReader)
            .foreach { header =>
              println(header)
              currentParser = getDesigns(parserFactories, designs, currentParser, header)
            }
        currentParser.foreach { parser => designs += parser.finish() } // Force finish at end of file.
      }
    }
    catch {
      case exception: ParseException => throw exception
      case exception: Exception => throw new ParseException(exception)
    }

    designs
  }

  protected def reorderDesigns(designs: Seq[Design]): Seq[Design] = {
    designs.sortWith { (left, right) => left.index.get < right.index.get }
  }

  protected def orderDesigns(designs: Seq[Design]): Seq[Design] = {
    val isReorderable = designs.nonEmpty && designs.forall { design => !design.isPotentiallyReorderable || design.isActuallyReorderable }

    if (false) { // isReorderable) {
      val reorderable = designs.filter(_.isActuallyReorderable)
      val reordered = reorderDesigns(reorderable)
      var pos = 0
      // Try to avoid this by saving in a canonical order.
      val ordered = designs.map { design =>
        if (design.isActuallyReorderable) {
          // "Insert" the next one from the ordered sequence.
          val result = reordered(pos)
          pos += 1
          result
        }
        else design // Stick with what we've got.
      }

      ordered
    }
    else designs
  }

  def getModel(designs: Seq[Design], name: String): Model = {
    val parameterCollection = new ParameterCollection
    val namedDesigns = designs.filter(_.name == name)
    val orderedDesigns = orderDesigns(namedDesigns)
    val artifacts = orderedDesigns.map { design =>
        design.build(parameterCollection)
    }

    new ClosableModelLoader(filename).autoClose { modelLoader =>
        if (artifacts.size > 1)
          // They must have been thrown together into a parameter collection
          modelLoader.populateModel(parameterCollection, name)
        else
          artifacts.foreach { artifact =>
            artifact.populate(modelLoader, parameterCollection)
          }
    }
    new Model(name, parameterCollection, artifacts, orderedDesigns)
  }
}

object Repo {
  type ParserFactory = Header => Option[Parser]

  val parserFactories: Array[ParserFactory] = Array(
    CompactVanillaLstmParser.mkParser,
    CoupledLstmParser.mkParser,
    FastLstmParser.mkParser,
    GruParser.mkParser,
    SimpleRnnParser.mkParser,

    BidirectionalTreeLstmParser.mkParser, // Must be before other vanilla-lstm-builder
    UnidirectionalTreeLstmParser.mkParser, // Must be before other vanilla-lstm-builder

    LstmParser.mkParser, // Uses vanilla-lstm-builder
    VanillaLstmParser.mkParser, // Uses vanilla-lstm-builder, so hidden by LstmParser

    // These need to be last so that buildType is not used in the name.
    ParameterParser.mkParser,
    LookupParameterParser.mkParser
  )
}
