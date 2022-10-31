import sbt._

object Dependencies {

  object TestContainer {
    def testContainer(artifact: String): ModuleID =
      "com.dimafeng" %% artifact % "0.39.12" % Test

    val postgresqlContainer = testContainer("testcontainers-scala-postgresql")
    val all = Seq(postgresqlContainer)
  }

  object ZIO {
    def zioM(artifact: String): ModuleID = "dev.zio" %% artifact % "2.0.2"
    val zio = zioM("zio")
    val zioStreams = zioM("zio-streams")
    val zioMacros = zioM("zio-macros")
    val zioTest = zioM("zio-test") % Test
    val zioTestSbt = zioM("zio-test-sbt") % Test
    val zioTestMagnolia=  zioM("zio-test-magnolia" )% Test

    val config = Seq(
      "dev.zio" %% "zio-config" % "3.0.2",
      "dev.zio" %% "zio-config-magnolia" % "3.0.2",
      "dev.zio" %% "zio-config-typesafe" % "3.0.2",
      "dev.zio" %% "zio-config-refined" % "3.0.2"
    )
    val kafka = "dev.zio" %% "zio-kafka" % "2.0.1"
    val zioInteropCats =
      "dev.zio" %% "zio-interop-cats" % "3.3.0"
    val zioLogging = "dev.zio" %% "zio-logging" % "2.1.2"
    val loggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % "2.1.3"

    object Versions {
      val jaeger = "1.8.0"
      val opentelemetry = "1.19.0"
      val zipkin = "2.16.3"
      val zioHttp = "2.0.0-RC11"
      val zioJson = "0.3.0"
      val zioConfig = "2.0.0"
    }
    val opentelemetry = Seq(
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
      zioTestMagnolia,
      loggingSlf4j,
      kafka
    ) ++ config ++ opentelemetry
  }
  object FlyWay {
    def flyway(artifact: String) =
      "org.flywaydb" % artifact % "8.4.1"
    val flyWayCore = flyway("flyway-core")
    val driver = "org.postgresql" % "postgresql" % "42.5.0"
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
      "org.tpolecat" %% artifact % "1.0.0-RC2"

    val core = doobie("doobie-core")
    val hikari = doobie("doobie-hikari")
    val postgres = doobie("doobie-postgres")
    val refined = doobie("doobie-refined")
    val quill = "io.getquill" %% "quill-doobie" % "4.6.0"
    val doobieH2 = doobie("doobie-h2")
    val quillJdbc = "io.getquill" %% "quill-jdbc" % "4.6.0"
    val h2 = "com.h2database" % "h2" % "2.1.214"
    val all: Seq[ModuleID] =
      Seq(core, hikari, refined, postgres, quill, h2, quillJdbc, doobieH2)
  }

  object Config {
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"
    val pureconfigRefined = "eu.timepit" %% "refined-pureconfig" % "0.10.1"
    val all = Seq(pureConfig, pureconfigRefined)
  }

  object Logback {
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.4"
    val all = Seq(logback)
  }

}
