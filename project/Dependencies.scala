import sbt._

object Dependencies {

  object TestContainer {
    def testContainer(artifact: String): ModuleID =
      "com.dimafeng" %% artifact % "0.38.1" % Test

    val postgresqlContainer = testContainer("testcontainers-scala-postgresql")
    val all = Seq(postgresqlContainer)
  }

  object ZIO {
    def zioM(artifact: String): ModuleID = "dev.zio" %% artifact % "1.0.0"
    val zio = zioM("zio")
    val zioStreams = zioM("zio-streams")
    val zioMacros = zioM("zio-macros")
    val zioTest = zioM("zio-test") % Test
    val zioTestSbt = zioM("zio-test-sbt") % Test
    val zioInteropCats =
      "dev.zio" %% "zio-interop-cats" % "2.1.4.0"
    val zioLogging = "dev.zio" %% "zio-logging" % "0.3.1"
    val loggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % "0.3.2"
    val all = Seq(
      zio,
      zioStreams,
      zioMacros,
      zioInteropCats,
      zioLogging,
      zioTest,
      zioTestSbt,
      loggingSlf4j
    )
  }
  object FlyWay {
    def flyway(artifact: String) =
      "org.flywaydb" % artifact % "6.5.5"
    val flyWayCore = flyway("flyway-core")
    val driver = "org.postgresql" % "postgresql" % "42.2.14"
    val all = Seq(flyWayCore, driver)
  }

  object Http4s {
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% artifact % "0.21.4"
    val http4sServer = http4s("http4s-blaze-server")
    val http4sDsl = http4s("http4s-dsl")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce = http4s("http4s-circe")
    val all = Seq(http4sServer, http4sDsl, http4sClient, http4sCirce)
  }

  object Circe {
    def circe(artifact: String): ModuleID =
      "io.circe" %% artifact % "0.13.0"
    val circeCore = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser = circe("circe-parser")
    val all = Seq(circeCore, circeGeneric, circeParser)
  }

  object Doobie {
    def doobie(artifact: String): ModuleID =
      "org.tpolecat" %% artifact % "0.9.0"
    val core = doobie("doobie-core")
    val hikari = doobie("doobie-hikari")
    val postgres = doobie("doobie-postgres")
    val refined = doobie("doobie-refined")
    val quill = doobie("doobie-quill")
    val doobieH2 = doobie("doobie-h2")
    val quillJdbc = "io.getquill" %% "quill-jdbc" % "3.5.1"
    val h2 = "com.h2database" % "h2" % "1.4.200"
    val all: Seq[ModuleID] =
      Seq(core, hikari, refined, postgres, quill, h2, quillJdbc, doobieH2)
  }

  object Config {
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.12.3"
    val pureconfigRefined = "eu.timepit" %% "refined-pureconfig" % "0.9.15"
    val all = Seq(pureConfig, pureconfigRefined)
  }

  object Logback {
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
    val all = Seq(logback)
  }

}
