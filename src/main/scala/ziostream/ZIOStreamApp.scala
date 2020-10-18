package ziostream
import java.nio.file.Paths

import zio.random.Random
import zio.{ExitCode, IO, UIO, URIO, ZIO, random}
import zio.stream.{ZStream, ZTransducer}
import zio.console._
//https://medium.com/@brianxiang/write-a-simple-message-processing-pipeline-using-zio-streams-cb72a3289913
object ZIOStreamApp extends zio.App {

  val oneValue: UIO[Int] = ZIO.succeed(1)
  val oneFailure: IO[RuntimeException, Nothing] = ZIO.fail(new RuntimeException)
  val requiresRandom: URIO[Random, Int] = random.nextInt
  val values: ZStream[Any, Nothing, Int] = ZStream(1, 2, 3)
  val empty: ZStream[Any, Nothing, Nothing] = ZStream.empty
  val valueThanFailure: ZStream[Any, RuntimeException, Int] =
    ZStream(1, 2) ++ ZStream.fail(new RuntimeException)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    for {
      elems <- ZStream("Hello", "World").runCollect
      _ <- putStrLn(elems.mkString(" "))
    } yield ExitCode.success
}

object InfiniteStream extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZStream
      .iterate(0)(_ + 1)
      .take(20)
      .runCollect
      .flatMap { chunk =>
        putStrLn(chunk.toString())
      }
      .exitCode
}

object Effects extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (ZStream.fromEffect(putStrLn("Hello")).drain ++
      ZStream.iterate(0)(_ + 1))
      .tap(i => putStrLn((i + 2).toString))
      .take(20)
      .runCollect
      .exitCode
}

object ControlFlow extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    ZStream
      .repeatEffect(getStrLn)
      .take(4)
      .tap(line => putStrLn(line) *> putStrLn(line))
      .runCollect
      .exitCode
}

object Transforming extends zio.App {
  case class StockQuote(symbol: String, openPrice: Double, closePrice: Double)
  val streamStocks =
    ZStream(StockQuote("DOOG", 37.84, 39.00), StockQuote("NET", 18.48, 19.01))
  val streamSymbols = streamStocks.map(_.symbol)
  val streamOpenAndClose =
    streamStocks.flatMap {
      case StockQuote(symbol, openPrice, closePrice) =>
        ZStream(
          symbol -> openPrice,
          symbol -> closePrice
        )
    }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    streamOpenAndClose.runCollect.exitCode
}

object TransDocing extends zio.App {
  val stockQuotePath = ""
  val stream = ZStream
    .fromFile(Paths.get(stockQuotePath))
    .transduce(ZTransducer.utf8Decode >>> ZTransducer.splitLines)
    .take(10)
    .tap(putStrLn(_))
    .runDrain

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    stream.exitCode

}

object StockQuoteMonitor extends zio.App{
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
  ???
}

object MergeToS3 extends zio.App{
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ???
}
