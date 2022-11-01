package persistence.config

import pureconfig.ConfigSource
import eu.timepit.refined.pureconfig._ // Do not remove
import pureconfig.generic.auto._
import zio.{System, Task, ZIO, ZLayer}

object Config {

  private val buildEnv: Task[String] =
    System.envs
      .map(_.toList.map(v => s"${v._1} = ${v._2}").mkString("\n", "\n", ""))

  private def logEnv(ex: Throwable): ZIO[Any, Throwable, Unit] =
    for {
      env <- buildEnv
      _ <- ZIO.logInfo(
        s"Loading configuration failed with the following environment variables: $env."
      )
      _ <- ZIO.logError(s"Error thrown was $ex.")
    } yield ()

  val live: ZLayer[
    Any,
    Throwable,
    AppConfig
  ] = ZLayer {
    ZIO.environmentWithZIO
    ZIO
      .attempt(ConfigSource.default.loadOrThrow[AppConfig])
      .tapError(logEnv)
  }

}
