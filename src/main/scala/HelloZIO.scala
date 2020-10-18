import zio.console.Console
import zio.{ExitCode, IO, UIO, URIO, ZIO, console}

import scala.util.Random

import cats.Apply
object HelloZIO extends zio.App {

  val effectOnce: UIO[ZIO[Any, Throwable, Int]] = IO(Random.nextInt()).memoize
  def p1: ZIO[Console, Throwable, Unit] =
    for {
      e <- effectOnce
      ee <- e
      _ <- console.putStrLn(ee.toString)
    } yield ()

  def p2: ZIO[Console, Throwable, Unit] =
    for {
      e <- effectOnce
      ee <- e
      _ <- console.putStrLn(ee.toString)
    } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    (
      for {
        _ <- p1
        _ <- p2
      } yield ()
    ).exitCode
  }
}
