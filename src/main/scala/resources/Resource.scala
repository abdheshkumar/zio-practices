package resources

trait Resource[R] { outer =>
  def use[U](f: R => U): U
  def flatMap[B](mapping: R => Resource[B]): Resource[B] =
    new Resource[B] {
      override def use[U](f: B => U): U =
        outer.use { res1 =>
          mapping(res1).use { res2 =>
            f(res2)
          }
        }
    }
  def map[B](mapping: R => B): Resource[B] =
    new Resource[B] {
      override def use[U](f: B => U): U = outer.use(a => f(mapping(a)))
    }
}

object Resource {
  def pure[R](r: R): Resource[R] = Resource.make(r)(_ => ())
  def make[R](acquire: => R)(close: R => Unit): Resource[R] =
    new Resource[R] {
      override def use[U](f: R => U): U = {
        val resource = acquire
        try {
          f(resource)
        } finally {
          close(resource)
        }
      }
    }
}
