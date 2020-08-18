package com.abtechsoft
import zio._
import zio.console._
/*
Fibers are lightweight mechanism for concurrency
 */
object FiberApp extends zio.App {
  val program = for {
    fiberRef <- FiberRef.make[Int](0)
    _ <- fiberRef.set(10)
    _ <- putStrLn("Running...")
    v <- fiberRef.get
  } yield v == 10


  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.as(ExitCode.success)
}
