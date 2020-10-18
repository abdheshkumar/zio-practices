import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import pureconfig.ConfigSource

import scala.concurrent.duration.FiniteDuration

object RefinedApp extends App {

  import pureconfig.generic.auto._
  import eu.timepit.refined._
  import eu.timepit.refined.api.Refined

  import eu.timepit.refined.numeric._
  import eu.timepit.refined.string._
  import eu.timepit.refined.collection._
  import eu.timepit.refined.boolean._
  import eu.timepit.refined.char._
  import shapeless.{::, HNil}

  import eu.timepit.refined.pureconfig._ //pureconfig

  //suppose the value is known at compile-time
  import eu.timepit.refined.auto._
  val i1: Int Refined Positive = 5
  println(i1 + i1)
  //val i1: Int Refined Positive = 5
  //val i2: Int Refined Positive = -5
  //Predicate failed: (-5 > 0).
  //  val i2: Int Refined Positive = -5

  //suppose the value is not known at compile-time
  val x = 42
  val xv: Either[String, Refined[Int, Positive]] = refineV[Positive](x)
  //xv: Either[String, Int Refined Positive] = Right(42)
  println(xv)

  val a: Int Refined Greater[W.`5`.T] = 10
  refineMV[NonEmpty]("Hello")

  type ZeroToOne = Not[Less[W.`0.0`.T]] And Not[Greater[W.`1.0`.T]]
  //val r: Refined[Double, ZeroToOne] = refineMV[ZeroToOne](1.8)
  refineMV[AnyOf[Digit :: Letter :: Whitespace :: HNil]]('F')
  val u1: String Refined Url = "http://example.com"
  type Age = Int Refined Interval.ClosedOpen[W.`7`.T, W.`77`.T]
  type Id = Int Refined Interval.Open[100, 500]
  type PostCode = String Refined MatchesRegex["^[0-9]{5}$"]
  type PhoneNumber =
    String Refined MatchesRegex["^((00|\\+)33|0)([0-9]{5}|[0-9]{9})$"]

  def tag(asset: String, version: String): String = ???
  //tag("v0.1", "image.png") //Is this right?

  //case class Version(value: String)
  case class Asset(name: String)

  def tag(asset: Asset, version: Version) = ???
  //Now it’s no longer possible to switch the arguments. However it’s still possible to build a wrong version:

  val version = Version.fromString("image.png") //It is still a problem

  //Lets add validation
  sealed abstract case class Version(value: String)
  object Version {
    val version = """v\d+(\.\d+(\.\d+)?)?""".r
    def fromString(value: String): Option[Version] =
      value match {
        case version(_*) => Some(new Version(value) {})
        case _           => None
      }
  }

  // let's pretend this is known at runtime
  val input: String = "v1.0.3"

  type ValidVersion = MatchesRegex[W.`"""v\\d+(\\.\\d+(\\.\\d+)?)?"""`.T]
  type VersionT = String Refined ValidVersion
  val version1: Either[String, String Refined ValidVersion] =
    refineV[ValidVersion](input)
  //We can then define our version type as

  import eu.timepit.refined.api.Refined
  import eu.timepit.refined.collection.NonEmpty
  import eu.timepit.refined.string.StartsWith
  import eu.timepit.refined.W
  import eu.timepit.refined.auto._

  object refinements {
    type Name = Refined[String, NonEmpty]
    type TwitterHandle = String Refined StartsWith[W.`"@"`.T]
    final case class Developer(name: Name, twitterHandle: TwitterHandle)
  }

  sealed trait Status
  sealed trait Red extends Status
  sealed trait Orange extends Status
  sealed trait Green extends Status

  class TrafficSignal[T <: Status] {
    private def to[U <: Status]: TrafficSignal[U] =
      this.asInstanceOf[TrafficSignal[U]]
    def stop(implicit ev: T =:= Orange): TrafficSignal[Red] = to[Red]
    def start(implicit ev: T =:= Red): TrafficSignal[Orange] = to[Orange]
    def go(implicit ev: T =:= Orange): TrafficSignal[Green] = to[Green]
    def slow(implicit ev: T =:= Green): TrafficSignal[Orange] = to[Orange]
  }
  val signal = new TrafficSignal[Red]
  //signal.stop //Compilation Error: Can not prove that Red =:= Orange
  signal.start.go.slow //Compilation Successful

  refinements.Developer("Abdhesh", "@abdhesh_rkg")
  //And swapping arguments fails to compile
  //refinements.Developer("@abdhesh_rkg", "Abdhesh") // Does not compile, sweet

  //Predicate failed: "Abdhesh".startsWith("@").
  //  refinements.Developer("@abdhesh_rkg", "Abdhesh") // Does not compile, sweet

  case class HttpConfig(
      host: String Refined NonEmpty,
      port: Int Refined Positive,
      timeout: FiniteDuration
  )
  case class Settings(
      name: String Refined MatchesRegex[W.`"[a-z0-9_-]+"`.T],
      http: HttpConfig
  )
  val config = ConfigSource
    .string(
      s"""name = "test-data"
     |http {
     |    host = "localhost"
     |    host = $${?HTTP_ADDR}
     |    port = 80
     |    port = $${?HTTP_PORT}
     |    timeout = 30 s
     |  }""".stripMargin
    )
    .load[
      Settings
    ] //Right(Settings(test-data,HttpConfig(localhost,80,30 seconds)))

  val invalidConfig = ConfigSource
    .string(
      s"""name = "test-data"
       |http {
       |    host = ""
       |    host = $${?HTTP_ADDR}
       |    port = 80sd
       |    port = $${?HTTP_PORT}
       |    timeout = 30 s
       |  }""".stripMargin
    )
    .load[Settings]
  //Left(ConfigReaderFailures(
  // ConvertFailure(CannotConvert("",Refined[String,Not[Empty]],Predicate isEmpty() did not fail.),Some(ConfigOrigin(String)),http.host),
  // ConvertFailure(WrongType(STRING,Set(NUMBER)),Some(ConfigOrigin(String)),http.port)))

  println(invalidConfig)

}
