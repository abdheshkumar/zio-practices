package client
import zio._
import io.circe._
import org.http4s.client.Client

object HttpClient {
  type HttpClient = Has[Service]

  trait Service {
    def get[T](uri: String, parameters: Map[String, String])(implicit
        d: Decoder[T]
    ): Task[T]
  }

  def http4s: URLayer[Has[Client[Task]], HttpClient] =
    ZLayer.fromService[Client[Task], Service] { http4sClient =>
      Http4s(http4sClient)
    }

  def get[T](resource: String, id: Long)(implicit
      d: Decoder[T]
  ): RIO[HttpClient, T] =
    RIO.accessM[HttpClient](_.get.get[T](s"$resource/$id", Map()))

  def get[T](resource: String, parameters: Map[String, String] = Map())(implicit
      d: Decoder[T]
  ): RIO[HttpClient, List[T]] =
    RIO.accessM[HttpClient](_.get.get[List[T]](resource, parameters))

}
