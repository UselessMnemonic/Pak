package com.uselessmnemonic.pak

actual class CompressionOptions actual constructor(
    actual val strategy: CompressionStrategy,
    actual val level: Int,
    actual val dictionary: ByteArray?
)
