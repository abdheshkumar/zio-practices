package com.abtechsoft
import zio._
import zio.console
import zio.duration._
import zio.clock.Clock

object BackgroundProcessingFibre extends zio.App {

  val effect: RIO[Clock, Int] = for {
    _ <-
      ZIO.effectTotal(println("heartbeat")).delay(1.second).forever.forkDaemon
    _ <- ZIO.effectTotal(println("Doing some expensive work"))
  } yield 42

  val module = for {
    fiber <- effect.fork
    _ <- ZIO.effectTotal(println("Doing some other work")).delay(5.seconds)
    result <- fiber.join
  } yield result

  val program = for {
    fiber <- module.fork
    _ <-
      ZIO
        .effectTotal(println("Running another module entirely"))
        .delay(10.seconds)
    _ <- fiber.join
  } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    program.exitCode
  }

}
