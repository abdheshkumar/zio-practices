package streaming.environment

import streaming.domain.City
import streaming.environment.config.Configuration.{AppConfig, DbConfig}
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits.rts

package object repository {

  type DbTransactor = DbTransactor.Resource
  type CitiesRepository = CitiesRepository.Service

  def allCities: RIO[CitiesRepository, fs2.Stream[Task, City]] =
    ZIO.serviceWith(_.all)

  def cityById(id: Int): RIO[CitiesRepository, Task[Option[City]]] =
    ZIO.serviceWith(_.byId(id))

  def citiesByCountry(
      country: String
  ): RIO[CitiesRepository, fs2.Stream[Task, City]] =
    ZIO.serviceWith(_.byCountry(country))

  object DbTransactor {
    trait Resource {
      val xa: Transactor[Task]
    }

    val h2: URLayer[AppConfig, DbTransactor] = ZLayer {
      ZIO.service[AppConfig].map { appConfig =>
        val db = appConfig.dbConfig
        new Resource {
          val xa: Transactor[Task] =
            Transactor
              .fromDriverManager(db.driver, db.url, db.user, db.password)
        }
      }
    }
  }
}
