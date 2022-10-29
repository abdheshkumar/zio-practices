import sbt._

object Dependencies {

  object TestContainer {
    def testContainer(artifact: String): ModuleID =
      "com.dimafeng" %% artifact % "0.39.12" % Test

    val postgresqlContainer = testContainer("testcontainers-scala-postgresql")
    val all = Seq(postgresqlContainer)
  }

  object ZIO {
    def zioM(artifact: String): ModuleID = "dev.zio" %% artifact % "1.0.13"
    val zio = zioM("zio")
    val zioStreams = zioM("zio-streams")
    val zioMacros = zioM("zio-macros")
    val zioTest = zioM("zio-test") % Test
    val zioTestSbt = zioM("zio-test-sbt") % Test

    val config = Seq(
      "dev.zio" %% "zio-config" % "3.0.2",
      "dev.zio" %% "zio-config-magnolia" % "3.0.2",
      "dev.zio" %% "zio-config-typesafe" % "3.0.2",
      "dev.zio" %% "zio-config-refined" % "3.0.2"
    )
    val kafka = "dev.zio" %% "zio-kafka" % "2.0.1"
    val zioInteropCats =
      "dev.zio" %% "zio-interop-cats" % "22.0.0.0"
    val zioLogging = "dev.zio" %% "zio-logging" % "2.1.2"
    val loggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % "2.1.2"
    val zioMagic = "io.github.kitlangton" %% "zio-magic" % "0.3.12"

    object Versions {
      val jaeger = "1.8.0"
      val sttp3 = "3.3.17"
      val opentelemetry = "1.10.0"
      val zipkin = "2.16.3"
      val zioHttp = "1.0.0.0-RC17"
      val zioJson = "0.1.5"
      val zioConfig = "2.0.0"
    }
    val opentelemetry = Seq(
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % Versions.sttp3,
      "com.softwaremill.sttp.client3" %% "zio-json" % Versions.sttp3,
      "io.d11" %% "zhttp" % Versions.zioHttp,
      "dev.zio" %% "zio-json" % Versions.zioJson,
      "io.opentelemetry" % "opentelemetry-exporter-jaeger" % Versions.opentelemetry,
      "dev.zio" %% "zio-opentelemetry" % "2.0.3"
    )
    val all = Seq(
      zio,
      zioStreams,
      zioMacros,
      zioInteropCats,
      zioLogging,
      zioTest,
      zioTestSbt,
      loggingSlf4j,
      zioMagic,
      kafka
    ) ++ config ++ opentelemetry
  }
  object FlyWay {
    def flyway(artifact: String) =
      "org.flywaydb" % artifact % "8.4.1"
    val flyWayCore = flyway("flyway-core")
    val driver = "org.postgresql" % "postgresql" % "42.3.1"
    val all = Seq(flyWayCore, driver)
  }

  object Http4s {
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% artifact % "0.23.7"
    val http4sServer = http4s("http4s-blaze-server")
    val http4sDsl = http4s("http4s-dsl")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce = http4s("http4s-circe")
    val all = Seq(http4sServer, http4sDsl, http4sClient, http4sCirce)
  }

  object Circe {
    def circe(artifact: String): ModuleID =
      "io.circe" %% artifact % "0.14.1"
    val circeCore = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser = circe("circe-parser")
    val all = Seq(circeCore, circeGeneric, circeParser)
  }

  object Doobie {
    def doobie(artifact: String): ModuleID =
      "org.tpolecat" %% artifact % "1.0.0-RC1"

    val core = doobie("doobie-core")
    val hikari = doobie("doobie-hikari")
    val postgres = doobie("doobie-postgres")
    val refined = doobie("doobie-refined")
    val quill = doobie("doobie-quill")
    val doobieH2 = doobie("doobie-h2")
    val quillJdbc = "io.getquill" %% "quill-jdbc" % "4.6.0"
    val h2 = "com.h2database" % "h2" % "2.1.214"
    val all: Seq[ModuleID] =
      Seq(core, hikari, refined, postgres, quill, h2, quillJdbc, doobieH2)
  }

  object Config {
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"
    val pureconfigRefined = "eu.timepit" %% "refined-pureconfig" % "0.9.28"
    val all = Seq(pureConfig, pureconfigRefined)
  }

  object Logback {
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.10"
    val all = Seq(logback)
  }

}
