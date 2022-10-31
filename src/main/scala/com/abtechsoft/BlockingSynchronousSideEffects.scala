package com.abtechsoft
import zio._
import scala.io.{Codec, Source}
object BlockingSynchronousSideEffects extends zio.ZIOAppDefault {

  def download(url: String): Task[String] =
    ZIO.attempt {
      Source.fromURL(url)(Codec.UTF8).mkString
    }

  def safeDownload(url: String): ZIO[Any, Throwable, String] =
    ZIO.blocking(download(url))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {

//The resulting effect will be executed on a separate thread pool designed specifically for blocking effects.
    val sleeping: Task[Unit] = ZIO.attemptBlocking {
      println("Running blocking" + Thread.currentThread().getName)
      Thread.sleep(Long.MaxValue)
    }
    (for {
      _ <- sleeping
      _ <- Console.printLine("After blocking")
    } yield ()).exitCode
  }
}
