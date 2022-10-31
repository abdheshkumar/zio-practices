package persistence

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import persistence.api.Api
import persistence.config.AppConfig
import persistence.dbtransactor.DBTransactor
import zio.{Console, _}
import zio.interop.catz._

object MainApp extends zio.ZIOAppDefault {
  type AppEnvironment = AppConfig & User.UserService

  type AppTask[A] = RIO[AppEnvironment, A]

  override def run = {
    val program: ZIO[AppEnvironment with AppConfig, Throwable, Unit] =
      for {
        api <- ZIO.service[AppConfig]
        httpApp = Router[AppTask](
          "/users" -> Api(s"${api.httpServer.path}/users").route
        ).orNotFound
        blockingEC <- ZIO.executor
        server <- ZIO.runtime[AppEnvironment].flatMap { _ =>
          BlazeServerBuilder[AppTask]
            .withExecutionContext(blockingEC.asExecutionContext)
            .bindHttp(api.httpServer.port.value, api.httpServer.host.value)
            .withHttpApp(CORS.policy.withAllowOriginAll(httpApp))
            .serve
            .compile
            .drain
        }
      } yield server

    program
      .provide(Config.live, DBTransactor.live, User.live)
      .tapError(err => Console.printLine(s"Execution failed with: $err"))
      .exitCode
  }
}
