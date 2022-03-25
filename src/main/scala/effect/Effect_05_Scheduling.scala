package effect
import zio.Schedule.Decision
import zio.console.putStrLn
import zio.duration.durationInt
import zio.{ExitCode, Schedule, Task, URIO}
import zio._
import zio.clock._
import zio.console._
import java.util.Random

object Effect_05_Scheduling extends zio.App {

  val withTime: Schedule[Any, Any, Long] = Schedule.spaced(1.second)
  val numberOfTimes: Schedule[Any, Any, Long] = Schedule.recurs(4)

  val scheduleWithTimeAndNumberOfAttempts: Schedule[Any, Any, (Long, Long)] =
    numberOfTimes && withTime

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    //API.makeRequest.schedule(numberOfTimes).exitCode
    //API.makeRequest.schedule(withTime).exitCode
    //API.makeRequest.schedule(scheduleWithTimeAndNumberOfAttempts).exitCode
    API.makeRequest.schedule(ScheduleUtil.scheduleWithLog).exitCode
  }

}

object API {
  def makeRequest: Task[String] =
    Task.effect {
      if (new Random().nextInt(10) < 5) "some value"
      else throw new Exception("hi")
    }
}

object ScheduleUtil {
  def scheduleWithLog[A]: Schedule[Console, Any, (Long, Long)] =
    Schedule.spaced(1.second) && Schedule
      .recurs(4)
      .onDecision({
        case Decision.Done(_) => putStrLn(s"done trying").orDie
        case Decision.Continue(attempt, _, _) =>
          putStrLn(s"attempt #$attempt").orDie
      })
}

object ScheduleApp extends scala.App {

  implicit val rt: Runtime[Clock with Console] = Runtime.default

  rt.unsafeRun(
    API.makeRequest
      .retry(ScheduleUtil.scheduleWithLog)
      .foldM(
        ex => putStrLn(s"Exception Failed: ${ex.getMessage}"),
        v => putStrLn(s"Succeeded with $v")
      )
  )
}
