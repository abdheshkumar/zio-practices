package effect.telemetry.http

import effect.telemetry.http.config.AppConfig
import zio.{&, Task, ZIO, ZLayer}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.json._
import zhttp.http.{Body, Headers, Method}
object MiddlewareClient {

  trait Service {
    def status(headers: Map[String, String]): Task[Statuses]
    def create(headers: Map[String, String], user: User): Task[String]
  }

  def status(headers: Map[String, String]) =
    ZIO.serviceWithZIO[Service](_.status(headers))

  def create(headers: Map[String, String], user: User) =
    ZIO.serviceWithZIO[Service](_.create(headers, user))

  val up = Status.up("proxy")

  val live =
    ZLayer.fromFunction(
      (el: EventLoopGroup, cf: ChannelFactory, conf: AppConfig) =>
        new Service {
          def status(headers: Map[String, String]): Task[Statuses] = {
            Client
              .request(
                s"${conf.backend.host}/status",
                headers = Headers(headers)
              )
              .flatMap { response =>
                response.body.asString
                  .map(_.fromJson[Status].getOrElse(Status.down("backend")))
                  .map(status => Statuses(List(status, up)))

              }
              .provide(ZLayer.succeed(el), ZLayer.succeed(cf))
          }

          override def create(
              headers: Map[String, String],
              user: User
          ): Task[String] = {
            Client
              .request(
                s"${conf.backend.host}/create",
                method = Method.POST,
                headers = Headers(headers),
                content = Body.fromString(user.toJson)
              )
              .flatMap { response =>
                response.body.asString
              }
          }.provide(ZLayer.succeed(el), ZLayer.succeed(cf))
        }
    )
}
