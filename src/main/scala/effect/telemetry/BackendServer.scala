package effect.telemetry

import effect.telemetry.http.BackendApp
import effect.telemetry.http.config.AppConfig
import org.apache.kafka.clients.producer.RecordMetadata
import zio.console.{Console, putStrLn}
import zio.magic._
import zio.config.getConfig
import zio.config.typesafe.TypesafeConfig
import zio.config.magnolia.{Descriptor, descriptor}
import zio.telemetry.opentelemetry.Tracing
import zio.{App, Has, RIO, ZIO, ZLayer}
import sttp.model.Uri
import zhttp.service.{EventLoopGroup, Server}
import zhttp.service.server.ServerChannelFactory
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.ZStream

object BackendServer extends App {
  implicit val sttpUriDescriptor: Descriptor[Uri] =
    Descriptor[String].transformOrFailLeft(Uri.parse)(_.toString)

  val server =
    getConfig[AppConfig].flatMap { conf =>
      val port = conf.backend.host.port.getOrElse(9000)
      (Server.port(port) ++ Server.app(BackendApp.routes)).make.use(_ =>
        putStrLn(s"BackendServer started on port $port") *> ZIO.never
      )
    }

  val configLayer = TypesafeConfig.fromResourcePath(descriptor[AppConfig])

  override def run(args: List[String]) = {
    val program = Kafka.consumer.forkDaemon *> server
    program
      .injectCustom(
        configLayer,
        JaegerTracer.live,
        Tracing.live,
        ServerChannelFactory.auto,
        EventLoopGroup.auto(0),
        Kafka.producerLayer,
        Kafka.consumerLayer
      )
      .exitCode
  }

  //.exitCode
}

object Kafka {

  def producerLayer: ZLayer[Blocking, Throwable, Has[Producer]] =
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

  val consumer
      : ZIO[Console with Any with Has[Consumer] with Clock, Throwable, Unit] =
    Consumer
      .subscribeAnd(Subscription.topics("random"))
      .plainStream(Serde.string, Serde.string)
      .tap(r => putStrLn(s"Consume: ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapM(_.commit)
      .drain
      .runDrain
}
