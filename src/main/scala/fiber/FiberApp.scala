package fiber

import cats.effect.{ExitCode, IO, IOApp}

object FiberApp extends IOApp {

  def io(i: Int): IO[Unit] =
    IO(println(s"Hi from $i!")) // short for IO.delay or IO.apply

  val program1 = for {
    _ <- io(1)
    _ <- io(2)
  } yield ExitCode.Success

  def io2(i: Int): IO[Unit] = IO({
    Thread.sleep(3000)
    println(s"Hi from $i!")
  })

  val program2 = for {
    fiber <- io2(1).start
    _ <- io2(2)
    _ <- fiber.join
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program2
}
