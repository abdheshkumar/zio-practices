import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import scala.io.Source

object FBFriendAssignment extends App {
  type Positions = List[Int]
  type DocumentKey = String
  type Word = String
  type NumberOfDocs = Int
  case class InvertedIndex(
      word: Word,
      numberOfDocs: NumberOfDocs,
      positionsWithInDoc: Map[DocumentKey, Positions]
  ) {

    def toJson = {
      //Scala String Interpolator
      val positionsJson =
        positionsWithInDoc.map(value => "{" + "\"" + value._1 +"\"" + ":" + value._2.mkString("[", ",", "]")+ "}").mkString(",")
      s"{" +"\""+word+"\"" + ":" + List(numberOfDocs, positionsJson).mkString("[", ",", "]")+"}"
    }

  }

  /**
    * Word with its positions in the files
    * @param word
    * @param fileContent
    * @return
    */
  def findPositions(word: String, fileContent: String): Positions = {
    val wordLength = word.length //hi -> 2
    //Recursive call - It is a loop
    def find(lastPosition: Int, positions: List[Int]): List[Int] = {
      val index =
        fileContent.indexOf(
          word,
          lastPosition + wordLength
        ) // 14 + 2 -> 16 -> FromIndex
      if (index == -1) positions //Stopped loop
      else find(index, index +: positions) //List(14,0)
    }
    find(-wordLength, List.empty) //List(14,0)
  }
  val files =
    new File(getClass.getClassLoader.getResource("files").getFile).listFiles()

  val result = files.foldLeft(Map.empty[Word, InvertedIndex]) {
    case (acc, file) =>
      val documentKey = file.getName // DocumentKey
      val fileContent = new String(
        Files.readAllBytes(Paths.get(file.getPath)),
        StandardCharsets.UTF_8
      )
      val keys = fileContent
        .split("[\n]")
        .flatMap(line =>
          line.split("[ ]").filterNot(_.length <= 3)
        )
        .groupBy(identity)
        .keys
println(keys.toList)
      keys.foldLeft(acc) { //keys unique words in the currently processing file
        case (acc, word) =>
          val positions = findPositions(word, fileContent)

          /**
            * hi->InvertedIndex(1, Map(file01.text->List(14,0), file02.txt-> List(12)))
            * test->InvertedIndex(1, Map(file01.text->List(3)))
            */
          acc.updatedWith(word) { oldValue =>
            oldValue match {
              case Some(value) => //Value is exist
                val positionsWithInDoc: Map[DocumentKey, Positions] =
                  value.positionsWithInDoc
                    .updated(documentKey, positions) //file02.txt
                Some(
                  value.copy(
                    numberOfDocs = positionsWithInDoc.size,
                    positionsWithInDoc = positionsWithInDoc
                  )
                )
              case None => //Value is not exist
                Some(
                  InvertedIndex(
                    word = word,
                    numberOfDocs = 1,
                    positionsWithInDoc = Map(documentKey -> positions)
                  )
                )
            }
          }
      }
  }

  result.get("test").map(_.toJson) match {
    case Some(value) => println(s"Found: $value")
    case None        => println(s"Not Found")
  }
}
