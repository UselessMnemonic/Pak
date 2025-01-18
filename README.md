# Pak

`Pak` is a Kotlin Multiplatform library that wraps `zlib` in a safe and well-structured API.

### Kotlin/JVM
With the introduction of Java 22, the [Foreign Function and Memory interface](https://docs.oracle.com/en/java/javase/22/core/foreign-function-and-memory-api.html)
can be leveraged to utilize native `zlib` over the less flexible [java.util.zip](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/util/zip/package-summary.html)
package.

Pak/JVM embeds a Rust library located in the [`pak-rs`](./pak-rs) subproject. This library loads zlib and wraps it with
an interface designed for the Java world. Therefore, `zlib` must be installed on the host machine. A user may select a
specific implementation with the `ZLIB_LIBRARY` environment variable, otherwise a selection is made between `libz.so`,
`libz.dylib`, or `zlib1.dll`.

### Kotlin/Native
Kotlin/Native comes pre-configured with bindings to `zlib`, which `Pak` employs internally. Therefore, Pak/Native
hosts require their native `zlib` only. Depending on the host platform, a specific `zlib` implementation may be usable
over the default implementation.

### Kotlin/JS
Kotlin/JS is supported with the excellent [`pako`](https://github.com/nodeca/pako) zlib implementation. This allows Pak
to work in any JavaScript environment, with minimal overhead.
