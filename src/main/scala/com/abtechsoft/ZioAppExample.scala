package com.abtechsoft

import java.io.IOException
import java.util.concurrent.TimeUnit

import zio._
import zio.console._
import zio.console.Console

object ZioAppExample extends zio.App {

  val program: ZIO[Console, Nothing, Unit] =
    putStrLn("TicTacToe game!")

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    program.as(ExitCode.success)
  //program.provideLayer(zio.ZEnv.any).flatMap(_ => ZIO.succeed(0))
}

object PrintSequence extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    //putStrLn("Hello").zipRight(putStrLn("World"))
    (putStrLn("Hello") *> putStrLn("World")) *> ZIO.succeed(
      ExitCode.success
    ) //*> is zipRight
  }
}

object ErrorRecovery extends zio.App {
  val failed = putStrLn("About to fail...") *> ZIO.fail("Uh oh!") *> putStrLn(
    "This will NEVER be printed"
  )
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    //failed as 0 //Not compile because error is a String type
    //(failed as 0) orElse ZIO.succeed(1)
    //failed.fold(_ => 1, _ => 0)
    (failed as 0)
      .catchAllCause(cause => putStrLn(s"${cause.prettyPrint}"))
      .as(ExitCode.success)
  }
}

object Looping extends zio.App {

  def repeat[R, E, A](n: Int)(effect: ZIO[R, E, A]): ZIO[R, E, A] = {
    if (n <= 1) effect
    else effect *> repeat(n - 1)(effect)
  }
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    repeat(100)(
      putStrLn("All work and no play makes Jack a dull boy")
    ) as ExitCode.success
  }
}

object PromptName extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    /* putStrLn("What is your name?") *> getStrLn
      .flatMap(name => putStrLn(s"Hello ${name}")) //We can use for-comprehension
      .fold(_ => 1, _ => 0)*/
    //
    (for {
      _ <- putStrLn("What is your name?")
      name <- getStrLn
      name <- putStrLn(s"Hello ${name}")
    } yield ExitCode.success).orElse(ZIO.succeed(ExitCode.failure))
  }
}

object NumberGuesser extends zio.App {
  import zio.random._
  def analyzeAnswer(random: Int, guess: String) =
    if (random.toString == guess.trim) putStrLn("You guessed correctly!")
    else putStrLn(s"You did not guess correctly. The answer was ${random}")

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    (for {
      random <- nextIntBounded(3)
      _ <- putStrLn("Please guess a number from 0 to 3:")
      guess <- getStrLn
      _ <- analyzeAnswer(random, guess)
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.success)
  }
}

object AlarmAppImproved extends zio.App {
  import zio.duration._
  def toDoubles(s: String): Either[NumberFormatException, Double] =
    try (Right(s.toDouble))
    catch { case e: NumberFormatException => Left(e) }

