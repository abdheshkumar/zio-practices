package effect
import zio._

object Effect_07_Fibers extends zio.App {

  def printThread = s"[${Thread.currentThread().getName}]"

  val bathTime = ZIO.succeed("Going to the bathroom")
  val boilingWater = ZIO.succeed("Boiling some water")
  val preparingCoffee = ZIO.succeed("Preparing the coffee")

  def sequentialWakeUpRoutine(): ZIO[Any, Nothing, Unit] =
    for {
      _ <- bathTime.debug(printThread)
      _ <- boilingWater.debug(printThread)
      _ <- preparingCoffee.debug(printThread)
    } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    concurrentWakeUpRoutine().exitCode

  /*
A fiber is a schedulable computation, much like a thread. However, it’s only a data structure,
which means it’s up to the ZIO runtime to schedule these fibers for execution (on the internal JVM thread pool).
Unlike a system/JVM thread which is expensive to start and stop, fibers are cheap to allocate and remove.
Hence, we can create millions of fibers and switch between them without the overheads associated with threads
   */

  def concurrentBathroomTimeAndBoilingWater(): ZIO[Any, Nothing, Unit] =
    for {
      //let’s imagine that Bob can boil the water for the coffee and going to the bathroom concurrently.
      _ <- bathTime.debug(printThread).fork
      _ <- boilingWater.debug(printThread)
    } yield ()

  def concurrentWakeUpRoutine(): ZIO[Any, Nothing, Unit] =
    for {
      bathFiber <- bathTime.debug(printThread).fork
      boilingFiber <- boilingWater.debug(printThread).fork
      zippedFiber = bathFiber.zip(boilingFiber)
      result <- zippedFiber.join.debug(printThread)
      _ <-
        ZIO.succeed(s"$result...done").debug(printThread) *> preparingCoffee
          .debug(printThread)
    } yield ()
}
