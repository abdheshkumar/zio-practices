package com.abtechsoft.persistence

import persistence.User
import zio.test.Assertion._
import zio.test._

object UserPersistenceTest extends ZIOSpecDefault {

  def spec =
    suite("Persistence unit test")(
      test("get a non existing user should fail") {
        User.find(100).map(uOpt => assert(uOpt)(equalTo(Option.empty[User])))
      },
      test("create a user then get it ") {
        for {
          created <- User.create(User(14, "usr"))
          user <- User.find(14)
        } yield assert(created)(equalTo(User(14, "usr"))) &&
          assert(user)(equalTo(Some(User(14, "usr"))))
      },
      test("delete user") {
        for {
          deleted <- User.delete(14)
          notFound <- User.find(14)
        } yield assert(deleted)(isTrue) &&
          assert(notFound)(equalTo(Option.empty[User]))
      }
    ).provideSomeLayer[TestEnvironment](Test.layer)
}
