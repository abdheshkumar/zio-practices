package com.abtechsoft
import zio.{Console, Scope, ZIO, ZIOAppArgs}

import java.io.IOException
object ZIOHelpers extends zio.ZIOAppDefault {

  val sumAll: ZIO[Any, IOException, Unit] = ZIO
    .reduceAll(
      ZIO.succeed(0),
      List(1, 2, 3, 4).map(i =>
        Console.printLine(s"reduceAll: Current value: ${i}") *> ZIO.succeed(i)
      )
    )(_ + _)
    .flatMap(v => Console.printLine(s"reduceAll : $v"))

  val parSumAll: ZIO[Any, IOException, Unit] = ZIO
    .reduceAllPar(
      ZIO.succeed(0),
      List(1, 2, 3, 4)
        .map(i =>
          Console.printLine(s"reduceAllPar: Current value: $i") *> ZIO
            .succeed(i)
        )
    )(_ + _)
    .flatMap(v => Console.printLine(s"reduceAllPar : $v"))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    (for {
      _ <- sumAll
      _ <- parSumAll
    } yield ())
  }
}
