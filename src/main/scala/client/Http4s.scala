package client

import io.circe.Decoder
import org.http4s.Uri
import org.http4s.Uri.Path
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import zio._
import zio.interop.catz._

final case class Http4s(client: Client[Task])
    extends HttpClient.Service
    with Http4sClientDsl[Task] {
  protected val rootUrl = "http://www.transparencia.gov.br/api-de-dados/"
  def get[T](resource: String, parameters: Map[String, String])(implicit
      d: Decoder[T]
  ): Task[T] = {
    val uri = Uri(path = Path.unsafeFromString(rootUrl + resource))
      .withQueryParams(parameters)

    client.expect[T](uri.toString())
  }
}
