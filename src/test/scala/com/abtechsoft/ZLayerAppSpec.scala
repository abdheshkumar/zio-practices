package com.abtechsoft

import zio._
import zio.test._
import zio.random.Random
import Assertion._
import com.abtechsoft.ZLayerApp.{History, History2, Names, Teams}
import zio.clock.Clock

object ZLayerAppSpec extends DefaultRunnableSpec {

  def namesTest =
    testM("names test") {
      for {
        name <- Names.randomName
      } yield {
        assert(ZLayerApp.firstNames.contains(name))(equalTo(true))
      }
    }

  def justTeamsTest =
    testM("small team test") {
      for {
        team <- Teams.pickTeam(1)
      } yield {
        assert(team.size)(equalTo(1))
      }
    }

  def inMyTeam =
    testM("combines names and teams") {
      for {
        name <- Names.randomName
        team <- Teams.pickTeam(5)
        _ =
          if (team.contains(name)) println("one of mine")
          else println("not mine")
      } yield assertCompletes
    }

  def wonLastYear =
    testM("won last year") {
      for {
        team <- Teams.pickTeam(5)
        _ <- History.wonLastYear(team)
      } yield assertCompletes
    }

  def wonLastYear2 =
    testM("won last year") {
      for {
        team <- Teams.pickTeam(5)
        _ <- History2.wonLastYear(team)
      } yield assertCompletes
    }

  val individually = suite("individually")(
    suite("needs Names")(
      namesTest
    ).provideCustomLayer(Names.live),
    suite("needs just Team")(
      justTeamsTest
    ).provideCustomLayer(Names.live >>> Teams.live),
    suite("needs Names and Teams")(
      inMyTeam
    ).provideCustomLayer(Names.live ++ (Names.live >>> Teams.live)),
    suite("needs History and Teams")(
      wonLastYear
    ).provideCustomLayerShared(
      (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live)
    ),
    suite("needs History2 and Teams")(
      wonLastYear2
    ).provideCustomLayerShared(
      (Names.live >>> Teams.live) ++ (((Names.live >>> Teams.live) ++ Clock.any) >>> History2.live)
    )
  )

  val altogether: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("all together")(
    suite("needs Names")(
      namesTest
    ),
    suite("needs just Team")(
      justTeamsTest
    ),
    suite("needs Names and Teams")(
      inMyTeam
    ),
    suite("needs History and Teams")(
      wonLastYear
    )
  ).provideCustomLayerShared(
    Names.live ++ (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live)
  )

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] =
  suite("All ZLayer")(altogether, individually)
}
