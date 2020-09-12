package persistence

import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import persistence.api.Api
import persistence.config.Config
import persistence.dbtransactor.DBTransactor
import persistence.logging.Logger
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.Logging
import zio.interop.catz._
import org.http4s.implicits._
import cats.effect.{ExitCode => CatsExitCode}
import persistence.User.UserService

object MainApp extends zio.App {
  type AppEnvironment = Config with Clock with Blocking with User.UserService with Logging

  type AppTask[A] = RIO[AppEnvironment, A]

  val logger: ULayer[Logging] = Logger.live
  val configLayer: ZLayer[Any, Throwable, Config] = logger >>> Config.live
  val transactor: ZLayer[Blocking, Throwable, DBTransactor] =
    logger ++ Blocking.any ++ configLayer >>> DBTransactor.live
  val userPersistence: ZLayer[Blocking, Throwable, UserService] =
    transactor >>> User.live
  val appLayers: ZLayer[
    Any with Blocking,
    Throwable,
    Logging with Config with UserService
  ] = logger ++ configLayer ++ userPersistence

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        api <- Config.httpServerConfig
        httpApp = Router[AppTask](
          "/users" -> Api(s"${api.path}/users").route
        ).orNotFound
        blockingEC <- blocking.blocking(ZIO.descriptor.map(_.executor.asEC))
        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask](blockingEC)
            .bindHttp(api.port.value, api.host.value)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, CatsExitCode]
            .drain
        }
      } yield server

    program
      .provideCustomLayer(appLayers)
      .tapError(err => zio.console.putStrLn(s"Execution failed with: $err"))
      .exitCode
  }
}
