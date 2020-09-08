package com.abtechsoft
import zio._
object PromiseApp extends App {
  val race: IO[String, Int] = for {
    p     <- Promise.make[String, Int]
    _     <- p.succeed(13).fork
    _     <- p.complete(ZIO.succeed(2)).fork
    _     <- p.completeWith(ZIO.succeed(3)).fork
    _     <- p.done(Exit.succeed(4)).fork
    _     <- p.fail("5")
    _     <- p.halt(Cause.die(new Error("6")))
    _     <- p.die(new Error("7"))
    _     <- p.interrupt.fork
    value <- p.await
  } yield value

  val ioPromise2: UIO[Promise[Exception, Nothing]] = Promise.make[Exception, Nothing]
  val ioBooleanFailed: UIO[Boolean] = ioPromise2.flatMap(promise => promise.fail(new Exception("boom")))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    ioBooleanFailed.map(println).exitCode
  }
}
