package effect

import cats.Monad
import effect.Cats_Effect_03.Testing.TestIO

//https://guillaumebogard.dev/posts/functional-error-handling/
//https://github.com/gbogard/cats-mtl-talk
//https://github.com/svroonland/rezilience
object Effect_03 extends zio.App {
  import zio.console.Console
  import zio.{console, _}

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

object Cats_Effect_03 {

  def main(args: Array[String]): Unit = {

    val programIO = Program.getUserInput[IO]
    val programTest: TestIO[String] = Program.getUserInput[TestIO]
    println("IO: " + programIO.unsafeInterpret())
    val (result, output) = programTest.run(
      Testing
        .TestData(input = List("1-value", "2-value"), output = List.empty)
    )
    println(s"TestIO: Output: $output, Program's Test data: $result")
  }

  object Program {
    //Below imports need for for-comprehension
    import cats.syntax.flatMap._
    import cats.syntax.functor._

    def getUserInput[F[_]: Console: Monad]: F[String] = {

      for {
        _ <- Console[F].putStrLn("Good morning, what's your name?")
        name <- Console[F].getStrLn
        _ <- Console[F].putStrLn(s"Great to meet you, $name")
      } yield name
    }

    def getUserInputV2[F[_]](implicit
        console: Console[F],
        M: Monad[F]
    ): F[String] = {
      for {
        _ <- console.putStrLn("Good morning, what's your name?")
        name <- console.getStrLn
        _ <- console.putStrLn(s"Great to meet you, $name")
      } yield name
    }

  }

  class IO[+A](val unsafeInterpret: () => A) { s =>
    def map[B](f: A => B) = flatMap(f.andThen(IO.effect(_)))
    def flatMap[B](f: A => IO[B]): IO[B] =
      IO.effect(f(s.unsafeInterpret()).unsafeInterpret())
  }
  object IO {
    def effect[A](eff: => A) = new IO(() => eff)
  }

  implicit val ioMonad: Monad[IO] = new Monad[IO] {
    override def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => IO[Either[A, B]]): IO[B] =
      f(a).flatMap {
        case Left(value)  => tailRecM(value)(f)
        case Right(value) => IO.effect(value)
      }

    override def pure[A](x: A): IO[A] = IO.effect(x)
  }

  trait Console[F[_]] {
    def putStrLn(line: String): F[Unit]

    val getStrLn: F[String]
  }
  object Console {
    def apply[F[_]](implicit F: Console[F]): Console[F] = F
  }

  implicit val ConsoleIO: Console[IO] = new Console[IO] {
    def putStrLn(line: String): IO[Unit] =
      IO.effect(println(line))

    val getStrLn: IO[String] =
      IO.effect(scala.io.StdIn.readLine())
  }

  implicit val ConsoleTestIO: Console[TestIO] = new Console[TestIO] {
    def putStrLn(line: String): TestIO[Unit] =
      TestIO(d => (d.copy(output = line :: d.output), ()))

    val getStrLn: TestIO[String] =
      TestIO(d => (d.copy(input = d.input.drop(1)), d.input.head))
  }

  implicit val TestIOMonad: Monad[TestIO] = new Monad[TestIO] {
    override def flatMap[A, B](fa: TestIO[A])(f: A => TestIO[B]): TestIO[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => TestIO[Either[A, B]]): TestIO[B] =
      f(a).flatMap {
        case Left(value)  => tailRecM(value)(f)
        case Right(value) => TestIO.value(value)
      }

    override def pure[A](x: A): TestIO[A] = TestIO.value(x)
  }

  object Testing {
    case class TestData(input: List[String], output: List[String])
    case class TestIO[A](run: TestData => (TestData, A)) { s =>
      def map[B](f: A => B): TestIO[B] = flatMap(a => TestIO.value(f(a)))
      def flatMap[B](f: A => TestIO[B]): TestIO[B] =
        TestIO(d => (s run d) match { case (d, a) => f(a) run d })
    }
    object TestIO {
      def value[A](a: => A): TestIO[A] = TestIO(d => (d, a))
    }
  }
}
