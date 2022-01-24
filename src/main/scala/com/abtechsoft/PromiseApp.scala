package com.abtechsoft
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt

import java.io.IOException
object PromiseApp extends App {
  val race: IO[String, Int] = for {
    p <- Promise.make[String, Int]
    _ <- p.succeed(13).fork
    _ <- p.complete(ZIO.succeed(2)).fork
    _ <- p.completeWith(ZIO.succeed(3)).fork
    _ <- p.done(Exit.succeed(4)).fork
    _ <- p.fail("5")
    _ <- p.halt(Cause.die(new Error("6")))
    _ <- p.die(new Error("7"))
    _ <- p.interrupt.fork
    value <- p.await
  } yield value

  val promiseDelay
      : ZIO[Any with Clock with Has[Console.Service], IOException, Int] = for {
    promise <- Promise.make[IOException, Int]
    c <- ZIO.service[zio.console.Console.Service]
    _ <-
      promise
        .completeWith({
          c.putStrLn("Running promise") *> IO.succeed(12)
        })
        .delay(1.second)
        .fork
    _ <- c.putStrLn("It should run before promise logic..")
    value <- promise.await // Resumes when forked fiber completes promise
    _ <- c.putStrLn("Wait for final result")
  } yield value

  val ioPromise2: UIO[Promise[Exception, Nothing]] =
    Promise.make[Exception, Nothing]
  val ioBooleanFailed: UIO[Boolean] =
    ioPromise2.flatMap(promise => promise.fail(new Exception("boom")))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    //ioBooleanFailed.map(println).exitCode
    promiseDelay.exitCode
  }
}
