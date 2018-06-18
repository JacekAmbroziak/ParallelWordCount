# Parallel Word Count

The project is an opportunity to learn about alternative implementations of text tokenization
as well as strategies to parallelize word count for multi core processors.

## What's in the repository

This is an SBT 1.0, Java 8 project; you need sbt 1.0 to compile, build and run the project and its tests.
Once a fat jar is created w/ sbt assemble, you can run the code anywhere. 

The core multithreaded word counting functionality is implemented in `com.jacek.wordcount.ForkJoinWordCounting`
supported by classes `WordCounter`, `Core` and `Punctuation` in the same package.
Several other classes contain alternative implementations of tokenization and counting for experimentation and comparison.

`com.jacek.wordcount.Main` assumes a list of text files as program arguments
 
`com.jacek.wordcount.MainDirExt` assumes 2 arguments: a root dir to search for text files and an extension such as `txt` 

### How to run

1. it is easy to run all classes from Intellij IDEA
2. `sbt "testOnly com.jacek.wordcount.PerformanceComparison"`
3. `sbt "testOnly com.jacek.wordcount.TokenizationPerformanceComparison"`
4. otherwise build a fat jar with `sbt assembly`, then
   eg. `java -cp /tmp/sbt/WordCount/scala-2.12/WordCount-assembly-0.2.jar com.jacek.wordcount.MainDirExt /opt/projects/WordCount .java`


## Text tokenization

We have compared four different implementations of splitting lines into whitespace separated tokens,
stripped of leading and trailing punctuation characters.
The code is in test dir's `TokenizationSpeedTest` class.

The short and elegant way using Java streams and a powerful regex turns out to be 6 times slower
than the solution using `StringTokenizer` that we have finally chosen.

Using regex just for whitespace tokenization and a hand coded method for cleaning tokens
of surrounding punctuation is twice as fast as the do-it-all regex.

Moving away from regex to the `StringTokenizer` is 3 times faster still and is the performance winner.

Strangely, the fully hand written WS tokenization is NOT faster...

java.text.BreakIterator (used in Paul Butcher's book on concurrency) is slow,
similar to regex based solution.

Given more time one could also add StreamTokenizer to the comparison.  

## Token counting

While a HashMap of Integers (or Longs) comes to mind first as a natural and simple choice for counting
occurrences of unique, cleaned and lowercased tokens, it can be improved upon by using
custom, mutable `Counter` class. We do not need Integer's full generality when
all we want is counter incrementation. When all the counting and merging is finished
we can then export data using immutable data structures.
With `Counter` we avoid excessive Integer object creation, rehashing and garbage collection.
The mutable `WordCounter` class is supposed to be touched by serial code only.

In addition, we have augmented the `WordCount` class with data on batch and merge performance.

When all the counting is done, one can use WordCounter's toMap method to get the count data
as an ImmutableMap of Integers.

## Parallelization

This is the most interesting aspect of the project!

We have chosen to apply the beautiful and powerful `ForkJoin` framework, a part of Java's concurrent utilities
since Java 7. `ForkJoin` does exactly what we need while hiding all complexity from users
of it's API, who do not need to concern themselves with explicit Thread management or synchronization.

One simply needs to implement a `RecursiveTask` implementing a `compute` method to return result value.
If input given to a task is small (by some conditional logic), the `compute` method will work
on the input directly, in its current Thread. Otherwise, it will split its task into two (or more) smaller 
tasks. It can then submit one of these tasks back to the `ForkJoin` framework for asynchronous, concurrent execution,
(with `fork`)
while continuing to work on the second subtask on its current Thread (with recursively called `compute`).
The framework will block on calls awaiting results from subtasks, allowing the Thread to do something else
where progress can be made. When results from subtasks are all available, execution of the main task
can continue, typically merging results and returning final value.
Since both tokenization, token counting, and count merging all are executed by serial code in the
`RecursiveTask` (`CountingTask`) we are free to use faster, mutable data structures.

Recursive splitting of tasks into smaller subtasks, effectively creates a binary tree of tasks.
Tokenization and token counting happen in parallel in all the leaves of that tree,
while counter merging happens in internal nodes. Crucially, merging of partial results, which is potentially costly, is also
performed concurrently on each level of the task tree. The height of the tree becomes an important component of the solution's
performance and part of the critical path.

The beauty of the `ForkJoin` framework lies in the simple and powerful abstraction it exports, and in the ingenuity
in how a fixed number of Threads in the `ForkJoinPool` (say 8) collaborate behind the scenes to make progress
as much as possible executing tasks in the task tree containing any number of nodes.

### Comparison

We have compared the performance of single threaded, linear token counting, with `ForkJoin` and also with 
a simple way of using `ExecutorService` to split initial initial into `noOfThreads==8` 'equal' parts,
performing them concurrently, and finishing with 7 partial results merges on the main thread.

This method performs in the same ballpark as the chosen `ForkJoin` approach. As our test processes
a few thousand text files in Linux kernel sources, the bottleneck might be shipping all the files' data
from RAM to the CPU. Both parallelization approaches only bring about 3x speedup over single-threaded linear counting.

We experimented with making parallel tasks do more CPU intensive work, like repeated SHA hashing inspired by
Blockchain's Proof-of-Work; then the speedup comes closer to 4x, the number of cores on our machine.
