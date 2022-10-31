package com.abtechsoft
import zio._

object BackgroundProcessingFibre extends zio.ZIOAppDefault {

  val effect: ZIO[Any, Throwable, Int] = for {
    _ <- ZIO.attempt(println("heartbeat")).delay(1.second).forever.forkDaemon
    _ <- ZIO.attempt(println("Doing some expensive work"))
  } yield 42

  val module = for {
    fiber <- effect.fork
    _ <- ZIO.attempt(println("Doing some other work")).delay(5.seconds)
    result <- fiber.join
  } yield result

  val program = for {
    fiber <- module.fork
    _ <-
      ZIO
        .attempt(println("Running another module entirely"))
        .delay(10.seconds)
    _ <- fiber.join
  } yield ()

  override def run = {
    program
  }

}
