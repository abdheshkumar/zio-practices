package simplehttps

package example

import org.http4s._
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio._
import zio.test._
import Assertion._

object SimpleHttp4sSpec extends ZIOSpecDefault {
  override def spec =
    suite("routes suite")(
      test("root request returns Ok") {
        val io: Task[Response[Task]] = simplehttp4s.SimpleHttp4s.helloService
          .run(Request[Task](Method.GET, uri"/"))
        io.map(s => assert(s.status)(equalTo(Status.Ok)))
      },
      test("unmapped request returns not found") {
        val io: Task[Response[Task]] = simplehttp4s.SimpleHttp4s.helloService
          .run(Request[Task](Method.GET, uri"/a"))
        io.map(s => assert(s.status)(equalTo(Status.NotFound)))
      },
      test("root request body returns hello!") {
        (for {
          request <- simplehttp4s.SimpleHttp4s.helloService.run(
            Request[Task](Method.GET, uri"/")
          )
          body <-
            request.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
        } yield assert(body)(equalTo("hello!")))
      }
    )
}
