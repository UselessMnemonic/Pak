# Pak

`Pak` is a Kotlin Multiplatform library that wraps `zlib` in a safe and well-structured API.

### Kotlin/JVM
With the introduction of Java 22, [Java FFM can be leveraged](https://docs.oracle.com/en/java/javase/22/core/foreign-function-and-memory-api.html)
to utilize native `zlib` over the less flexible [java.util.zip](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/util/zip/package-summary.html)
package.

Pak/JVM also ships with a pre-built library called [`pakext`](./pakext) which can be built for any host
platform that supports Java.

Pak/JVM requires `zlib` be installed on the host machine. A user may select the specific implementation with the
`ZLIB_LIBRARY` environment variable, otherwise a selection is made between `libz.so`, `libz.dylib`, or `zlib1.dll`.

### Kotlin/Native
Kotlin/Native comes pre-configured with bindings to `zlib`, which `Pak` employs internally. Therefore, Pak/Native
hosts require their native `zlib` only. Depending on the host platform, a specific `zlib` implementation may be usable
over the host implementation.

### Kotlin/JS
Kotlin/JS is planned for the future.
