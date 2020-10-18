package com.abtechsoft
import zio._
object ParallelismApp {
  case class InvalidData()
  def decrypt(data: Array[Byte]): ZIO[Any, Nothing, String] = ???
  def validate(data: Array[Byte]): ZIO[Any, InvalidData, Unit] = ???
  def program(data: Array[Byte]) = {
    for {
      fiber <- decrypt(data).fork
      _ <- validate(data)
      result <- fiber.join
    } yield result

  }
}
