package org.clulab.fatdynet

import java.io.BufferedReader

import edu.cmu.dynet._
import org.clulab.fatdynet.design._
import org.clulab.fatdynet.parser._
import org.clulab.fatdynet.utils.BaseTextLoader
import org.clulab.fatdynet.utils.Closer.AutoCloser
import org.clulab.fatdynet.utils.Header
import org.clulab.fatdynet.utils.HeaderIterator
import org.clulab.fatdynet.utils.RawTextLoader
import org.clulab.fatdynet.utils.ZipTextLoader

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Repo(val textLoader: BaseTextLoader) {

  class ParseException(cause: Exception, line: Option[String] = None, lineNo: Option[Int] = None) extends RuntimeException {

    def this(cause: Exception, line: String, lineNo: Int) = this(cause, Option(line), Option(lineNo))

    override def toString: String = {
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

    def getHeadersQuickly(bufferedReader: BufferedReader): Iterator[Header] = new HeaderIterator(bufferedReader)

    try {
      var currentParser: Option[Parser] = None
//      Source.fromFile(filename).autoClose { source =>
//        getHeadersSlowly(source)
      textLoader.newBufferedReader().autoClose { bufferedReader =>
        getHeadersQuickly(bufferedReader)
            .foreach { header =>
//              println(header)
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

    textLoader.newModelLoader().autoClose { modelLoader =>
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

  def apply(filename: String): Repo = {
    new Repo(new RawTextLoader(filename))
  }

  def apply(filename: String, zipname: String): Repo = {
    new Repo(new ZipTextLoader(filename, zipname))
  }
}
