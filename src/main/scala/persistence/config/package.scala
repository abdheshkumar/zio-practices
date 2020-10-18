package persistence

import scala.jdk.CollectionConverters._

import eu.timepit.refined.pureconfig._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import zio.logging.{Logging, log}
import zio.{Task, _}

package object config {

  type Config =
    Has[HttpServerConfig] with Has[HttpClientConfig] with Has[DBConfig]

  object Config {

    private val buildEnv: Task[String] =
      Task.effect {
        System
          .getenv()
          .asScala
          .map(v => s"${v._1} = ${v._2}")
          .mkString("\n", "\n", "")
      }

    private def logEnv(ex: Throwable): ZIO[Logging, Throwable, Unit] =
      for {
        env <- buildEnv
        _ <- log.error(
          s"Loading configuration failed with the following environment variables: $env."
        )
        _ <- log.error(s"Error thrown was $ex.")
      } yield ()

    val live: ZLayer[Logging, Throwable, Config] = ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[AppConfig])
        .map(c => Has(c.httpServer) ++ Has(c.httpClient) ++ Has(c.dbConfig))
        .tapError(logEnv)
    )

    val httpServerConfig: URIO[Has[HttpServerConfig], HttpServerConfig] =
      ZIO.service
    val httpClientConfig: URIO[Has[HttpClientConfig], HttpClientConfig] =
      ZIO.service
    val dbConfig: URIO[Has[DBConfig], DBConfig] = ZIO.service
  }
}
