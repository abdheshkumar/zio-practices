package effect.telemetry

import effect.telemetry.http.{MiddlewareClient, ProxyApp}
import effect.telemetry.http.config.AppConfig
import zio.{Console, Scope, ZIO, ZLayer}
import zio.config.getConfig
import zio.config.typesafe.TypesafeConfig
import zio.config.magnolia.{Descriptor, descriptor}
import zio.telemetry.opentelemetry.Tracing
import zhttp.service.{
  ChannelFactory,
  EventLoopGroup,
  Server,
  ServerChannelFactory
}
import zhttp.service.server.ServerChannelFactory
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde

object BackendServer extends zio.ZIOAppDefault {

  val server =
    getConfig[AppConfig].flatMap { conf =>
      val port = 9000
      (Server.port(9000) ++ Server.app(ProxyApp.routes)).make.flatMap(_ =>
        Console.printLine(s"BackendServer started on port $port") *> ZIO.never
      )
    }

  val configLayer = TypesafeConfig.fromResourcePath(descriptor[AppConfig])

  override def run = {
    val program: ZIO[
      MiddlewareClient.Service with Tracing with EventLoopGroup with ServerChannelFactory with Scope with AppConfig with Any with Consumer,
      Throwable,
      Nothing
    ] = Kafka.consumer.forkDaemon *> server
    program
      .provide(
        configLayer,
        MiddlewareClient.live,
        JaegerTracer.live,
        Tracing.live,
        ServerChannelFactory.auto,
        ChannelFactory.auto,
        EventLoopGroup.auto(0),
        Scope.default,
        // Kafka.producerLayer,
        Kafka.consumerLayer
      )
  }

}

object Kafka {

  def producerLayer: ZLayer[Any, Throwable, Producer] =
    ZLayer.scoped(
      Producer.make(ProducerSettings(List("localhost:29092")))
    )

  def consumerLayer: ZLayer[Any, Throwable, Consumer] =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
      )
    )

  val consumer: ZIO[Any with Consumer, Throwable, Unit] =
    Consumer
      .subscribeAnd(Subscription.topics("random"))
      .plainStream(Serde.string, Serde.string)
      .tap(r => Console.printLine(s"Consume: ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain
      .runDrain
}
