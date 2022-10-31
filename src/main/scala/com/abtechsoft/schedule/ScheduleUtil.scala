package com.abtechsoft.schedule

import zio.Schedule.Decision
import zio._

import java.io.IOException
object ScheduleUtil extends zio.ZIOAppDefault {

  def schedule[A]: Schedule[Any, Any, Serializable] =
    Schedule
      .exponential(10.milliseconds)
      .whileOutput(_ < 60.seconds)
      .andThen(Schedule.fixed(60.seconds) && Schedule.recurs(100))
      .onDecision { (_, _, decision) =>
        decision match {
          case Decision.Done => Console.printLine(s"done trying").ignore
          case Decision.Continue(attempt) =>
            Console.printLine(s"attempt #$attempt").ignore
        }

      }

  def makeRequest: Task[String] =
    ZIO.attempt {
      throw new Exception(s"boom")
    }

  def program: ZIO[Any, IOException, Unit] =
    makeRequest
      .retry(schedule)
      .foldZIO(
        ex => Console.printLine(s"Exception Failed: $ex"),
        v => Console.printLine(s"Succeeded with $v")
      )

  override def run = program
}
