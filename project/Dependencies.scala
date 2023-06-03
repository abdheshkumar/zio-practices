import sbt._

object Dependencies {

  object TestContainer {
    val postgresqlContainer =
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.40.16" % Test
    val all = Seq(postgresqlContainer)
  }

  object ZIO {
    private val zioVersion = "2.0.14"
    val zio = "dev.zio" %% "zio" % zioVersion
    val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
    val zioTest = "dev.zio" %% "zio-test" % zioVersion % Test
    val zioTestSbt = "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % zioVersion % Test

    private val zioConfigVersion = "3.0.7"
    val config = Seq(
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-refined" % zioConfigVersion
    )
    val kafka = "dev.zio" %% "zio-kafka" % "2.0.7"
    val zioInteropCats =
      "dev.zio" %% "zio-interop-cats" % "3.3.0"
    private val zioLoggingVersion = "2.1.13"
    val zioLogging = "dev.zio" %% "zio-logging" % zioLoggingVersion
    val loggingSlf4j = "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion

    object Versions {
      val jaeger = "1.8.0"
      val opentelemetry = "1.19.0"
      val zipkin = "2.16.3"
      val zioHttp = "2.0.0-RC11"
      val zioJson = "0.3.0"
      val zioConfig = "2.0.0"
      val zioOpentelemetry = "2.0.3"
    }
    val opentelemetry = Seq(
      "io.d11" %% "zhttp" % Versions.zioHttp,
      "dev.zio" %% "zio-json" % Versions.zioJson,
      "io.opentelemetry" % "opentelemetry-exporter-jaeger" % Versions.opentelemetry,
      "dev.zio" %% "zio-opentelemetry" % Versions.zioOpentelemetry
    )
    val all = Seq(
      zio,
      zioStreams,
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
    val flyWayCore = "org.flywaydb" % "flyway-core" % "9.19.1"
    val driver = "org.postgresql" % "postgresql" % "42.6.0"
    val all = Seq(flyWayCore, driver)
  }

  object Http4s {
    private val http4sVersion = "0.23.19"
    private val http4sBlazeVersion = "0.23.15"
    
    val http4sServer = "org.http4s" %% "http4s-blaze-server" % http4sBlazeVersion
    val http4sClient = "org.http4s" %% "http4s-blaze-client" % http4sBlazeVersion
    val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
    val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
    val all = Seq(http4sServer, http4sDsl, http4sClient, http4sCirce)
  }

  object Circe {
    private val circeVersion = "0.14.5"
    val circeCore = "io.circe" %% "circe-core" % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
    val all = Seq(circeCore, circeGeneric, circeParser)
  }

  object Doobie {
    private val doobieVersion = "1.0.0-RC2"
    val core = "org.tpolecat" %% "doobie-core" % doobieVersion
    val hikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
    val postgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion
    val refined = "org.tpolecat" %% "doobie-refined" % doobieVersion
    val quill = "io.getquill" %% "quill-doobie" % "4.6.0"
    val doobieH2 = "org.tpolecat" %% "doobie-h2" % doobieVersion
    val quillJdbc = "io.getquill" %% "quill-jdbc" % "4.6.1"
    val h2 = "com.h2database" % "h2" % "2.1.214"
    val all: Seq[ModuleID] =
      Seq(core, hikari, refined, postgres, quill, h2, quillJdbc, doobieH2)
  }

  object Config {
    val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.4"
    val pureconfigRefined = "eu.timepit" %% "refined-pureconfig" % "0.10.3"
    val all = Seq(pureConfig, pureconfigRefined)
  }

  object Logback {
    val logback = "ch.qos.logback" % "logback-classic" % "1.4.7"
    val all = Seq(logback)
  }

}
