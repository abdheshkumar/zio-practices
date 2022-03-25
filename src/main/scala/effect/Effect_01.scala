package effect

object Effect_01 {

  //A pure function must be total
  //1-
  object P1 {

    def divide(a: Int, b: Int): Int = a / b

    divide(5, 0)
  }

  object P2 {
    def divide(a: Int, b: Int): Option[Int] =
      if (b != 0) Some(a / b) else None
  }

  object P3 {
    def divide(a: Int, b: Int): Either[Exception, Int] =
      if (b == 0) Left(new Exception("b is zero")) else Right(a / b)
  }

  //A pure function must be deterministic and must depend only on its inputs
  object P4 {
    def generateRandomInt(): Int = (new scala.util.Random).nextInt
    //why this function is not deterministic
    generateRandomInt() // Result: -272770531
    generateRandomInt() // Result: 217937820

    //This function is not pure
    def add(a: Int, b: Int): Int = {
      println(s"Adding two integers: $a and $b")
      a + b
    }
  }
}
