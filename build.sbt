val ZIO = "1.0.0-RC19-2"
lazy val root = project
  .in(file("."))
  .settings(
    name := "zio-practices",
    version := "0.1",
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZIO,
      "dev.zio" %% "zio-test" % ZIO % Test,
      "dev.zio" %% "zio-test-sbt" % ZIO % Test
    )
  )
