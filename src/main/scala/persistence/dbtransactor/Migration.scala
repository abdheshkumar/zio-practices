package persistence.dbtransactor

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import persistence.config.{Config, DBConfig}
import zio._
import zio.logging.{Logging, log}

object Migration {

  private val cpLocation = new Location("classpath:db/migration")
  private val fsLocation = new Location("filesystem:db/migration")

  val migrate: RIO[Has[DBConfig] with Logging, Unit] =
    Config.dbConfig
      .flatMap { cfg =>
        ZIO.effect {
          val config = new ClassicConfiguration()
          config.setDataSource(
            cfg.url.value,
            cfg.user,
            cfg.password
          )
          config.setLocations(cpLocation, fsLocation)
          val newFlyway = new Flyway(config)
          newFlyway.baseline()
          newFlyway.migrate()
        }.unit
      }
      .tapError(err => log.error(s"Error migrating database: $err."))

}
