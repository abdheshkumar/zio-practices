import zio.ExitCode
import zio._
import zio.console._

object HandleImmediatelyFiberCreation extends zio.App {
  val grandChild: UIO[Unit] =
    ZIO.effectTotal(println("Hello, World!"))

  val child = {
    //grandChild.fork //It will not always be printed so use grandChild.fork.flatMap(_.join)
    grandChild.fork.flatMap(_.join)
  }

  override def run(args: List[String]): zio.URIO[zio.ZEnv, ExitCode] = {
    (child.fork *> ZIO.never).exitCode
  }

}
