package com.uselessmnemonic.pak

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * An implementation of ZStream which uses the Foreign Function and Memory API to bind ZLib.
 */
class JavaZStream : ZStream {
    private val zRef = ZStreamRef()

    override val availIn get() = zRef.availIn.toUInt()
    override val availOut get() = zRef.availOut.toUInt()
    override val totalIn get() = zRef.totalIn.toULong()
    override val totalOut get() = zRef.totalOut.toULong()
    override val adler get() = zRef.adler.toULong()

    private fun parseResult(result: Int): ZResult {
        return ZResult.entries.find { it.value == result }
            ?: ZError.entries.find { it.value == result }?.thrown(zRef.msg.takeIfNotNull()?.getString(0))
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    /**
     * Provides input data to the compression engine from the given memory segment.
     * Once set, the engine will continue to slice the same segment automatically until it is exhausted.
     *
     * @param buffer The input segment, with the desired buffer size already configured.
     */
    fun setInput(buffer: MemorySegment) {
        zRef.input = buffer
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        zRef.input = buffer.asMemorySegment(indices)
    }

    /**
     * Provides output space to the compression engine from the given memory segment.
     * Once set, the engine will fill as much output as is permitted.
     *
     * @param buffer The output segment, with the desired buffer size already configured.
     */
    fun setOutput(buffer: MemorySegment) {
        zRef.output = buffer
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        zRef.output = buffer.asMemorySegment(indices)
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef.deflateInit(level.value)
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        return Arena.ofConfined().use { arena ->
            val size = arena.allocate(ValueLayout.JAVA_INT, 0)
            val result = parseResult(
                zRef.deflateGetDictionary(MemorySegment.NULL, size)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.get(ValueLayout.JAVA_INT, 0).toUInt()
        }
    }

    override fun deflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        if (indices.isEmpty()) {
            return indices
        }

        val dictionary = dictionary.asMemorySegment(indices)
        val size = Arena.ofConfined().use { arena ->
            val size = arena.allocateFrom(ValueLayout.JAVA_INT, 0)
            val result = parseResult(
                zRef.deflateGetDictionary(dictionary, size)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            // always bound by [0, indices.last - indices.first + 1]
            // and therefore by [0, Int.MAX_VALUE]
            size.get(ValueLayout.JAVA_INT, 0)
        }

        if (size == 0) {
            return IntRange.EMPTY
        }
        return IntRange(indices.first, indices.first + size - 1)
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val slice = dictionary.asMemorySegment(indices)
        return parseResult(
            zRef.deflateSetDictionary(slice)
        )
    }

    override fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult {
        return parseResult(
            zRef.deflateParams(level.value, strategy.value)
        )
    }

    override fun deflate(flush: ZFlush): ZResult {
        return parseResult(
            zRef.deflate(flush.value)
        )
    }

    override fun deflateReset(): ZResult {
        return parseResult(
            zRef.deflateReset()
        )
    }

    override fun deflateEnd(): ZResult {
        return parseResult(
            zRef.deflateEnd()
        )
    }

    override fun inflateInit(): ZResult {
        return parseResult(
            zRef.inflateInit()
        )
    }

    override fun inflateGetDictionaryLength(): UInt {
        return Arena.ofConfined().use { arena ->
            val size = arena.allocateFrom(ValueLayout.JAVA_INT, 0)
            val result = parseResult(
                zRef.inflateGetDictionary(MemorySegment.NULL, size)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.get(ValueLayout.JAVA_INT, 0)
        }.toUInt()
    }

    override fun inflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        if (indices.isEmpty()) {
            return indices
        }

        val dictionary = dictionary.asMemorySegment(indices)
        val size = Arena.ofConfined().use { arena ->
            val size = arena.allocateFrom(ValueLayout.JAVA_INT, 0)
            val result = parseResult(
                zRef.inflateGetDictionary(dictionary, size)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            // always bound by [0, indices.last - indices.first + 1]
            // and therefore by [0, Int.MAX_VALUE]
            size.get(ValueLayout.JAVA_INT, 0)
        }

        if (size == 0) {
            return IntRange.EMPTY
        }
        return IntRange(indices.first, indices.first + size - 1)
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val slice = dictionary.asMemorySegment(indices)
        return parseResult(
            zRef.inflateSetDictionary(slice)
        )
    }

    override fun inflate(flush: ZFlush): ZResult {
        return parseResult(
            zRef.inflate(flush.value)
        )
    }

    override fun inflateReset(): ZResult {
        return parseResult(
            zRef.inflateReset()
        )
    }

    override fun inflateEnd(): ZResult {
        return parseResult(
            zRef.inflateEnd()
        )
    }

    override fun close() {
        zRef.close()
    }
}

actual fun ZStream(): ZStream = JavaZStream()
