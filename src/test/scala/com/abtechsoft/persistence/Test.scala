package com.abtechsoft.persistence

import persistence.{User, UserNotFound}
import zio.{Ref, Task, ZLayer}

case class Test(users: Ref[Vector[User]]) extends User.Service[User] {
  def find(id: Int): Task[Option[User]] =
    users.get.flatMap(users => Task.succeed(users.find(_.id == id)))
  def create(user: User): Task[User] =
    users.update(_ :+ user).map(_ => user)
  def delete(id: Int): Task[Boolean] =
    users.modify(users => true -> users.filterNot(_.id == id))
}

object Test {
  val layer: ZLayer[Any, Nothing, User.UserService] =
    ZLayer.fromEffect(Ref.make(Vector.empty[User]).map(Test(_)))
}
