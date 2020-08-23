package com.abtechsoft
import zio._
import zio.console._

/**
Managed is a data structure that encapsulates the acquisition and the release of a resource
 */
object ZManagedApp extends zio.App {

  val zManagedResource: ZManaged[Console, Nothing, Unit] =
    ZManaged.make(console.putStrLn("acquiring"))(_ =>
      console.putStrLn("releasing")
    )
  val zUsedResource: URIO[Console, Unit] = zManagedResource.use { _ =>
    console.putStrLn("running")
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    zUsedResource.as(ExitCode.success)
  }
}
