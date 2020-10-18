package com.abtechsoft.schedule

import zio.console.putStrLn
import zio.{ExitCode, Schedule, Task, URIO}
import zio.Schedule.Decision
import zio.duration.durationInt
import java.util.Random
object ScheduleUtil extends zio.App {
  def schedule[A] =
    Schedule.spaced(1.second) && Schedule
      .recurs(4)
      .onDecision({
        case Decision.Done(_)                 => putStrLn(s"done trying")
        case Decision.Continue(attempt, _, _) => putStrLn(s"attempt #$attempt")
      })

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    API.makeRequest
      .retry(schedule)
      .foldM(
        ex => putStrLn(s"Exception Failed: $ex"),
        v => putStrLn(s"Succeeded with $v")
      )
      .exitCode
  }
}

object API {
  def makeRequest =
    Task.effect[String] {
      val i = new Random().nextInt(10)
      //println(s"Test...$i")
      //if (i > 7) "some value"
      //else
      throw new Exception(s"hiout ${i}")
    }
}
