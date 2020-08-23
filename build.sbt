import Dependencies.Libraries._
lazy val root = project
  .in(file("."))
  .settings(
    name := "zio-practices",
    version := "0.1",
    scalaVersion := "2.13.2",
    scalacOptions ++= Seq(
      //"-Xfatal-warnings",
      "-Ymacro-annotations"
    ),
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      zioMacros,
      zioInteropCats,
      zioLogging,
      http4sServer,
      http4sDsl,
      http4sClient,
      http4sCirce,
      circeCore,
      circeGeneric,
      circeParser,
      quillJdbc,
      doobieCore,
      doobieQuill,
      doobieH2,
      pureConfig,
      h2,
      logback,
      zioTestSbt
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
