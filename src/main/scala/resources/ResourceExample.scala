package resources

import cats.effect.{ExitCode, IO, IOApp, Resource}

object ResourceExample extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program.use(_ => IO.unit).map(_ => ExitCode.Success)

  def program: Resource[IO, Unit] =
    for {
      sqs <- Resource.fromAutoCloseable(IO(new Sqs))
      dynamoDB <- Resource.fromAutoCloseable(IO(new DynamoDB))
      _ <- Resource.eval(Service.businessLogic(dynamoDB, sqs))
    } yield ()

  object Service {
    def businessLogic(dynamo: DynamoDB, sqs: Sqs): IO[Unit] =
      for {
        _ <- IO(dynamo.use)
        _ <- IO(sqs.use)
      } yield ()
  }
}
