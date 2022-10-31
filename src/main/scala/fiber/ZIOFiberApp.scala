package fiber
import zio._
object ZIOFiberApp extends zio.ZIOAppDefault {
  /*
A fiber is a lightweight thread of execution that never consumes more than a whole thread (but may consume much less, depending on contention and asynchronicity). Fibers are spawned by forking ZIO effects, which run concurrently with the parent effect.
Fibers can be joined, yielding their result to other fibers, or interrupted, which terminates the fiber, safely releasing all resources.
   */
  def parallel[A, B](io1: Task[A], io2: Task[B]): Task[(A, B)] =
    for {
      fiber1 <- io1.fork
      fiber2 <- io2.fork
      _ <- fiber1.interrupt
      a <- fiber1.join
      b <- fiber2.join
    } yield (a, b)

  def task(i: Long): Task[Unit] =
    ZIO.attempt({
      Thread.sleep(3000 * i)
      println(s"Hi from $i! ${Thread.currentThread().getName}")
    })
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    parallel(task(2), task(1)).exitCode
}
