package com.abtechsoft.schedule

import zio.console.{Console, putStrLn}
import zio.{ExitCode, Schedule, Task, URIO, ZIO}
import zio.Schedule.Decision
import zio.clock.Clock
import zio.duration.durationInt

import java.io.IOException
import java.util.Random
import zio.duration._

object ScheduleUtil extends zio.App {

  def schedule[A]: Schedule[Console, Any, Serializable] =
    Schedule
      .exponential(10.milliseconds)
      .whileOutput(_ < 60.seconds)
      .andThen(Schedule.fixed(60.seconds) && Schedule.recurs(100))
      .onDecision {
        case Decision.Done(_) => putStrLn(s"done trying").ignore
        case Decision.Continue(attempt, _, _) =>
          putStrLn(s"attempt #$attempt").ignore
      }

  def makeRequest: Task[String] =
    Task.effect[String] {
      throw new Exception(s"boom")
    }

  def program: ZIO[Console with Clock, IOException, Unit] =
    makeRequest
      .retry(schedule)
      .foldM(
        ex => putStrLn(s"Exception Failed: $ex"),
        v => putStrLn(s"Succeeded with $v")
      )

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    program.exitCode
  }
}
