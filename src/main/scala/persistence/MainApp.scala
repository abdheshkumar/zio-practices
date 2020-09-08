package persistence

import persistence.config.Config
import persistence.dbtransactor.DBTransactor
import persistence.logging.Logger
import zio.{ExitCode, ULayer, URIO, ZIO, ZLayer}
import zio.blocking.Blocking
import zio.logging.Logging

object MainApp extends zio.App {
  val logger: ULayer[Logging] = Logger.live
  val config: ZLayer[Any, Throwable, Config] = logger >>> Config.live
  val transactor: ZLayer[Blocking, Throwable, DBTransactor] =
    logger ++ Blocking.any ++ config >>> DBTransactor.live
  val userPersistence: ZLayer[Blocking, Throwable, CustomerService] =
    transactor >>> CustomerService.live

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    ZIO.succeed(()).exitCode
  }
}
