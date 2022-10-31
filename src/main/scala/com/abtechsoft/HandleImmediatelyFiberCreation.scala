package com.abtechsoft
import zio._

object HandleImmediatelyFiberCreation extends zio.ZIOAppDefault {
  val grandChild: UIO[Unit] =
    ZIO.succeed(println("Hello, World!"))

  val child = {
    //grandChild.fork //It will not always be printed so use grandChild.fork.flatMap(_.join)
    grandChild.fork.flatMap(_.join)
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (child.fork *> ZIO.never).exitCode
  }

}
