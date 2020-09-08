package com.abtechsoft
import com.abtechsoft.TestContainer.Postgres
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.flywaydb.core.Flyway
import persistence.CustomerService
import persistence.MainApp.transactor
import persistence.data.User
import zio.{ZIO, _}
import zio.blocking.{Blocking, effectBlocking}
import zio.clock.nanoTime
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.test.environment.TestEnvironment

object MyPostgresIntegrationSpec extends DefaultRunnableSpec {

  val postgresLayer: ULayer[Postgres] =
    Blocking.live >>> TestContainer.postgres()

  val userService: RLayer[Any, CustomerService] =
    Blocking.live >>> (transactor >>> CustomerService.live)

  val app: ZLayer[Any, Throwable, Postgres with CustomerService] =
    postgresLayer ++ userService

  val testEnv: ZLayer[
    Any,
    Throwable,
    _root_.zio.test.environment.TestEnvironment with Postgres with CustomerService
  ] =
    zio.test.environment.testEnvironment ++ app

  def testA: ZSpec[CustomerService, Throwable] = {
    testM("Can compile") {
      (for {
        user <- CustomerService.create(User("testUser", "testPassword"))
        result <- CustomerService.find(user.username)
      } yield assert(result)(equalTo(Some(user))) &&
        assert(result.get.username)(equalTo("testUser")))
    }
  }
  def spec =
    suite("All")(
      suite("Postgres integration")(
        testM("Can create and fetch a customer") {
          def program: ZIO[CustomerService, Throwable, Option[User]] = {
            for {
              user <- CustomerService.create(User("testUser", "testPassword"))
              result <- CustomerService.find(user.username)
            } yield result
          }
          val result = program.provideCustomLayer(app)
          assertM(result)(equalTo(Option.empty[User]))
        }
      ),
      suite("sa")(testA)
        .provideCustomLayer(userService ++ zio.test.environment.testEnvironment)
        .mapError(TestFailure.fail)
    )
}

object TestContainer {
  type Postgres = Has[PostgreSQLContainer]

  def postgres(
      imageName: Option[String] = Some("postgres")
  ): ZLayer[Blocking, Nothing, Postgres] =
    ZManaged.make {
      effectBlocking {
        val container = new PostgreSQLContainer(
          dockerImageNameOverride = imageName
        )
        container.start()
        container
      }.orDie
    }(container => effectBlocking(container.stop()).orDie).toLayer
}

object MigrationAspects {
  def migrate(): TestAspect[Nothing, Has[PostgreSQLContainer], Nothing, Any] = {
    val migration = for {
      _ <- ZIO.service[PostgreSQLContainer]
      //_ <- runMigration(pg.jdbcUrl, pg.username, pg.password, schema, paths: _*)
    } yield ()

    before(migration)
  }

  /*  private def runMigration(
      url: String,
      username: String,
      password: String,
      schema: String,
      locations: String*
  ) =
    effectBlocking {
      Flyway
        .configure()
        .dataSource(url, username, password)
        .schemas(schema)
        .locations(locations: _*)
        .load()
        .migrate()
    }*/
}
