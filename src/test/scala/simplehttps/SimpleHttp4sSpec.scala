package simplehttps

package example

import org.http4s._
import zio._
import zio.interop.catz._
import zio._
import zio.test._
import Assertion._

object SimpleHttp4sSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("routes suite")(
      testM("root request returns Ok") {
        val io: Task[Response[Task]] = simplehttp4s.SimpleHttp4s.helloService
          .run(Request[Task](Method.GET, Uri.uri("/")))
        io.map(s => assert(s.status)(equalTo(Status.Ok)))
      },
      testM("unmapped request returns not found") {
        val io: Task[Response[Task]] = simplehttp4s.SimpleHttp4s.helloService
          .run(Request[Task](Method.GET, Uri.uri("/a")))
        io.map(s => assert(s.status)(equalTo(Status.NotFound)))
      },
      testM("root request body returns hello!") {
        (for {
          request <- simplehttp4s.SimpleHttp4s.helloService.run(
            Request[Task](Method.GET, Uri.uri("/"))
          )
          body <-
            request.body.compile.toVector.map(x => x.map(_.toChar).mkString(""))
        } yield assert(body)(equalTo("hello!")))
      }
    )
}
