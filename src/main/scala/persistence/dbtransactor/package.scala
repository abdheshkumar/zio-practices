package persistence

import scala.concurrent.ExecutionContext
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import persistence.config.{Config, DBConfig}
import zio.blocking.Blocking
import zio.interop.catz._
import zio.interop.catz.implicits.rts
import zio.logging.Logging
import zio.{Has, Managed, Task, URIO, ZIO, ZLayer, ZManaged, blocking}

package object dbtransactor {

  type DBTransactor = Has[Transactor[Task]]

  object DBTransactor {
    private def makeTransactor(
        conf: DBConfig,
        connectEC: ExecutionContext
    ): Managed[Throwable, Transactor[Task]] =
      HikariTransactor
        .newHikariTransactor[Task](
          conf.driver.value,
          conf.url.value,
          conf.user,
          conf.password,
          connectEC
        )
        .toManagedZIO

    val managed: ZManaged[Has[
      DBConfig
    ] with Blocking, Throwable, Transactor[Task]] =
      for {
        config <- Config.dbConfig.toManaged_
        connectEC <- ZIO.descriptor.map(_.executor.asEC).toManaged_
        transactor <- makeTransactor(config, connectEC)
      } yield transactor

    /*val managedWithMigration: ZManaged[Has[
      DBConfig
    ] with Logging with Blocking, Throwable, Transactor[Task]] =
      Migration.migrate.toManaged_ *> managed*/

    val test: ZLayer[Has[DBConfig] with Blocking, Throwable, DBTransactor] =
      ZLayer.fromManaged(managed)

    val live: ZLayer[Has[
      DBConfig
    ] with Logging with Blocking, Throwable, DBTransactor] =
      ZLayer.fromManaged(managed)

    val transactor: URIO[DBTransactor, Transactor[Task]] = ZIO.service

  }
}
