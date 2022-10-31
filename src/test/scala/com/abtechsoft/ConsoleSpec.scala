package com.abtechsoft

import zio._
import zio.test.Assertion._
import zio.test._
object ConsoleSpec extends ZIOSpecDefault {
  val consoleSuite = suite("ConsoleTest")(
    test("One can test output of console") {
      for {
        _ <- TestConsole.feedLines("Jimmy", "37")
        _ <- Console.printLine("What is your name?")
        name <- Console.readLine
        _ <- Console.printLine("What is your age?")
        age <- Console.readLine.map(_.toInt)
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
  override def spec = consoleSuite
}
