package effect

import zio.{Task, ULayer, ZIO, ZLayer, Console}

object Effect_06_EndToEnd_App extends zio.ZIOAppDefault {

  case class User(name: String, email: String)
  // service definition
  trait UserEmailer {
    def sendEmail(
        user: User,
        message: String
    ): Task[Unit] // ZIO[Any, Throwable, Unit]
  }
  object UserEmailer {

    // layer; includes service implementation
    val liveLayer: ZLayer[Any, Nothing, UserEmailer] =
      ZLayer.succeed(new UserEmailer {
        override def sendEmail(user: User, message: String): Task[Unit] =
          ZIO.attempt {
            println(s"[User emailer] sending '$message' to ${user.email}")
          }
      })

    // front-facing API, aka "accessor"
    def sendEmail(
        user: User,
        message: String
    ): ZIO[UserEmailer, Throwable, Unit] =
      ZIO.serviceWithZIO(_.sendEmail(user, message))
  }

  // service definition
  trait UserDb {
    def insert(user: User): Task[Unit]
  }
  object UserDb {

    // layer - service implementation
    val liveLayer: ULayer[UserDb] = ZLayer.succeed(new UserDb {
      override def insert(user: User): Task[Unit] =
        ZIO.attempt {
          // can replace this with an actual DB SQL string
          println(s"[Database] insert into public.user values('${user.email}')")
        }
    })

    // accessor
    def insert(user: User): ZIO[UserDb, Throwable, Unit] =
      ZIO.serviceWithZIO(_.insert(user))
  }

  class UserSubscription(notifier: UserEmailer, userDb: UserDb) {
    def subscribe(user: User): Task[User] = {
      for {
        _ <- userDb.insert(user)
        _ <- notifier.sendEmail(user, "Welcome to Expedia")
      } yield user
    }
  }

  object UserSubscription {
    // service definition as a class

    def subscribe(user: User): ZIO[UserSubscription, Throwable, User] =
      ZIO.serviceWithZIO(_.subscribe(user))

    val liveLayer: ZLayer[UserDb with UserEmailer, Nothing, UserSubscription] =
      ZLayer {
        for {
          notifier <- ZIO.service[UserEmailer]
          userDb <- ZIO.service[UserDb]
        } yield new UserSubscription(notifier, userDb)

      }
  }

  /**
    Creating heavy apps involving services:
      - Interacting with storage layer
      - Business logic
      - Front facing API e.g through HTTP
      - Communicating with other services
    */
  //HORIZONTAL Composition
  val userBackendLayer: ZLayer[Any, Nothing, UserEmailer with UserDb] =
    UserEmailer.liveLayer ++ UserDb.liveLayer

  //VERTICAL Composition
  val userSubscriptionBackendLayer =
    userBackendLayer >>> UserSubscription.liveLayer

  val user = User("Abdhesh Kumar", "abdkumar@expediagroup.com")
  val message = "Welcome to ZIO library"

  def notifyUser() = {
    UserEmailer
      .sendEmail(user, message) //The kind of effect
      .provideLayer(
        //UserEmailer.liveLayer
        userBackendLayer
      ) //Provide the input for that effect to run ..Dependency Injection
      .exitCode
  }

  override def run = {
    UserSubscription
      .subscribe(user)
      .provide(
        UserEmailer.liveLayer,
        UserDb.liveLayer,
        UserSubscription.liveLayer
      )
  }

}
