package com.abtechsoft
import zio._
import zio.Console

import java.io.IOException

/**
Managed is a data structure that encapsulates the acquisition and the release of a resource
  */
object ZManagedApp extends zio.ZIOAppDefault {
  val acquire = Console.printLine("acquiring")
  val release = Console.printLine("releasing").ignore
  val use = Console.printLine("running").ignore
  val resource: ZIO[Any, IOException, Unit] =
    ZIO.acquireReleaseWith(acquire)(_ => release)(_ => use)

  override def run = resource
}
