package effect.telemetry.http

import effect.telemetry.http.config.AppConfig
import sttp.client3._
import sttp.client3.ziojson._
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import zio.{Has, Task, ZIO, ZLayer}
import cats.syntax.either._
import zio.json._

object Client {
  type Backend = SttpBackend[Task, ZioStreams with WebSockets]

  trait Service {
    def status(headers: Map[String, String]): Task[Statuses]
    def create(headers: Map[String, String], user: User): Task[String]
  }

  def status(headers: Map[String, String]) =
    ZIO.accessM[Client](_.get.status(headers))

  def create(headers: Map[String, String], user: User) =
    ZIO.accessM[Client](_.get.create(headers, user))

  val up = Status.up("proxy")

  val live: ZLayer[Has[Backend] with Has[AppConfig], Nothing, Has[Service]] =
    ZLayer.fromServices[Backend, AppConfig, Service](
      (backend: Backend, conf: AppConfig) =>
        new Service {
          def status(headers: Map[String, String]): Task[Statuses] =
            backend
              .send(
                basicRequest
                  .get(conf.backend.host.withPath("status"))
                  .headers(headers)
                  .response(asJson[Status])
              )
              .map { response =>
                val status = response.body.getOrElse(Status.down("backend"))
                Statuses(List(status, up))
              }

          override def create(
              headers: Map[String, String],
              user: User
          ): Task[String] = {
            backend
              .send(
                basicRequest
                  .post(conf.backend.host.withPath("create"))
                  .headers(headers)
                  .body(user.toJson)
                  .response(asJson[String])
              )
              .flatMap { response =>
                val res = response.body
                Task.fromEither(res.leftMap(ex => new Exception(ex.getMessage)))
              }
          }
        }
    )
}
