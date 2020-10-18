package com.abtechsoft
import zio.{ExitCode, URIO, ZIO}
import zio.console
import zio.console.Console
object ZIOHelpers extends zio.App {

  val sumAll: ZIO[Console, Nothing, Unit] = ZIO
    .reduceAll(
      ZIO.succeed(0),
      List(1, 2, 3, 4).map(i =>
        console.putStrLn(s"reduceAll: Current value: ${i}") *> ZIO.succeed(i)
      )
    )(_ + _)
    .flatMap(v => console.putStrLn(s"reduceAll : $v"))

  val parSumAll: ZIO[Console, Nothing, Unit] = ZIO
    .reduceAllPar(
      ZIO.succeed(0),
      List(1, 2, 3, 4)
        .map(i =>
          console.putStrLn(s"reduceAllPar: Current value: ${i}") *> ZIO
            .succeed(i)
        )
    )(_ + _)
    .flatMap(v => console.putStrLn(s"reduceAllPar : $v"))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    (for {
      _ <- sumAll
      _ <- parSumAll
    } yield ()).exitCode
  }
}
