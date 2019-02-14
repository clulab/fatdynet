package org.clulab.fatdynet

import org.clulab.fatdynet.design._
import org.clulab.fatdynet.parser._
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Header

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Repo(val filename: String) {

  class ParseException(cause: Exception, line: Option[String] = None, lineNo: Option[Int] = None) extends RuntimeException {

    def this(cause: Exception, line: String, lineNo: Int) = this(cause, Option(line), Option(lineNo))

    override def toString(): String = {
      cause.getMessage
    }
  }

  protected def getDesigns(parserFactories: Seq[Repo.ParserFactory], designs: ArrayBuffer[Design], optionParser: Option[Parser], line: String, lineNo: Int): Option[Parser] = {
    var currentOptionsParser = optionParser

    try {
      val header = new Header(line, lineNo)
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
      case exception: Exception => throw new ParseException(exception, line, lineNo)
    }
    currentOptionsParser
  }

  def getDesigns(parserFactories: Seq[Repo.ParserFactory] = Repo.parserFactories): Seq[Design] = {
    val designs: ArrayBuffer[Design] = new ArrayBuffer

    try {
      var currentParser: Option[Parser] = None

      Source.fromFile(filename).autoClose { source =>
        source
            .getLines
            .zipWithIndex
            .filter { case (line, _) => line.startsWith("#") }
            .foreach { case (line, lineNo) =>
              currentParser = getDesigns(parserFactories, designs, currentParser, line, lineNo)
            }
        currentParser.foreach { parser => designs += parser.finish() } // Force finish at end of file.
      }
    }
    catch {
      case exception: ParseException => throw exception
      case exception: Exception => throw new ParseException(exception)
    }

    designs
    // TODO.  These designs must be grouped and organized into param, lparam, and model
  }

  def mapDesigns(designs: Seq[Design]): Map[String, Seq[Design]] = {
    val names = designs.map(_.name)
    val designsByName = names.map { name => name -> designs.filter(_.name == name)}.toMap

    designsByName
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
