package effect.telemetry.http.config

final case class AppConfig(proxy: ProxyConfig, backend: BackendConfig, tracer: TracerHost)