package streaming.environment

import streaming.environment.config.Configuration
import streaming.environment.repository.{CitiesRepository, DbTransactor}
import zio.ZLayer
import zio.blocking.Blocking
import zio.clock.Clock

object Environments {
  type HttpServerEnvironment = Configuration with Clock
  type AppEnvironment = HttpServerEnvironment
    with CitiesRepository
    with Blocking

  val httpServerEnvironment: ZLayer[Any, Nothing, Configuration with Clock] =
    Configuration.live ++ Clock.live

  val dbTransactor: ZLayer[Blocking with Any, Nothing, DbTransactor] =
    Blocking.any ++ Configuration.live >>> DbTransactor.h2

  val citiesRepository: ZLayer[Blocking with Any, Nothing, CitiesRepository] =
    dbTransactor >>> CitiesRepository.live

  val appEnvironment: ZLayer[Any with Blocking, Nothing, Configuration with Clock with CitiesRepository] = httpServerEnvironment ++ citiesRepository

}
