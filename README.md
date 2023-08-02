# Enhanced Read Write Lock

This is a toy implementation of Read-Write lock using Java monitor. Basically, it improves the concurrency over the standard Read-Write lock.

I've defined a `Set` which uses this `lock`. It allows multiple readers to go in their critical section at the same time but only allows one writer to enter its critical section at a given time. This lock inreases the concurrency by locking specific parts of the underlying array of the `Set` and allows safe interleaving of read and write accesses. This is why I call it enhanced.

The `model.pml` file contains the `Promela` modeling used to test the correctness of this `lock`.
