package com.abtechsoft
import com.abtechsoft.TestContainer.Postgres
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.flywaydb.core.Flyway
import _root_.persistence.MainApp.transactor
import _root_.persistence.User
import _root_.persistence.User.UserService
import org.testcontainers.utility.DockerImageName
import zio.{ZIO, _}
import zio.blocking.{Blocking, effectBlocking}
import zio.clock.nanoTime
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object MyPostgresIntegrationSpec extends DefaultRunnableSpec {

  val postgresLayer = Blocking.live >>> TestContainer.postgres()
  val userService: RLayer[Any, User.UserService] =
    Blocking.live >>> (transactor >>> User.live)
  val testEnv =
    zio.test.environment.testEnvironment ++ postgresLayer ++ userService

  def testA: ZSpec[UserService, Throwable] = {
    testM("Can compile") {
      (for {
        user <- User.create(User(1, "testUser"))
        result <- User.find(user.id)
      } yield assert(result)(equalTo(Some(user))) &&
        assert(result.get.id)(equalTo(1)))
    }
  }

  val postgresSpec: ZSpec[
    Blocking with Postgres with _root_.zio.test.environment.TestEnvironment,
    Object
  ] =
    suite("Postgres integration second") {
      testM("Can create and fetch a customer") {
        assertM(nanoTime)(isGreaterThanEqualTo(0L))
      }
    }.provideCustomLayer(testEnv).mapError(TestFailure.fail) @@ MigrationAspects
      .migrate(
        "customers",
        "filesystem:src/customers/resources/db/migration"
      )

  def spec =
    suite("All")(
      suite("Postgres integration")(
        testM("Can create and fetch a customer") {
          def program: ZIO[UserService, Throwable, Option[User]] = {
            for {
              user <- User.create(User(2, "testUser"))
              result <- User.find(user.id)
            } yield result
          }
          val result = program.provideCustomLayer(testEnv)
          assertM(result)(equalTo(Option.empty[User]))
        }
      ),
      suite("sa")(testA)
        .provideCustomLayer(testEnv)
        .mapError(TestFailure.fail) /* @@ MigrationAspects.migrate("","")*/,
      suite("Postgres integration second") {
        testM("Can create and fetch a customer") {
          assertM(nanoTime)(isGreaterThanEqualTo(0L))
        }
      } @@ sequential
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
          dockerImageNameOverride = imageName.map(DockerImageName.parse)
        )
        container.start()
        container
      }.orDie
    }(container => effectBlocking(container.stop()).orDie).toLayer
}

object MigrationAspects {
  def migrate(
      schema: String,
      paths: String*
  ): TestAspect[Nothing, Blocking with Postgres, Nothing, Any] = {
    val migration = for {
      pg <- ZIO.service[PostgreSQLContainer]
      _ <- runMigration(pg.jdbcUrl, pg.username, pg.password, schema, paths: _*)
    } yield ()

    before(migration.orDie)
  }

  private def runMigration(
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
    }
}
