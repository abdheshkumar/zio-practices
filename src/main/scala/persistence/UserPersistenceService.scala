package persistence

import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import persistence.UserPersistenceService._
import zio.Task
import zio.interop.catz._

class UserPersistenceService(trx: Transactor[Task])
    extends User.Service[User] {
  override def create(user: User): Task[User] = {
    Queries
      .create(user)
      .run
      .transact(trx)
      .map(_ => user)
  }

  override def find(id: Int): Task[Option[User]] = {
    Queries
      .find(id)
      .option
      .transact(trx)
  }

  override def delete(id: Int): Task[Boolean] =
    Queries
      .delete(id)
      .run
      .transact(trx)
      .fold(_ => false, _ => true)
}
object UserPersistenceService {
  object Queries {
    def find(id: Int): Query0[User] =
      sql"""SELECT * FROM USERS WHERE id = ${id} """.query[User]

    def create(user: User): Update0 =
      sql"""INSERT INTO USERS (id, username) VALUES (${user.id}, ${user.username})""".update

    def delete(id: Int): Update0 =
      sql"""DELETE FROM USERS WHERE id = $id""".update
  }

}
