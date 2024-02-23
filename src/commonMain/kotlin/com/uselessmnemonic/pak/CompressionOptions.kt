package com.uselessmnemonic.pak

expect class CompressionOptions {
    val strategy: CompressionStrategy
    val level: Int
    val dictionary: ByteArray?
    constructor(strategy: CompressionStrategy, level: Int, dictionary: ByteArray? = null)
}

internal val DEFAULT_COMPRESSION_OPTIONS =
    CompressionOptions(CompressionStrategy.Z_DEFAULT_STRATEGY, CompressionLevel.Z_DEFAULT_COMPRESSION)
