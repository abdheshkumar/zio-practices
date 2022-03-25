package effect.kafka

import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.duration.durationInt
import zio.kafka.consumer.{Consumer, ConsumerSettings, _}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.random.Random
import zio.stream.ZStream

object ZIOKafkaProducerConsumerExample extends zio.App {

  val producer: ZStream[Any with Has[
    Producer
  ] with Random with Clock, Throwable, Nothing] =
    ZStream
      .repeatEffect(zio.random.nextIntBetween(0, Int.MaxValue))
      .schedule(Schedule.fixed(2.seconds))
      .mapM { random =>
        Producer.produce[Any, Long, String](
          topic = "random",
          key = random.toLong % 4,
          value = random.toString,
          keySerializer = Serde.long,
          valueSerializer = Serde.string
        )
      }
      .drain

  val consumer =
    Consumer
      .subscribeAnd(Subscription.topics("random"))
      .plainStream(Serde.string, Serde.string)
      .tap(r => putStrLn(s"Consume: ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapM(_.commit)
      .drain

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    /*producer
      .merge(consumer)*/
    consumer.runDrain
      .provideCustomLayer(appLayer)
      .exitCode

  def producerLayer =
    ZLayer.fromManaged(
      Producer.make(
        settings = ProducerSettings(List("localhost:29092"))
      )
    )

  def consumerLayer: ZLayer[Clock with Blocking, Throwable, Has[Consumer]] =
    ZLayer.fromManaged(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
      )
    )

  def appLayer = producerLayer ++ consumerLayer
}
