package com.abtechsoft
import zio._
import zio.clock.Clock
import zio.random.Random
object ZLayerApp {


  type Names = Has[Names.Service]
  type Teams = Has[Teams.Service]
  type History = Has[History.Service]
  type History2 = Has[History2.Service]
  val firstNames = Vector("Ed", "Jane", "Joe", "Linda", "Sue", "Tim", "Tom")

  object Names {
    trait Service {
      def randomName: UIO[String]
    }

    def namesImpl(random: Random.Service): Service =
      new Names.Service {
        println(s"created namesImpl")
        def randomName =
          random.nextIntBounded(firstNames.size).map(firstNames(_))
      }

    val live: ZLayer[Random, Nothing, Names] =
      ZLayer.fromService(namesImpl)

    def randomName = ZIO.accessM[Names](_.get.randomName)
  }

  object Teams {
    trait Service {
      def pickTeam(size: Int): UIO[Set[String]]
    }

    def teamsImpl(names: Names.Service): Service =
      new Service {
        def pickTeam(size: Int) =
          ZIO
            .collectAll(0.until(size).map { _ => names.randomName })
            .map(_.toSet) // yeah I know team could have < size!
      }

    val live: ZLayer[Names, Nothing, Teams] =
      ZLayer.fromService(teamsImpl)

    def pickTeam(size: Int): ZIO[Teams, Nothing, Set[String]] =
      ZIO.accessM[Teams](_.get.pickTeam(size))
  }

  object History {

    trait Service {
      def wonLastYear(team: Set[String]): Boolean
    }

    def historyImpl(lastYearsWinners: Set[String]): Service =
      new Service {
        def wonLastYear(team: Set[String]) = lastYearsWinners == team
      }

    val live: ZLayer[Teams, Nothing, History] = ZLayer.fromServiceM { teams =>
      teams.pickTeam(5).map(nt => historyImpl(nt))
    }
    def wonLastYear(team: Set[String]) =
      ZIO.access[History](_.get.wonLastYear(team))
  }

  object History2 {

    trait Service {
      def wonLastYear(team: Set[String]): Boolean
    }

    def history2Impl(lastYearsWinners: Set[String], lastYear: Long): Service =
      new Service {
        def wonLastYear(team: Set[String]) = {
          val _ = lastYear
          lastYearsWinners == team
        }
      }

    val live: ZLayer[Clock with Teams, Nothing, History2] = ZLayer.fromEffect {
      for {
        someTime <- ZIO.accessM[Clock](_.get.nanoTime)
        team <- Teams.pickTeam(5)
      } yield history2Impl(team, someTime)
    }
    def wonLastYear(team: Set[String]) =
      ZIO.access[History2](_.get.wonLastYear(team))
  }
}
