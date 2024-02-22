package com.uselessmnemonic.pak

actual class CompressionOptions {
    actual val strategy: CompressionStrategy
    actual val level: Int
    actual val dictionary: ByteArray?
    val windowBits: Int
    actual constructor(strategy: CompressionStrategy, level: Int, dictionary: ByteArray?):
            this(strategy, level, -1, dictionary)
    constructor(strategy: CompressionStrategy, level: Int, windowBits: Int, dictionary: ByteArray? = null) {
        this.strategy = strategy
        this.level = level
        this.windowBits = windowBits
        this.dictionary = dictionary
    }
}
