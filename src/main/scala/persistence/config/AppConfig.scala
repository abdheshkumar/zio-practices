package persistence.config

final case class AppConfig(
    httpServer: HttpServerConfig,
    httpClient: HttpClientConfig,
    dbConfig: DBConfig
)
