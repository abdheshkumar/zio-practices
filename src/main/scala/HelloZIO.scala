import zio.{
  Console,
  ExitCode,
  IO,
  Scope,
  UIO,
  URIO,
  ZIO,
  ZIOAppArgs,
  ZIOAppDefault
}

import java.io.IOException
import scala.util.Random
object HelloZIO extends ZIOAppDefault {

  val effectOnce: UIO[ZIO[Any, Nothing, Int]] =
    ZIO.succeed(Random.nextInt()).memoize
  def p1: ZIO[Any, IOException, Unit] =
    for {
      e <- effectOnce
      ee <- e
      _ <- Console.printLine(ee.toString)
    } yield ()

  def p2: ZIO[Any, IOException, Unit] =
    for {
      e <- effectOnce
      ee <- e
      _ <- Console.printLine(ee.toString)
    } yield ()

  def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- p1
      _ <- p2
    } yield ()
}
