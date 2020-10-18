package com.abtechsoft

/**
  Combinators provide strong guarantees that make it easy to reason about concurrent programs:
• Race - the slower of two computations will be immediately interrupted
• Bracket - resource will always be released when computation is terminated
• Lock - computation will always be executed on specified executor

Try finally doesn't work with asynchronous or concurrent code

Resources are guaranteed to be acquired in correct order and released as soon as possible

Concurrent Data Structures
"Batteries included" library of concurrent data structures to solve any problem:
• Ref - concurrent state
• Promise - single element coordination between fibers
• Queue - multiple element coordination between fibers
• STM - full software transactional memory library

Layers Compose
Layers compose horizontally and vertically:
• Horizontally - Combine two layers with ++ to get a new layer that requires both of their inputs and produces both their outputs
• Vertically - Combine one layer that depends on another with >>> to get a new layer that takes the input to the first and produces the output of the second
  */
import zio._
object ConcurrencyApp {}
