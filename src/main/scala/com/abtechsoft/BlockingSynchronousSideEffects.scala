package com.abtechsoft
import zio._
import zio.blocking.blocking
import zio.blocking._
import scala.io.{Codec, Source}
object BlockingSynchronousSideEffects extends zio.App {

  def download(url: String) =
    Task.effect {
      Source.fromURL(url)(Codec.UTF8).mkString
    }

  def safeDownload(url: String) =
    blocking(download(url))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

//The resulting effect will be executed on a separate thread pool designed specifically for blocking effects.
    val sleeping: RIO[Blocking, Unit] = effectBlocking {
      println("Running blocking" + Thread.currentThread().getName)
      Thread.sleep(Long.MaxValue)
    }
    (for {
      _ <- sleeping
      _ <- console.putStrLn("After blocking")
    } yield ()).exitCode
  }
}
