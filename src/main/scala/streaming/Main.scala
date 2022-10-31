package streaming

import streaming.environment.config.Configuration
import streaming.environment.repository.{CitiesRepository, DbTransactor}
import streaming.http.Server

object Main extends zio.ZIOAppDefault {
  def run = {
    val program = for {
      _ <- Server.runServer
    } yield ()

    program.provide(Configuration.live, CitiesRepository.live, DbTransactor.h2)
  }
}
