package com.abtechsoft

import zio.Clock.nanoTime
import zio.{Console, Random}
import zio.test.Assertion._
import zio.test._
object SimpleZioTest extends ZIOSpecDefault {
  val suite1 = suite("suite1")(
    test("s1.t1") {
      assertZIO(nanoTime)(isGreaterThanEqualTo(0L))
    },
    test("s1.t2") { assertZIO(nanoTime)(isGreaterThanEqualTo(0L)) }
  )

  val suite2 = suite("suite2")(
    test("s2.t1") { assertZIO(nanoTime)(isGreaterThanEqualTo(0L)) },
    test("s2.t2") { assertZIO(nanoTime)(isGreaterThanEqualTo(0L)) },
    test("s2.t3") { assertZIO(nanoTime)(isGreaterThanEqualTo(0L)) }
  )

  val suite3 = suite("suite3")(
    test("s3.t1") {
      assertZIO(nanoTime)(isGreaterThanEqualTo(0L))
    },
    test("Use setSeed to generate stable values") {
      for {
        _ <- TestRandom.setSeed(27)
        r1 <- Random.nextLong
        r2 <- Random.nextLong
        r3 <- Random.nextLong
      } yield assert(List(r1, r2, r3))(
        equalTo(
          List[Long](
            -4947896108136290151L,
            -5264020926839611059L,
            -9135922664019402287L
          )
        )
      )
    }
  )

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

  override def spec =
    suite("All tests")(suite1, suite2, suite3, consoleSuite)
}
