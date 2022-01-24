package simplehttp4s

import cats.data.Kleisli
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import scala.concurrent.ExecutionContext.Implicits
import org.http4s.blaze.server.BlazeServerBuilder
object ioz extends Http4sDsl[Task]

object SimpleHttp4s extends App {
  import ioz._

  val helloService: Kleisli[Task, Request[Task], Response[Task]] = HttpRoutes
    .of[Task] {
      case GET -> Root => Ok("hello!")
    }
    .orNotFound

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    server.exitCode

  val server: ZIO[ZEnv, Throwable, Unit] = ZIO
    .runtime[ZEnv]
    .flatMap { implicit rts =>
      BlazeServerBuilder[Task]
        .withExecutionContext(Implicits.global)
        .bindHttp(8080, "localhost")
        .withHttpApp(helloService)
        .serve
        .compile
        .drain
    }

}
