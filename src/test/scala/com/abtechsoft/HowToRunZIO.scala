package com.abtechsoft

import zio.ZIO
import zio.console._
import zio.Runtime
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
