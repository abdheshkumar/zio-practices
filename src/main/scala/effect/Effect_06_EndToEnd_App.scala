package effect

import effect.Effect_06_EndToEnd_App.UserDb.UserDbEnv
import effect.Effect_06_EndToEnd_App.UserEmailer.UserEmailerEnv
import effect.Effect_06_EndToEnd_App.UserSubscription.UserSubscriptionEnv
import zio.{ExitCode, Has, Task, ULayer, URIO, ZIO, ZLayer}
import zio.magic._

object Effect_06_EndToEnd_App extends zio.App {


  case class User(name: String, email: String)

  object UserEmailer {
    // type alias
    type UserEmailerEnv = Has[UserEmailer.Service]

    // service definition
    trait Service {
      def sendEmail(
                     user: User,
                     message: String
                   ): Task[Unit] // ZIO[Any, Throwable, Unit]
    }

    // layer; includes service implementation
    val liveLayer: ZLayer[Any, Nothing, UserEmailerEnv] =
      ZLayer.succeed(new Service {
        override def sendEmail(user: User, message: String): Task[Unit] =
          Task {
            println(s"[User emailer] sending '$message' to ${user.email}")
          }
      })

    // front-facing API, aka "accessor"
    def sendEmail(
                   user: User,
                   message: String
                 ): ZIO[UserEmailerEnv, Throwable, Unit] =
      ZIO.accessM(_.get.sendEmail(user, message))
  }

  object UserDb {
    // type alias
    type UserDbEnv = Has[UserDb.Service]

    // service definition
    trait Service {
      def insert(user: User): Task[Unit]
    }

    // layer - service implementation
    val liveLayer: ULayer[UserDbEnv] = ZLayer.succeed(new Service {
      override def insert(user: User): Task[Unit] =
        Task {
          // can replace this with an actual DB SQL string
          println(s"[Database] insert into public.user values('${user.email}')")
        }
    })

    // accessor
    def insert(user: User): ZIO[UserDbEnv, Throwable, Unit] =
      ZIO.accessM(_.get.insert(user))
  }

  object UserSubscription {
    // type alias
    type UserSubscriptionEnv = Has[UserSubscription.Service]

    // service definition as a class
    class Service(notifier: UserEmailer.Service, userDb: UserDb.Service) {
      def subscribe(user: User): Task[User] = {
        for {
          _ <- userDb.insert(user)
          _ <- notifier.sendEmail(user, "Welcome to Expedia")
        } yield user
      }
    }

    def subscribe(user: User): ZIO[UserSubscriptionEnv, Throwable, User] =
      ZIO.accessM(_.get.subscribe(user))

    val liveLayer
    : ZLayer[UserEmailerEnv with UserDbEnv, Nothing, UserSubscriptionEnv] =
      ZLayer.fromServices[
        UserEmailer.Service,
        UserDb.Service,
        UserSubscription.Service
      ] { (notifier, userDb) =>
        new Service(notifier, userDb)
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
  val userBackendLayer: ZLayer[Any, Nothing, UserEmailerEnv with UserDbEnv] =
    UserEmailer.liveLayer ++ UserDb.liveLayer

  //VERTICAL Composition
  val userSubscriptionBackendLayer: ZLayer[Any, Nothing, UserSubscriptionEnv] =
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

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val l = ZLayer.wire[UserSubscriptionEnv](
      UserSubscription.liveLayer,
      UserEmailer.liveLayer,
      UserDb.liveLayer
    )
    UserSubscription
      .subscribe(user)
      .provideLayer(l)
      .exitCode
  }

}
