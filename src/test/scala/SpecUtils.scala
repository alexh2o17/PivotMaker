
import scala.io.{BufferedSource, Source}

object SpecUtils {

    def readResourceFromPath(path: String): List[String] = {
      val source = Source.fromInputStream(getClass.getResourceAsStream(path))
      val res = source.getLines().toList
      source.close
      res
    }

    def getSourceFromPath(path: String) : BufferedSource = Source.fromInputStream(getClass.getResourceAsStream(path))

    def readCSV(path: String, separator: Char) : List[List[String]] = {
      readResourceFromPath(path).map(_.split(separator).toList)
    }
}
