package com.abtechsoft

import java.io.IOException
import java.util.concurrent.TimeUnit

import zio._
import zio.Console

object ZioAppExample extends zio.ZIOAppDefault {

  val program: ZIO[Any, IOException, Unit] =
    Console.printLine("TicTacToe game!")

  def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = program
}

object PrintSequence extends zio.ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    Console.printLine("Hello") *> Console.printLine("World") //*> is zipRight
  }
}

object ErrorRecovery extends zio.ZIOAppDefault {
  val failed = Console.printLine("About to fail...") *> ZIO.fail(
    "Uh oh!"
  ) *> Console.printLine(
    "This will NEVER be printed"
  )
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    //failed as 0 //Not compile because error is a String type
    //(failed as 0) orElse ZIO.succeed(1)
    //failed.fold(_ => 1, _ => 0)
    failed
      .catchAllCause(cause => Console.printLine(s"${cause.prettyPrint}"))
      .exitCode
  }
}

object Looping extends zio.ZIOAppDefault {

  def repeat[R, E, A](n: Int)(effect: ZIO[R, E, A]): ZIO[R, E, A] = {
    if (n <= 1) effect
    else effect *> repeat(n - 1)(effect)
  }
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    repeat(100)(
      Console.printLine("All work and no play makes Jack a dull boy")
    ).exitCode
  }
}

object PromptName extends zio.ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    /* Console.printLine("What is your name?") *> Console.readLine
      .flatMap(name => Console.printLine(s"Hello ${name}")) //We can use for-comprehension
      .fold(_ => 1, _ => 0)*/
    //
    (for {
      _ <- Console.printLine("What is your name?")
      name <- Console.readLine
      _ <- Console.printLine(s"Hello ${name}")
    } yield ExitCode.success).orElse(ZIO.succeed(ExitCode.failure))
  }
}

object NumberGuesser extends zio.ZIOAppDefault {
  def analyzeAnswer(random: Int, guess: String) =
    if (random.toString == guess.trim)
      Console.printLine("You guessed correctly!")
    else
      Console.printLine(
        s"You did not guess correctly. The answer was ${random}"
      )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (for {
      random <- Random.nextIntBounded(3)
      _ <- Console.printLine("Please guess a number from 0 to 3:")
      guess <- Console.readLine
      _ <- analyzeAnswer(random, guess)
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.success)
  }
}

object AlarmAppImproved extends zio.ZIOAppDefault {
  def toDoubles(s: String): Either[NumberFormatException, Double] =
    try (Right(s.toDouble))
    catch { case e: NumberFormatException => Left(e) }

  lazy val getAlarmDuration: ZIO[Any, IOException, Duration] = {
    def parseDuration(input: String) =
      toDoubles(input).map(double =>
        Duration((double * 1000.0).toLong, TimeUnit.MILLISECONDS)
      )
    val fallback =
      Console.printLine(
        "You didn't enter the number of seconds!"
      ) *> getAlarmDuration
    for {
      _ <- Console.printLine("Please enter the number of seconds to sleep:")
      input <- Console.readLine
      duration <- ZIO.fromEither(parseDuration(input)) orElse fallback
    } yield duration
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (for {
      duration <- getAlarmDuration
      fiber <- (Console.printLine(".") *> ZIO.sleep(1.second)).forever.fork
      _ <-
        Console
          .printLine("Time to wake up!!")
          .delay(duration) *> fiber.interrupt
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}

object ComputePi extends zio.ZIOAppDefault {

  final case class PiState(inside: Long, total: Long)

  def estimatePi(inside: Long, total: Long): Double =
    (inside.toDouble / total.toDouble) * 4.0

  def insideCircle(x: Double, y: Double): Boolean =
    Math.sqrt(x * x + y * y) <= 1.0

  val randomPoint: ZIO[Any, Nothing, (Double, Double)] =
    Random.nextDouble zip Random.nextDouble

  def updateOnce(ref: Ref[PiState]): ZIO[Any, Nothing, Unit] = {
    for {
      tuple <- randomPoint
      (x, y) = tuple
      inside = if (insideCircle(x, y)) 1 else 0
      _ <- ref.update(state => PiState(state.inside + inside, state.total + 1))
    } yield ()
  }

  def printEstimate(ref: Ref[PiState]): ZIO[Any, IOException, Unit] =
    for {
      state <- ref.get
      _ <- Console.printLine(s"${estimatePi(state.inside, state.total)}")
    } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (for {
      ref <- Ref.make(PiState(0L, 0L))
      worker = updateOnce(ref).forever
      _ = List.fill(4)(worker)
      //fiber1 <- ZIO.forkAll(workers) //Not working
      fiber2 <- (printEstimate(ref) *> ZIO.sleep(1.second)).forever.fork
      _ <- Console.printLine("Enter any key to terminate...")
      _ <- Console.readLine *> (/*fiber1 zip */ fiber2).interrupt
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}

object StmDiningPhilosophers extends zio.ZIOAppDefault {
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
  ): ZIO[Any, IOException, Unit] = {
    val placement = roundtable.seats(philosopher)
    val left = placement.left
    val right = placement.right
    for {
      forks <- takeForks(left, right).commit
      _ <- Console.printLine(s"Philosopher ${philosopher} eating...")
      _ <- putForks(left, right)(forks).commit
      _ <- Console.printLine(s"Philosopher ${philosopher} is done eating")
    } yield ()
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val count = 10
    def eaters(
        roundtable: Roundtable
    ): Iterable[ZIO[Any, IOException, Unit]] =
      (0 to count).map(index => eat(index, roundtable))

    (for {
      table <- setupTable(count)
      fiber <- ZIO.forkAll(eaters(table))
      _ <- fiber.join
      _ <- Console.printLine("All philosophers have eaten!")
    } yield ()).exitCode
  }
}

object Actors extends zio.ZIOAppDefault {
  sealed trait Command
  case object ReadTemperature extends Command
  final case class AdjustTemperature(value: Double) extends Command
  type TemperatureActor = Command => Task[Double]
  def makActor(initialTemperature: Double): UIO[TemperatureActor] = {
    type Bundle = (Command, Promise[Nothing, Double])
    val _: UIO[Queue[(Command, Promise[Nothing, Double])]] =
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

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val temperatures = (0 to 100).map(_.toDouble)
    (for {
      actor <- makActor(0)
      _ <-
        ZIO
          .foreachPar(temperatures)(temp => actor(AdjustTemperature(temp)))
      temp <- actor(ReadTemperature)
      _ <- Console.printLine(s"Final temperature is ${temp}")
    } yield ExitCode.success) orElse ZIO.succeed(ExitCode.failure)
  }
}
