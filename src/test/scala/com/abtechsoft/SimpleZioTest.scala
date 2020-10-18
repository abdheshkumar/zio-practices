package com.abtechsoft

import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test._
import zio.clock.nanoTime
import Assertion._
import zio.{Semaphore, console, random}
import zio.test.environment.{TestClock, TestConsole, TestRandom}
import zio.duration._
object SimpleZioTest extends DefaultRunnableSpec {
  val suite1 = suite("suite1")(testM("s1.t1") {
    assertM(nanoTime)(isGreaterThanEqualTo(0L))
  }, testM("s1.t2") { assertM(nanoTime)(isGreaterThanEqualTo(0L)) })

  val suite2 = suite("suite2")(
    testM("s2.t1") { assertM(nanoTime)(isGreaterThanEqualTo(0L)) },
    testM("s2.t2") { assertM(nanoTime)(isGreaterThanEqualTo(0L)) },
    testM("s2.t3") { assertM(nanoTime)(isGreaterThanEqualTo(0L)) }
  )

  val suite3 = suite("suite3")(
    testM("s3.t1") {
      assertM(nanoTime)(isGreaterThanEqualTo(0L))
    },
    testM("Use setSeed to generate stable values") {
      for {
        _ <- TestRandom.setSeed(27)
        r1 <- random.nextLong
        r2 <- random.nextLong
        r3 <- random.nextLong
      } yield
        assert(List(r1, r2, r3))(
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
    testM("One can test output of console") {
      for {
        _              <- TestConsole.feedLines("Jimmy", "37")
        _              <- console.putStrLn("What is your name?")
        name           <- console.getStrLn
        _              <- console.putStrLn("What is your age?")
        age            <- console.getStrLn.map(_.toInt)
        questionVector <- TestConsole.output
        q1             = questionVector(0)
        q2             = questionVector(1)
      } yield {
        assert(name)(equalTo("Jimmy")) &&
          assert(age)(equalTo(37)) &&
          assert(q1)(equalTo("What is your name?\n")) &&
          assert(q2)(equalTo("What is your age?\n"))
      }
    }
  )

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
    suite("All tests")(suite1, suite2, suite3,consoleSuite)
}
