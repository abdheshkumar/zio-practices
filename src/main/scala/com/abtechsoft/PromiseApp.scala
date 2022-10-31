package com.abtechsoft
import zio._

import java.io.IOException
object PromiseApp extends zio.ZIOAppDefault {

  val race: IO[String, Int] = for {
    p <- Promise.make[String, Int]
    _ <- p.succeed(13).fork
    _ <- p.complete(ZIO.succeed(2)).fork
    _ <- p.completeWith(ZIO.succeed(3)).fork
    _ <- p.done(Exit.succeed(4)).fork
    _ <- p.fail("5")
    _ <- p.failCause(Cause.die(new Error("6")))
    _ <- p.die(new Error("7"))
    _ <- p.interrupt.fork
    value <- p.await
  } yield value

  val promiseDelay: ZIO[Any, IOException, Int] = for {
    promise <- Promise.make[IOException, Int]
    c <- ZIO.console
    _ <-
      promise
        .completeWith({
          c.printLine("Running promise") *> ZIO.succeed(12)
        })
        .delay(1.second)
        .fork
    _ <- c.printLine("It should run before promise logic..")
    value <- promise.await // Resumes when forked fiber completes promise
    _ <- c.printLine("Wait for final result")
  } yield value

  val ioPromise2: UIO[Promise[Exception, Nothing]] =
    Promise.make[Exception, Nothing]

  val ioBooleanFailed: UIO[Boolean] =
    ioPromise2.flatMap(promise => promise.fail(new Exception("boom")))

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    promiseDelay

}
