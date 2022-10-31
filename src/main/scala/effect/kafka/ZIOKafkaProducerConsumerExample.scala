package effect.kafka

import zio._
import zio.kafka.consumer.{Consumer, ConsumerSettings, _}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object ZIOKafkaProducerConsumerExample extends zio.ZIOAppDefault {

  val producer: ZStream[Producer, Throwable, Nothing] =
    ZStream
      .repeatZIO(zio.Random.nextIntBetween(0, Int.MaxValue))
      .schedule(Schedule.fixed(2.seconds))
      .mapZIO { random =>
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
      .tap(r => Console.printLine(s"Consume: ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)

  override def run = {
    producer
      .merge(consumer)
      .provideLayer(appLayer)
      .runDrain
  }

  def producerLayer =
    ZLayer.scoped(Producer.make(ProducerSettings(List("localhost:29092"))))

  def consumerLayer =
    ZLayer.scoped(
      Consumer
        .make(ConsumerSettings(List("localhost:29092")).withGroupId("group"))
    )

  val appLayer: ZLayer[Any, Throwable, Producer with Consumer] =
    producerLayer ++ consumerLayer
}
