package effect.config

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.GreaterEqual
import zio.config.magnolia.{describe, descriptor}
import zio.config.typesafe.TypesafeConfigSource
import zio.{ExitCode, URIO, ZIO}
import zio.Console
sealed trait DataSource

final case class Database(
    @describe("Database Host Name")
    host: Refined[String, NonEmpty],
    @describe("Database Port")
    port: Refined[Int, GreaterEqual[W.`1024`.T]]
) extends DataSource

final case class Kafka(
    @describe("Kafka Topics")
    topicName: String,
    @describe("Kafka Brokers")
    brokers: List[String]
) extends DataSource

object ZIOConfigExample extends zio.ZIOAppDefault {
  import zio.config._
  import zio.config.refined._

  val json =
    s"""
       |"Database" : {
       |  "port" : "1024",
       |  "host" : "localhost"
       |}
       |""".stripMargin

  val myApp: ZIO[Any, Throwable, Unit] =
    for {
      source <- ZIO.attempt(TypesafeConfigSource.fromHoconString(json))
      desc = descriptor[DataSource] from source
      dataSource <- read(desc)
      // Printing Auto Generated Documentation of Application Config
      _ <-
        Console.printLine(generateDocs(desc).toTable.toGithubFlavouredMarkdown)
      _ <- dataSource match {
        case Database(host, port) =>
          Console.printLine(s"Start connecting to the database: $host:$port")
        case Kafka(_, brokers) =>
          Console.printLine(s"Start connecting to the kafka brokers: $brokers")
      }
    } yield ()

  override def run = myApp
}
