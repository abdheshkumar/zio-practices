package streaming.environment.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

object Configuration {

  final case class DbConfig(
      driver: String,
      url: String,
      user: String,
      password: String
  )
  final case class HttpServerConfig(host: String, port: Int, path: String)
  final case class AppConfig(dbConfig: DbConfig, httpServer: HttpServerConfig)

  val live: ULayer[AppConfig] = ZLayer(
    ZIO
      .attempt(ConfigSource.default.loadOrThrow[AppConfig])
      .orDie
  )
}
