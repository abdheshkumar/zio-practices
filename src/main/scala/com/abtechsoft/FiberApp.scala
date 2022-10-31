package com.abtechsoft
import zio._
import zio.Console._
/*
Fibers are lightweight mechanism for concurrency
 */
object FiberApp extends zio.ZIOAppDefault {
  val program = for {
    fiberRef <- FiberRef.make[Int](0)
    _ <- fiberRef.set(10)
    _ <- printLine("Running...")
    v <- fiberRef.get
  } yield v == 10

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}
