package persistence

import persistence.dbtransactor.DBTransactor
import zio.{RIO, Task, ZIO, ZLayer}

object User {

  type UserService = Service[User]

  trait Service[A] {
    def create(user: A): Task[A]
    def find(id: Int): Task[Option[A]]
    def delete(id: Int): Task[Boolean]
  }

  def create(user: User): RIO[UserService, User] =
    ZIO.serviceWithZIO(_.create(user))
  def find(id: Int): RIO[UserService, Option[User]] =
    ZIO.serviceWithZIO(_.find(id))
  def delete(id: Int): RIO[UserService, Boolean] =
    ZIO.serviceWithZIO(_.delete(id))

  val live: ZLayer[DBTransactor, Nothing, UserService] = ZLayer(
    DBTransactor.transactor.map(new UserPersistenceService(_))
  )
}
case class User(id: Int, username: String)
final case class UserNotFound(id: Int) extends Exception
