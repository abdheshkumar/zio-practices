package effect.telemetry

import effect.telemetry.http.config.AppConfig
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import zio._;

object JaegerTracer {

  def live: RLayer[AppConfig, Tracer] =
    ZLayer
      .service[AppConfig]
      .flatMap(c =>
        ZLayer.fromZIO(for {
          spanExporter <- ZIO.attempt(
            JaegerGrpcSpanExporter
              .builder()
              .setEndpoint(c.get.tracer.host)
              .build()
          )
          spanProcessor <- ZIO.succeed(SimpleSpanProcessor.create(spanExporter))
          tracerProvider <- ZIO.succeed {
            val serviceNameResource = Resource.create(
              Attributes
                .of(ResourceAttributes.SERVICE_NAME, "zio-todo-example")
            )
            SdkTracerProvider
              .builder()
              .addSpanProcessor(spanProcessor)
              .setResource(Resource.getDefault.merge(serviceNameResource))
              .build()
          }
          openTelemetry <- ZIO.succeed(
            OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build()
          )
          tracer <- ZIO.succeed(
            openTelemetry
              .getTracer("zio.telemetry.opentelemetry.example.JaegerTracer")
          )
        } yield tracer)
      )

}
