package persistence

import persistence.data.User
import persistence.dbtransactor.DBTransactor
import zio.{RIO, Task, ZIO, ZLayer}

object CustomerService {

  trait Service {
    def create(user: User): Task[User]
    def find(username: String): Task[Option[User]]
  }

  def create(user: User): ZIO[CustomerService, Throwable, User] =
    RIO.accessM[CustomerService](_.get.create(user))
  def find(username: String): ZIO[CustomerService, Throwable, Option[User]] =
    RIO.accessM[CustomerService](_.get.find(username))

  val live: ZLayer[DBTransactor, Nothing, CustomerService] = ZLayer.fromEffect(
    DBTransactor.transactor.map(new UserPersistenceSQL(_))
  )
}
