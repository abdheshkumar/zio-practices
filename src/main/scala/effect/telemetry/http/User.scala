package effect.telemetry.http

case class User(name: String)

object User {
  import zio.json._
  implicit val decoder: JsonDecoder[User] =
    DeriveJsonDecoder.gen[User]
  implicit val encoder: JsonEncoder[User] =
    DeriveJsonEncoder.gen[User]
}
