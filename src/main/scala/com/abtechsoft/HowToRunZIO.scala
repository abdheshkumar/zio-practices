package com.abtechsoft

import zio.Console._
import zio.{Runtime, Unsafe, ZIO}

import java.io.IOException

object MainWithoutZIOApp {
  val program: ZIO[Any, IOException, Unit] = printLine("Hello, world")

  def main(args: Array[String]): Unit =
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.run(program)
    }
}

object MainWithZIOApp extends zio.ZIOAppDefault {

  val program: ZIO[Any, IOException, Unit] = printLine("Hello, world")
  override def run = {
    program.exitCode
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
