package resources

class HttpServer extends AutoCloseable {
  println("Opening Http server")
  def use(): Unit = ()
  def close(): Unit = println("Closing Http server")
}

class DynamoDB extends AutoCloseable {
  println("Opening DB connection")
  def use(): Unit = ()
  def close(): Unit = println("Closing DB")
}

class Sqs extends AutoCloseable {
  println("Opening MQ connection")
  def use(): Unit = ()
  def close(): Unit = println("Closing MQ")
}
