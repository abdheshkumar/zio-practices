package persistence.dbtransactor

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import persistence.config.AppConfig
import zio._

object Migration {

  private val cpLocation = new Location("classpath:db/migration")
  private val fsLocation = new Location("filesystem:db/migration")

  val migrate: ZIO[AppConfig, Throwable, Unit] =
    ZIO
      .service[AppConfig]
      .flatMap { cfg =>
        ZIO.attempt {
          val config = new ClassicConfiguration()
          config.setDataSource(
            cfg.dbConfig.url.value,
            cfg.dbConfig.user,
            cfg.dbConfig.password
          )
          config.setLocations(cpLocation, fsLocation)
          val newFlyway = new Flyway(config)
          newFlyway.baseline()
          newFlyway.migrate()
        }.unit
      }
      .tapError(err => ZIO.logError(s"Error migrating database: $err."))

}
