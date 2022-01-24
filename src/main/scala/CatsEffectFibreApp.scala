import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.parallel._

import scala.concurrent.duration.{FiniteDuration, _}

object CatsEffectFibreApp extends IOApp {
  def put(s: String)(count: Int): IO[Unit] = IO(println(s + count))

  def periodic[A](n: Int, t: FiniteDuration, action: Int => IO[A]): IO[Unit] = {
    def loop(i: Int): IO[Unit] =
      if (i == n) ().pure[IO]
      else action(i) >> IO.sleep(t) >> loop(i + 1)

    loop(0)
  }
  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      a <- periodic(7, 300.millis, put("Fiber 1-->")).start
      b <- periodic(10, 200.millis, put("Fiber 2-->")).start
      _ <- a.join
      _ <- b.join
    } yield ()).as(ExitCode.Success)
  }
}

object FS2FibreApp extends IOApp {
  def put(s: String): IO[Unit] = IO(println(s))
  override def run(args: List[String]): IO[ExitCode] = {
    fs2
      .Stream(
        fs2.Stream
          .repeatEval(put("Fiber 1"))
          .metered(300.millis)
          .take(3),
        fs2.Stream
          .repeatEval(put("Fiber 2"))
          .metered(300.millis)
          .take(5)
      )
      .parJoinUnbounded
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

object IOConcurrentApp extends IOApp {
  def put(s: String): IO[Unit] = IO(println(s))
  val jobOne = put("Job-1") >> IO.sleep(300.millis)
  val jobTwo = put("Job-2") >> IO.sleep(200.millis)
  //Sequential execution
  val sequentialJobs: IO[Unit] = jobOne.flatMap(_ => jobTwo)
  //Also sequential
  val alsoSequentialJobs: IO[(Unit, Unit)] = (jobOne, jobTwo).tupled

  //Concurrent execution(manually)
  //if the first job finishes in error. In that case the second job doesn't get canceled, which creates a potential memory leak.
  val concurrentJobs = for {
    jb1 <- jobOne.start
    jb2 <- jobTwo.start
    _ <- jb1.join
    _ <- jb2.join
  } yield ()

  //Concurrent execution(higher level)
  val higherLevelConcurrentJobs = (jobOne, jobTwo).parTupled
  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      _ <- sequentialJobs
      _ <- alsoSequentialJobs
      _ <- concurrentJobs
      _ <- alsoSequentialJobs
    } yield ()).as(ExitCode.Success)
  }
}

object IOBlockerResourceApp extends IOApp {
  case class Database(host: String, port: Int) { //Dummy DB
    def result: String = "Result from DB"
    def close(): IO[Unit] = IO(println("Db Closed"))
  }
  override def run(args: List[String]): IO[ExitCode] = {
    val resource = for {
      _ <- Resource.eval(IO(println("Hello, worlds")))
      db <- getDatabase()
      result <- Resource.eval(runQuery(db))
      _ <- Resource.eval(IO(println(s"Got result: ${result}")))
    } yield ()
    resource.use(_ => IO.pure(ExitCode.Success))
  }

  def getDatabase(): Resource[IO, Database] =
    Resource.make(IO(Database("host", 1234)))(_.close())

  def runQuery(db: Database): IO[String] = {
    IO.blocking(db.result)
  }
}
