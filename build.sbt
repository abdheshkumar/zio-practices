import Dependencies._
lazy val root = project
  .in(file("."))
  .settings(
    name := "zio-practices",
    version := "0.1",
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq(
      //"-Xfatal-warnings",
      "-Ymacro-annotations"
    ),
    libraryDependencies ++=
      ZIO.all ++
        Http4s.all ++
        Circe.all ++
        Doobie.all ++
        Config.all ++
        Logback.all ++
        TestContainer.all ++
        FlyWay.all,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
