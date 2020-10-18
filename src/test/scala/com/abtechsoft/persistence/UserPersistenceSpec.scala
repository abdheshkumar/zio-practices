package com.abtechsoft.persistence

import persistence.{MainApp, User}
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Cause, ZLayer}

object UserPersistenceSpec extends DefaultRunnableSpec {

  def spec =
    suite("Persistence integration test")(testM("Persistence Live") {
      for {
        notFound <- User.find(100)
        created <- User.create(User(14, "usr"))
        deleted <- User.delete(14)
      } yield assert(notFound)(equalTo(Option.empty[User])) &&
        assert(created)(equalTo(User(14, "usr"))) &&
        assert(deleted)(isTrue)
    }).provideSomeLayer[TestEnvironment](
      MainApp.appLayers
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )

}
