package com.abtechsoft

import zio.{BootstrapRuntime, Runtime, ZIO}
import zio.console._
object MainWithZIOApp extends zio.App {

  val program: ZIO[Console, Nothing, Unit] = putStrLn("Hello, world")
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    //program.fold(_ => 1, _ => 0)
    program.as(0)
  }
}

object Main01 {
  val program: ZIO[Console, Nothing, Unit] = putStrLn("Hello, world")

  def main(args: Array[String]): Unit = {
    Runtime.default.unsafeRun(program)
  }
}

object Main02 {
  val program: ZIO[Console, Nothing, Unit] = putStrLn("Hello, world")
  val runtime = new BootstrapRuntime {}
  def main(args: Array[String]): Unit = {
    runtime.unsafeRun(program)
  }
}

/*
ZIO is a zero-dependency library for asynchronous and concurrent programming in scala.
ZIO is a data type
trait ZIO[-R,+E,+A]
A description of a computation that required an environment R and may either fail with an E or succeed with an A
-A description, not a running computation
-Potentially asynchronous
-A better future
 * */
