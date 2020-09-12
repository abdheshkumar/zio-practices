package persistence

import persistence.dbtransactor.DBTransactor
import zio.{Has, RIO, Task, ZIO, ZLayer}

object User {

  type UserService = Has[Service[User]]

  trait Service[A] {
    def create(user: A): Task[A]
    def find(id: Int): Task[Option[A]]
    def delete(id: Int): Task[Boolean]
  }

  def create(user: User): RIO[UserService, User] =
    RIO.accessM(_.get.create(user))
  def find(id: Int): RIO[UserService, Option[User]] =
    RIO.accessM(_.get.find(id))
  def delete(id: Int): RIO[UserService, Boolean] =
    RIO.accessM(_.get.delete(id))

  val live: ZLayer[DBTransactor, Nothing, UserService] = ZLayer.fromEffect(
    DBTransactor.transactor.map(new UserPersistenceService(_))
  )
}
case class User(id: Int, username: String)
final case class UserNotFound(id: Int) extends Exception