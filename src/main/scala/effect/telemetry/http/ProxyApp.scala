package effect.telemetry.http

import cats.syntax.either._
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.api.trace.{SpanKind, StatusCode}
import io.opentelemetry.context.propagation.{TextMapPropagator, TextMapSetter}
import zhttp.http._
import zio.ZIO
import zio.json._
import zio.telemetry.opentelemetry.Tracing
import zio.telemetry.opentelemetry.Tracing.root

import scala.collection.mutable

object ProxyApp {

  val propagator: TextMapPropagator = W3CTraceContextPropagator.getInstance()
  val setter: TextMapSetter[mutable.Map[String, String]] =
    (carrier, key, value) => carrier.update(key, value)

  val errorMapper: PartialFunction[Throwable, StatusCode] = {
    case ex =>
      println(ex)
      StatusCode.UNSET
  }

  val routes: HttpApp[MiddlewareClient.Service with Tracing, Throwable] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "statuses" =>
        root("/statuses", SpanKind.SERVER, errorMapper) {
          for {
            carrier <- ZIO.succeed(mutable.Map[String, String]().empty)
            _ <- Tracing.setAttribute("http.method", "get")
            _ <- Tracing.addEvent("proxy-event")
            _ <- Tracing.inject(propagator, carrier, setter)
            res <- MiddlewareClient.status(carrier.toMap).map(s => s.toJson)
          } yield Response.text(res)
        }
      case request @ Method.POST -> !! / "create" =>
        root("/create", SpanKind.SERVER, errorMapper) {
          for {
            user <-
              request.body.asString
                .mapError(ex => new Exception(s"user name missing: $ex"))
                .flatMap(v =>
                  ZIO.fromEither(v.fromJson[User].leftMap(new Exception(_)))
                )

            carrier <- ZIO.succeed(mutable.Map[String, String]().empty)
            _ <- Tracing.setAttribute("http.method", "get")
            _ <- Tracing.addEvent("proxy-event")
            _ <- Tracing.inject(propagator, carrier, setter)
            res <-
              MiddlewareClient
                .create(carrier.toMap, user)
                .map(s => s.toJson)
          } yield Response.json(res)
        }
    }

}
