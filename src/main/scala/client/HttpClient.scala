package client
import zio._
import io.circe._
import org.http4s.client.Client

object HttpClient {

  trait Service {
    def get[T](uri: String, parameters: Map[String, String])(implicit
        d: Decoder[T]
    ): Task[T]
  }

  def http4s: URLayer[Client[Task], Service] =
    ZLayer.fromZIO {
      for {
        http4sClient <- ZIO.service[Client[Task]]
      } yield Http4s(http4sClient)
    }

  def get[T](resource: String, id: Long)(implicit
      d: Decoder[T]
  ): ZIO[Service, Nothing, Task[T]] =
    ZIO.serviceWith[Service](_.get[T](s"$resource/$id", Map()))

  def get[T](resource: String, parameters: Map[String, String] = Map())(implicit
      d: Decoder[T]
  ): ZIO[Service, Nothing, Task[List[T]]] =
    ZIO.serviceWith[Service](_.get[List[T]](resource, parameters))

}
