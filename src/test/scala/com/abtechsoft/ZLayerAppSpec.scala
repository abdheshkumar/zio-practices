package com.abtechsoft

import com.abtechsoft.ZLayerApp.{History, History2, Names, Teams}
import zio.test.Assertion._
import zio.test._

object ZLayerAppSpec extends ZIOSpecDefault {

  def namesTest: Spec[Names, Nothing] =
    test("names test") {
      for {
        name <- Names.randomName
      } yield {
        assert(ZLayerApp.firstNames.contains(name))(equalTo(true))
      }
    }

  def justTeamsTest: Spec[Teams, Nothing] =
    test("small team test") {
      for {
        team <- Teams.pickTeam(1)
      } yield {
        assert(team.size)(equalTo(1))
      }
    }

  def inMyTeam: Spec[Teams with Names, Nothing] =
    test("combines names and teams") {
      for {
        name <- Names.randomName
        team <- Teams.pickTeam(5)
        _ =
          if (team.contains(name)) println("one of mine")
          else println("not mine")
      } yield assertCompletes
    }

  def wonLastYear =
    test("won last year") {
      for {
        team <- Teams.pickTeam(5)
        _ <- History.wonLastYear(team)
      } yield assertCompletes
    }

  def wonLastYear2 =
    test("won last year") {
      for {
        team <- Teams.pickTeam(5)
        _ <- History2.wonLastYear(team)
      } yield assertCompletes
    }

  val individually = suite("individually")(
    suite("needs Names")(
      namesTest
    ).provideLayer(Names.live),
    suite("needs just Team")(
      justTeamsTest
    ).provideLayer(Names.live >>> Teams.live),
    suite("needs Names and Teams")(
      inMyTeam
    ).provideLayer(Names.live ++ (Names.live >>> Teams.live)),
    suite("needs History and Teams")(
      wonLastYear
    ).provideLayer(
      (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live)
    ),
    suite("needs History2 and Teams")(
      wonLastYear2
    ).provideLayer(
      (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History2.live)
    )
  )

  val altogether = suite("all together")(
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
  ).provideLayer(
    Names.live ++ (Names.live >>> Teams.live) ++ (Names.live >>> Teams.live >>> History.live)
  )

  override def spec =
    suite("All ZLayer")(altogether, individually)
}
