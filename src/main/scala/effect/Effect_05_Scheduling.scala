package effect
import zio.Schedule.Decision
import zio.{Schedule, Task, _}

import java.util.Random

object Effect_05_Scheduling extends zio.ZIOAppDefault {

  val withTime: Schedule[Any, Any, Long] = Schedule.spaced(1.second)
  val numberOfTimes: Schedule[Any, Any, Long] = Schedule.recurs(4)

  val scheduleWithTimeAndNumberOfAttempts: Schedule[Any, Any, (Long, Long)] =
    numberOfTimes && withTime

  override def run = {
    //API.makeRequest.schedule(numberOfTimes).exitCode
    //API.makeRequest.schedule(withTime).exitCode
    //API.makeRequest.schedule(scheduleWithTimeAndNumberOfAttempts).exitCode
    API.makeRequest.schedule(ScheduleUtil.scheduleWithLog).exitCode
  }

}

object API {
  def makeRequest: Task[String] =
    ZIO.attempt {
      if (new Random().nextInt(10) < 5) "some value"
      else throw new Exception("hi")
    }
}

object ScheduleUtil {
  def scheduleWithLog[A]: Schedule[Any, Any, (Long, Long)] =
    Schedule.spaced(1.second) && Schedule
      .recurs(4)
      .onDecision { (v1, v2, d) =>
        d match {
          case Decision.Done => Console.printLine(s"done trying").orDie
          case Decision.Continue(attempt) =>
            Console.printLine(s"attempt #$attempt").orDie
        }

      }
}

object ScheduleApp extends scala.App {

  implicit val rt: Runtime[Any] = Runtime.default
  Unsafe.unsafe { implicit unsafe =>
    rt.unsafe
      .run(
        API.makeRequest
          .retry(ScheduleUtil.scheduleWithLog)
          .foldZIO(
            ex => Console.printLine(s"Exception Failed: ${ex.getMessage}"),
            v => Console.printLine(s"Succeeded with $v")
          )
      )
      .getOrThrowFiberFailure()
  }
}