  lazy val getAlarmDuration: ZIO[Console, IOException, Duration] = {
    def parseDuration(input: String) =
      toDoubles(input).map(double =>
        Duration((double * 1000.0).toLong, TimeUnit.MILLISECONDS)
      )
    val fallback =
      putStrLn("You didn't enter the number of seconds!") *> getAlarmDuration
    for {
      _ <- putStrLn("Please enter the number of seconds to sleep:")
      input <- getStrLn
      duration <- ZIO.fromEither(parseDuration(input)) orElse fallback
    } yield duration
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    (for {
      duration <- getAlarmDuration
      fiber <- (putStrLn(".") *> ZIO.sleep(1.second)).forever.fork
      _ <- putStrLn("Time to wake up!!").delay(duration) *> fiber.interrupt
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}

object ComputePi extends zio.App {

  import zio.random._
  import zio.duration._

  final case class PiState(inside: Long, total: Long)

  def estimatePi(inside: Long, total: Long): Double =
    (inside.toDouble / total.toDouble) * 4.0

  def insideCircle(x: Double, y: Double): Boolean =
    Math.sqrt(x * x + y * y) <= 1.0

  val randomPoint: ZIO[Random, Nothing, (Double, Double)] =
    nextDouble zip nextDouble

  def updateOnce(ref: Ref[PiState]): ZIO[Random, Nothing, Unit] = {
    for {
      tuple <- randomPoint
      (x, y) = tuple
      inside = if (insideCircle(x, y)) 1 else 0
      _ <- ref.update(state => PiState(state.inside + inside, state.total + 1))
    } yield ()
  }

  def printEstimate(ref: Ref[PiState]): ZIO[Console, Nothing, Unit] =
    for {
      state <- ref.get
      _ <- putStrLn(s"${estimatePi(state.inside, state.total)}")
    } yield ()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    (for {
      ref <- Ref.make(PiState(0L, 0L))
      worker = updateOnce(ref).forever
      workers: Seq[ZIO[Random, Nothing, Nothing]] = List.fill(4)(worker)
      //fiber1 <- ZIO.forkAll(workers) //Not working
      fiber2 <- (printEstimate(ref) *> ZIO.sleep(1.second)).forever.fork
      _ <- putStrLn("Enter any key to terminate...")
      _ <- getStrLn *> (/*fiber1 zip */fiber2).interrupt
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}

object StmDiningPhilosophers extends zio.App {
  import zio.clock._
  import zio.stm._

  final case class Fork(number: Int)
  final case class Placement(
      left: TRef[Option[Fork]],
      right: TRef[Option[Fork]]
  )
  final case class Roundtable(seats: Vector[Placement])

  def takeForks(
      left: TRef[Option[Fork]],
      right: TRef[Option[Fork]]
  ): STM[Nothing, (Fork, Fork)] = {
    left.get.collect { case Some(fork) => fork } zip right.get.collect {
      case Some(fork) => fork
    }
    /*for {
      leftFork <- left.get.collect { case Some(fork)   => fork }
      rightFork <- right.get.collect { case Some(fork) => fork }
    } yield (leftFork, rightFork)*/

  }

  def putForks(left: TRef[Option[Fork]], right: TRef[Option[Fork]])(
      tuple: (Fork, Fork)
  ): STM[Nothing, Unit] = {
    val (leftFork, rightFork) = tuple
    for {
      _ <- right.set(Some(rightFork))
      _ <- left.set(Some(leftFork))
    } yield ()
  }

  def setupTable(size: Int): ZIO[Any, Nothing, Roundtable] = {
    def makeFork(i: Int) = TRef.make[Option[Fork]](Some(Fork(i)))
    (for {
      allForks0 <- STM.foreach(0 to size) { i =>
        makeFork(i)
      }
      allForks = allForks0 ++ List(allForks0(0))
      placeMents = (allForks zip allForks.drop(1)).map {
        case (l, r) => Placement(l, r)
      }
    } yield Roundtable(placeMents.toVector)).commit
  }

  def eat(
      philosopher: Int,
      roundtable: Roundtable
  ): ZIO[Console, Nothing, Unit] = {
    val placement = roundtable.seats(philosopher)
    val left = placement.left
    val right = placement.right
    for {
      forks <- takeForks(left, right).commit
      _ <- putStrLn(s"Philosopher ${philosopher} eating...")
      _ <- putForks(left, right)(forks).commit
      _ <- putStrLn(s"Philosopher ${philosopher} is done eating")
    } yield ()
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val count = 10
    def eaters(roundtable: Roundtable): Iterable[ZIO[Console, Nothing, Unit]] =
      (0 to count).map(index => eat(index, roundtable))
    for {
      table <- setupTable(count)
      fiber <- ZIO.forkAll(eaters(table))
      _ <- fiber.join
      _ <- putStrLn("All philosophers have eaten!")
    } yield ExitCode.success
  }
}

object Actors extends zio.App {
  import zio.console._
  import zio.stm._
  sealed trait Command
  case object ReadTemperature extends Command
  final case class AdjustTemperature(value: Double) extends Command
  type TemperatureActor = Command => Task[Double]
  def makActor(initialTemperature: Double): UIO[TemperatureActor] = {
    type Bundle = (Command, Promise[Nothing, Double])
    val r: UIO[Queue[(Command, Promise[Nothing, Double])]] =
      Queue.bounded[Bundle](1000)
    for {
      ref <- Ref.make(initialTemperature)
      queue <- Queue.bounded[Bundle](1000)
      _ <-
        queue.take
          .flatMap {
            case (ReadTemperature, promise) =>
              ref.get.flatMap(promise.succeed(_))
            case (AdjustTemperature(d), promise) =>
              ref.updateAndGet(_ + d).flatMap(promise.succeed(_))
          }
          .forever
          .fork
    } yield (c: Command) =>
      Promise
        .make[Nothing, Double]
        .flatMap(p => queue.offer(c -> p) *> p.await)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val temperatures = (0 to 100).map(_.toDouble)
    (for {
      actor <- makActor(0)
      _ <-
        ZIO
          .foreachPar(temperatures)(temp => actor(AdjustTemperature(temp)))
      temp <- actor(ReadTemperature)
      _ <- putStrLn(s"Final temperature is ${temp}")
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}
