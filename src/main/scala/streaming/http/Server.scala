package streaming.http

import streaming.environment.Environments.AppEnvironment
import streaming.environment.config.Configuration.HttpServerConfig
import streaming.http.endpoints.{CitiesEndpoint, HealthEndpoint}
import cats.data.Kleisli
import cats.implicits._
import org.http4s.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.{AutoSlash, GZip}
import org.http4s.{HttpRoutes, Request, Response}
import zio.interop.catz._
import zio.{RIO, ZIO}

object Server {
  type AppTask[A] = RIO[AppEnvironment, A]
  type ServerRoutes = Kleisli[AppTask, Request[AppTask], Response[AppTask]]

  def runServer =
    ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
      val cfg = rts.environment.get[HttpServerConfig]
      val ec = rts.platform.executor.asEC

      BlazeServerBuilder[AppTask].withExecutionContext(ec)
        .bindHttp(cfg.port, cfg.host)
        .withHttpApp(createRoutes(cfg.path))
        .serve
        .compile
        .drain
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
