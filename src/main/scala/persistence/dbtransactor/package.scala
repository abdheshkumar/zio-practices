package persistence

import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import persistence.config.{AppConfig, DBConfig}
import zio.interop.catz._
import zio.interop.catz.implicits.rts
import zio.{Scope, Task, URIO, ZIO, ZLayer}

import scala.concurrent.ExecutionContext

package object dbtransactor {

  type DBTransactor = Transactor[Task]

  object DBTransactor {
    private def makeTransactor(
        conf: DBConfig,
        connectEC: ExecutionContext
    ): ZIO[Any with Scope, Throwable, HikariTransactor[Task]] =
      HikariTransactor
        .newHikariTransactor[Task](
          conf.driver.value,
          conf.url.value,
          conf.user,
          conf.password,
          connectEC
        )
        .toScopedZIO

    val managed: ZIO[Any with Scope with AppConfig, Throwable, HikariTransactor[
      Task
    ]] =
      for {
        config <- ZIO.service[AppConfig]
        connectEC <- ZIO.executor
        transactor <-
          makeTransactor(config.dbConfig, connectEC.asExecutionContext)
      } yield transactor

    val live: ZLayer[AppConfig, Throwable, HikariTransactor[Task]] =
      ZLayer.scoped { managed }

    val transactor: URIO[DBTransactor, Transactor[Task]] = ZIO.service

  }
}
