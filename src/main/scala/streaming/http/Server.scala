package streaming.http

import cats.data.Kleisli
import cats.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.{AutoSlash, GZip}
import org.http4s.{HttpRoutes, Request, Response}
import streaming.environment.config.Configuration.AppConfig
import streaming.environment.repository.{CitiesRepository, DbTransactor}
import streaming.http.endpoints.{CitiesEndpoint, HealthEndpoint}
import zio.interop.catz._
import zio.{RIO, ZIO}

import scala.concurrent.ExecutionContext

object Server {
  type AppEnvironment = DbTransactor with CitiesRepository with AppConfig
  type AppTask[A] = RIO[AppEnvironment, A]
  type ServerRoutes = Kleisli[AppTask, Request[AppTask], Response[AppTask]]

  def runServer: ZIO[AppEnvironment, Throwable, Unit] =
    ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      ZIO.service[AppConfig].flatMap { appConfig =>
        BlazeServerBuilder[AppTask]
          .withExecutionContext(ExecutionContext.global)
          .bindHttp(appConfig.httpServer.port, appConfig.httpServer.host)
          .withHttpApp(createRoutes(appConfig.httpServer.path))
          .serve
          .compile
          .drain
      }
    }

  def createRoutes(basePath: String): ServerRoutes = {
    val citiesRoutes = new CitiesEndpoint[AppEnvironment].routes
    val healthRoutes = new HealthEndpoint[AppEnvironment].routes
    val routes = citiesRoutes <+> healthRoutes

    Router[AppTask](basePath -> middleware(routes)).orNotFound
  }

  private val middleware: HttpRoutes[AppTask] => HttpRoutes[AppTask] = {
    { http: HttpRoutes[AppTask] =>
      AutoSlash(http)
    }.andThen { http: HttpRoutes[AppTask] =>
      GZip(http)
    }
  }
}
