package com.abtechsoft

import zio.console._
import zio.internal.Platform
import zio.{BootstrapRuntime, ExitCode, Runtime, Task, URIO, ZEnv, ZIO}
import zio.console._

import java.io.IOException

object MainWithoutZIOApp {
  val program: ZIO[Console, IOException, Unit] = putStrLn("Hello, world")

  def main(args: Array[String]): Unit = {
    Runtime.default.unsafeRun(program)
  }
}

object MainWithZIOApp extends zio.App {

  val program: ZIO[Console, IOException, Unit] = putStrLn("Hello, world")
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    program.exitCode
  }
}

object FatalErrorMain extends zio.App {
  override val platform: Platform = Platform.default.withFatal(_ => true)

  def simpleName[A](c: Class[A]) = c.getSimpleName

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    Task(simpleName(FatalErrorMain.getClass))
      .fold(_ => ExitCode.failure, _ => ExitCode.success)
}

object Main02 {
  val program: ZIO[Console, IOException, Unit] = putStrLn("Hello, world")
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
