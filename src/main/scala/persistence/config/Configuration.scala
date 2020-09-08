package persistence.config

final case class Configuration(
    httpServer: HttpServerConfig,
    httpClient: HttpClientConfig,
    dbConfig: PostgresConfig,
    reqResClient: ReqResConfig
)
