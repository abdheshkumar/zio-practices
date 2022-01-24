package effect

import cats.Monad
import cats.effect.IOApp

object Effect_03 extends zio.App {
  import zio._
  import zio.console.Console
  import zio.console
  import java.io.IOException

  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    Program.getUserInput("Please enter your name").exitCode

  object Program {
    def getUserInput(message: String): ZIO[Console, IOException, String] = {
      for {
        _ <- console.putStrLn(message)
        input <- console.getStrLn
        _ <- console.putStrLn(s"Your have entered: $input")
      } yield input
    }
  }
}

object Cats_Effect_03 extends IOApp {
  import cats.effect._

  def run(args: List[String]): IO[ExitCode] =
    Program
      .getUserInput[IO]("Please enter your name")
      .map(_ => ExitCode.Success)

  object Program {

    def getUserInput[F[_]](
        message: String
    )(implicit console: Console[F], M: Monad[F]): F[String] = {
      //Below imports need for for-comprehension
      import cats.syntax.functor._
      import cats.syntax.flatMap._
      //---------
      for {
        _ <- console.putStrLn(message)
        input <- console.getStrLn
        _ <- console.putStrLn(s"Your have entered: $input")
      } yield input
    }
  }

  trait Console[F[_]] {
    def putStrLn(message: String): F[Unit]
    def getStrLn: F[String]
  }

  object Console {
    implicit val console: Console[IO] = new Console[IO] {
      override def putStrLn(message: String): IO[Unit] = IO(println(message))
      override def getStrLn: IO[String] = IO(scala.io.StdIn.readLine)
    }
  }

}
