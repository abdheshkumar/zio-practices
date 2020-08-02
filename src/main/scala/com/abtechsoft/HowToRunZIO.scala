package com.abtechsoft

import zio.console._
import zio.internal.Platform
import zio.{Runtime, Task, ZIO}
object MainWithZIOApp extends zio.App {
  val program: ZIO[Console, Nothing, Unit] = putStrLn("Hello, world")
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    //program.fold(_ => 1, _ => 0)
    program.as(0)
  }
}

object Main {
  val program: ZIO[Console, Nothing, Unit] = putStrLn("Hello, world")
  def main(args: Array[String]): Unit = {
    Runtime.default.unsafeRun(program)
  }
}

object FatalErrorMain extends zio.App {
  override val platform: Platform = Platform.default.withFatal(_ => true)
  def simpleName[A](c: Class[A]) = c.getSimpleName
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Task(simpleName(FatalErrorMain.getClass)).fold(_ => 1, _ => 0)
}
