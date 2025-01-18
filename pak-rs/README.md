# pak-rs

`pak-rs` is a Rust library that compliments Pak/JVM.

### Why
Java does not allow assigning [heap segments](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/foreign/MemorySegment.html)
to structured memory. This is because heap segments cannot be represented with a stable memory address, unless passed
into [critical](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/foreign/Linker.Option.html#critical(boolean))
down-calls.

For this reason, it is impossible to directly reference a `byte[]` in a `z_stream` struct, like any other arbitrary
buffer. Instead, this library implements wrappers for `zlib` which _can_ temporarily process heap segments. While this
step introduces some overhead, it is a tiny price to pay to avoid copying `byte[]` objects across the native boundary.
