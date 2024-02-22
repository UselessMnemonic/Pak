package com.uselessmnemonic.pak

expect class CompressionOptions {
    val strategy: CompressionStrategy
    val level: Int
    val dictionary: ByteArray?
    constructor(strategy: CompressionStrategy, level: Int, dictionary: ByteArray? = null)
}
