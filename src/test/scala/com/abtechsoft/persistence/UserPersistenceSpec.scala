package com.abtechsoft.persistence

import persistence.User
import zio.test.Assertion._
import zio.test._

object UserPersistenceSpec extends ZIOSpecDefault {

  def spec =
    suite("Persistence integration test")(test("Persistence Live") {
      for {
        notFound <- User.find(100)
        created <- User.create(User(14, "usr"))
        deleted <- User.delete(14)
      } yield assert(notFound)(equalTo(Option.empty[User])) &&
        assert(created)(equalTo(User(14, "usr"))) &&
        assert(deleted)(isTrue)
    }).provide(Test.layer)
}
