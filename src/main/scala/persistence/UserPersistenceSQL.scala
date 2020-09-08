package persistence

import doobie.{Query0, Transactor, Update0}
import doobie.implicits._
import zio.{Has, Task, ZLayer}
import UserPersistenceSQL._
import persistence.data.User
import persistence.dbtransactor.DBTransactor
import zio.interop.catz._

class UserPersistenceSQL(trx: Transactor[Task])
    extends CustomerService.Service {
  override def create(user: User): Task[User] = {
    Queries
      .create(user)
      .run
      .transact(trx)
      .map(_ => user)
  }

  override def find(username: String): Task[Option[User]] = {
    Queries
      .find(username)
      .option
      .transact(trx)
  }
}
object UserPersistenceSQL {
  object Queries {
    def find(username: String): Query0[User] =
      sql"""SELECT * FROM customer WHERE username = ${username} """.query[User]

    def create(user: User): Update0 =
      sql"""INSERT INTO customer (username, password) VALUES (${user.username}, ${user.password})""".update
  }

}
