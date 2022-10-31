package com.abtechsoft
import zio._
object ZLayerApp {

  val firstNames = Vector("Ed", "Jane", "Joe", "Linda", "Sue", "Tim", "Tom")

  trait Names {
    def randomName: UIO[String]
  }
  object Names {

    def namesImpl: Names =
      new Names {
        println(s"created namesImpl")
        def randomName = {
          for {
            random <- ZIO.random
            a <- random.nextIntBounded(firstNames.size).map(firstNames(_))
          } yield a

        }
      }

    val live: ZLayer[Any, Nothing, Names] = ZLayer.succeed(namesImpl)

    def randomName: ZIO[Names, Nothing, String] =
      ZIO.serviceWithZIO[Names](_.randomName)
  }

  trait Teams {
    def pickTeam(size: Int): UIO[Set[String]]
  }

  object Teams {

    def teamsImpl(names: Names): Teams =
      new Teams {
        def pickTeam(size: Int) =
          ZIO
            .collectAll(0.until(size).map { _ => names.randomName })
            .map(_.toSet) // yeah I know team could have < size!
      }

    val live: ZLayer[Names, Nothing, Teams] =
      ZLayer {
        for {
          names <- ZIO.service[Names]
        } yield teamsImpl(names)
      }

    def pickTeam(size: Int): ZIO[Teams, Nothing, Set[String]] =
      ZIO.serviceWithZIO[Teams](_.pickTeam(size))
  }

  trait History {
    def wonLastYear(team: Set[String]): Boolean
  }

  object History {

    def historyImpl(lastYearsWinners: Set[String]): History =
      new History {
        def wonLastYear(team: Set[String]) = lastYearsWinners == team
      }

    val live: ZLayer[Teams, Nothing, History] = ZLayer {
      ZIO
        .service[Teams]
        .flatMap(teams => teams.pickTeam(5).map(nt => historyImpl(nt)))
    }
    def wonLastYear(team: Set[String]) =
      ZIO.serviceWith[History](_.wonLastYear(team))
  }

  trait History2 {
    def wonLastYear(team: Set[String]): Boolean
  }
  object History2 {

    def history2Impl(lastYearsWinners: Set[String], lastYear: Long): History2 =
      new History2 {
        def wonLastYear(team: Set[String]) = {
          val _ = lastYear
          lastYearsWinners == team
        }
      }

    val live: ZLayer[Teams, Nothing, History2] = ZLayer {
      for {
        someTime <- ZIO.clock.flatMap(_.nanoTime)
        team <- Teams.pickTeam(5)
      } yield history2Impl(team, someTime)
    }
    def wonLastYear(team: Set[String]) =
      ZIO.serviceWith[History2](_.wonLastYear(team))
  }
}
