package com.abtechsoft

import zio.test.{DefaultRunnableSpec, ZSpec}
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestConsole
object ConsoleSpec extends DefaultRunnableSpec {
  val consoleSuite = suite("ConsoleTest")(
    testM("One can test output of console") {
      for {
        _ <- TestConsole.feedLines("Jimmy", "37")
        _ <- console.putStrLn("What is your name?")
        name <- console.getStrLn
        _ <- console.putStrLn("What is your age?")
        age <- console.getStrLn.map(_.toInt)
        questionVector <- TestConsole.output
        q1 = questionVector(0)
        q2 = questionVector(1)
      } yield {
        assert(name)(equalTo("Jimmy")) &&
        assert(age)(equalTo(37)) &&
        assert(q1)(equalTo("What is your name?\n")) &&
        assert(q2)(equalTo("What is your age?\n"))
      }
    }
  )
  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    consoleSuite
}
