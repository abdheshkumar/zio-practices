package effect

object Effect_02 {
  /*
Z[-R, +E, +A] - functional effect, which means that it is an immutable value that contains a description of a series of interactions with the outside world (database queries, calls to third-party APIs, etc.).
ZIO data type is the following
- Highly composable Because ZIO is based on functional programming principles, such as using pure functions and immutable values, it allows us to easily compose solutions to complex problems from simple building blocks.
- 100% asynchronous and non-blocking.
- Highly performant and concurrent: ZIO implements Fiber-based concurrency
R => Either[E, A]

This means that a effect:
● Needs a context of type R to run (this context can be anything: a connection to a database, a REST client , a configuration object, etc.).
● It may fail with an error of type E or it may complete successfully, returning a value of type A.
   */

  /*
Common aliases for the ZIO data type
Task[+A] = ZIO[Any, Throwable, A]: This means a Task[A] is a ZIO effect that:
  - Doesn’t require an environment to run (
             that’s why the R type is replaced by Any,
             meaning the effect will run no matter what we provide to it as an environment)
  - Can fail with a Throwable
  - Can succeed with an A

UIO[+A] = ZIO[Any, Nothing, A]: This means a UIO[A] is a ZIO effect that:
  - Doesn’t require an environment to run.
  - Can’t fail
  - Can succeed with an A

RIO[-R, +A] = ZIO[R, Throwable, A]: This means a RIO[R, A] is a ZIO effect that:
  - Requires an environment R to run
  - Can fail with a Throwable
  - Can succeed with an A

IO[+E, +A] = ZIO[Any, E, A]: This means a IO[E, A] is a ZIO effect that:
  - Doesn’t require an environment to run.
  - Can fail with an E
  - Can succeed with an A

URIO[-R, +A] = ZIO[R, Nothing, A]: This means a URIO[R, A] is a ZIO effect that:
  - Requires an environment R to run
  - Can’t fail
  - Can succeed with an A

   */
  /*
Better type:
1- ZIO’s has typed errors which help to write better applications.
The Advantages of ZIO, You can use a specific error type and ensure that consumers know exactly what errors they need to handle.
It helps handling the edge cases, if you don’t have type information when calling a specific effect you cannot know what you may need to handle

2- ZIO type is flat, making the operations on ZIO more accessible to developers. Flat structures are usually more adapted if you want a better understanding of the code.

ZIO’s Fibers over Threads are, when writing concurrent applications
1- Fibers are extremely lightweight compared to threads

ZIO is compositionality

ZManaged is a type that focuses on the composition of managed resources;
ZLayers : typed dependency injection for IO environments. For ZLayers, I like the type safe approach of dependency injection


ZIO is a developer-friendly scala library, it’s also ideal for newcomers to scala. They do not need to understand any concept from Category theory to get the most out of it (Like cats effect).
   */

}
