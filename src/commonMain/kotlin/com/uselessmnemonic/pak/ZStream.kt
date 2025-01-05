package com.uselessmnemonic.pak

/**
 * An interface which represents a Z-Lib ZStream object.
 *
 * This interface is implemented per-platform to take advantage of existing compression machinery. It conforms closely
 * to the zlib specification, encapsulating as many operations as is permissible across platforms.
 *
 * Implementations are expected to initialize the ZStream to a known state. In particular, any buffers should be
 * initialized to empty rather than null states, the [totalIn]/[totalOut] fields to zero, and the [adler] field to the
 * starting value of 1. After a stream is closed, it may be left in any arbitrary state undefined to the user as the
 * stream must not be used again.
 *
 * Though zlib communicates error conditions through return values, the target platform may instead elect to raise
 * exceptions. Implementations therefore must propagate exceptions or raise them in response to zlib error codes.
 */
interface ZStream : AutoCloseable {
    val availIn: UInt
    val availOut: UInt

    val totalIn: ULong
    val totalOut: ULong
    val adler: ULong

    fun setInput(buffer: ByteArray, indices: IntRange = buffer.indices)
    fun setOutput(buffer: ByteArray, indices: IntRange = buffer.indices)

    fun deflateInit(level: ZCompressionLevel): ZResult
    fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult
    fun deflateGetDictionaryLength(): UInt
    fun deflateGetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): IntRange
    fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): ZResult
    fun deflate(flush: ZFlush): ZResult
    fun deflateReset(): ZResult
    fun deflateEnd(): ZResult

    fun inflateInit(): ZResult
    fun inflateGetDictionaryLength(): UInt
    fun inflateGetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): IntRange
    fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange = dictionary.indices): ZResult
    fun inflate(flush: ZFlush): ZResult
    fun inflateReset(): ZResult
    fun inflateEnd(): ZResult

    override fun close()
}

/**
 * Creates the default ZStream for the host platform.
 */
expect fun ZStream(): ZStream
