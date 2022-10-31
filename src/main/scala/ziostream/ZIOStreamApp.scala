package ziostream
import zio.stream.{ZPipeline, ZStream}
import zio.{Console, IO, Scope, UIO, ZIO, ZIOAppArgs}
object ZIOStreamApp extends zio.ZIOAppDefault {

  val oneValue: UIO[Int] = ZIO.succeed(1)
  val oneFailure: IO[RuntimeException, Nothing] = ZIO.fail(new RuntimeException)
  val values: ZStream[Any, Nothing, Int] = ZStream(1, 2, 3)
  val empty: ZStream[Any, Nothing, Nothing] = ZStream.empty
  val valueThanFailure: ZStream[Any, RuntimeException, Int] =
    ZStream(1, 2) ++ ZStream.fail(new RuntimeException)

  override def run =
    (for {
      elems <- ZStream("Hello", "World").runCollect
      _ <- Console.printLine(elems.mkString(" "))
    } yield ()).exitCode
}

object InfiniteStream extends zio.ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    ZStream
      .iterate(0)(_ + 1)
      .take(20)
      .runCollect
      .flatMap { chunk =>
        Console.printLine(chunk.toString())
      }
      .exitCode
}

object Effects extends zio.ZIOAppDefault {
  override def run =
    (ZStream.fromZIO(Console.printLine("Hello")).drain ++
      ZStream.iterate(0)(_ + 1))
      .tap(i => Console.printLine((i + 2).toString))
      .take(20)
      .runCollect
      .exitCode
}

object ControlFlow extends zio.ZIOAppDefault {
  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    ZStream
      .repeatZIO(Console.readLine)
      .take(4)
      .tap(line => Console.printLine(line) *> Console.printLine(line))
      .runCollect
      .exitCode
}

object Transforming extends zio.ZIOAppDefault {
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

  override def run =
    streamOpenAndClose.runCollect.exitCode
}

object TransDocing extends zio.ZIOAppDefault {
  val stockQuotePath = "file.txt"
  val stream = ZStream
    .fromFileName(stockQuotePath)
    .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
    .take(10)
    .tap(Console.printLine(_))
    .runDrain

  override def run = stream.exitCode

}
