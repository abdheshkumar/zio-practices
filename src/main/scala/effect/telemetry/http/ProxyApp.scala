package effect.telemetry.http

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import io.opentelemetry.context.propagation.{TextMapPropagator, TextMapSetter}
import zio.{UIO, ZIO}
import zio.telemetry.opentelemetry.Tracing.root
import zio.telemetry.opentelemetry.Tracing
import zhttp.http.{->, /, Http, HttpApp, Method, Response, Root}
import zio.json._
import cats.syntax.either._
import scala.collection.mutable

object ProxyApp {

  val propagator: TextMapPropagator = W3CTraceContextPropagator.getInstance()
  val setter: TextMapSetter[mutable.Map[String, String]] =
    (carrier, key, value) => carrier.update(key, value)

  val errorMapper: PartialFunction[Throwable, StatusCode] = {
    case ex => {
      println(ex)
      StatusCode.UNSET
    }
  }

  val routes: HttpApp[Client with Tracing, Throwable] = Http.collectM {
    case Method.GET -> Root / "statuses" =>
      root("/statuses", SpanKind.SERVER, errorMapper) {
        for {
          carrier <- UIO(mutable.Map[String, String]().empty)
          _ <- Tracing.setAttribute("http.method", "get")
          _ <- Tracing.addEvent("proxy-event")
          _ <- Tracing.inject(propagator, carrier, setter)
          res <-
            Client.status(carrier.toMap).map(s => Response.jsonString(s.toJson))
        } yield res
      }
    case request @Method.POST -> Root / "create" =>
      root("/create", SpanKind.SERVER, errorMapper) {
        for {
          user <- ZIO.fromEither(
            request.getBodyAsString
              .toRight(new Exception("user name missing"))
              .flatMap(v => v.fromJson[User].leftMap(new Exception(_)))
          )
          carrier <- UIO(mutable.Map[String, String]().empty)
          _ <- Tracing.setAttribute("http.method", "get")
          _ <- Tracing.addEvent("proxy-event")
          _ <- Tracing.inject(propagator, carrier, setter)
          res <-
            Client
              .create(carrier.toMap, user)
              .map(s => Response.jsonString(s.toJson))
        } yield res
      }
  }

}
