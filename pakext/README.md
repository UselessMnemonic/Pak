# PakExt

`PakExt` is a Rust library that compliments Pak/JVM.

### Java FFM
Java does not allow assigning [heap segments](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/foreign/MemorySegment.html)
to structured memory. This is because heap segments cannot be represented with a stable memory address unless passed
into [critical](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/lang/foreign/Linker.Option.html#critical(boolean))
down-calls.

For this reason, it is impossible to reference a `byte[]` in a `z_stream` struct like any other buffer. Instead, this
library implements down-call wrappers for `zlib` which temporarily accept heap segments. While this step introduces
some overhead, it also allows `byte[]` objects to participate in the native world.

### Java JNI
In the future, `PakExt` will implement all the necessary routines required for JNI targets.
