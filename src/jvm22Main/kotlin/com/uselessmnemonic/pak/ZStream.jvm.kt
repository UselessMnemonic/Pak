package com.uselessmnemonic.pak

import java.lang.foreign.MemorySegment
import kotlin.IllegalArgumentException
import kotlin.IllegalStateException

/**
 * An implementation of ZStream which uses the Foreign Function and Memory API.
 */
class JavaZStream : ZStream {
    private val zRef = ZStreamRefImpl()

    override val availIn get() = zRef.availIn.toUInt()
    override val availOut get() = zRef.availOut.toUInt()
    override val totalIn get() = zRef.totalIn.toULong()
    override val totalOut get() = zRef.totalOut.toULong()
    override val adler get() = zRef.adler.toULong()

    private fun parseResult(result: Int): ZResult {
        return ZResult.entries.find { it.value == result } ?: throwError(result)
    }

    private fun throwError(result: Int): Nothing {
        return ZError.entries.find { it.value == result }?.thrown(zRef.msg)
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    /**
     * Provides input data to the compression engine from the given memory segment.
     * Once set, the engine will continue to slice the same segment automatically until it is exhausted.
     *
     * @param buffer The input segment, with the desired buffer size already configured.
     */
    fun setInput(buffer: MemorySegment) {
        zRef.setInput(buffer)
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        zRef.setInput(buffer, indices.first, indices.last - indices.first + 1)
    }

    /**
     * Provides output space to the compression engine from the given memory segment.
     * Once set, the engine will fill as much output as is permitted.
     *
     * @param buffer The output segment, with the desired buffer size already configured.
     */
    fun setOutput(buffer: MemorySegment) {
        zRef.setOutput(buffer)
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        zRef.setOutput(buffer, indices.first, indices.last - indices.first + 1)
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef.deflateInit(level.value)
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        val result = zRef.deflateGetDictionary(MemorySegment.NULL)
        if (result < 0) {
            throwError(result)
        }
        return result.toUInt()
    }

    /**
     * Returns the sliding dictionary being maintained by [deflate]. The provided buffer must have enough space
     * where 32768 bytes is always enough. Otherwise, a memory access error is inevitable.
     *
     * It may return a length less than the window size, even when more than the window size in input has been provided.
     * In that case, it may return up to 258 bytes less due to how zlib's implementation of deflate manages the sliding
     * window and lookahead for matches.
     *
     * If the application needs the last window-size bytes of input, then that would need to be saved by the
     * application.
     *
     * @param dictionary The buffer in which to write data
     * @return The length of the retrieved dictionary
     * @throws ZException
     */
    fun deflateGetDictionary(dictionary: MemorySegment): UInt {
        val result = zRef.deflateGetDictionary(dictionary)
        if (result < 0) {
            throwError(result)
        }
        if (result > dictionary.byteSize()) {
            throw IllegalArgumentException("Dictionary ($result bytes) is larger than the provided buffer (${dictionary.byteSize()} bytes)")
        }
        return result.toUInt()
    }

    override fun deflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef.deflateGetDictionary(dictionary)
        if (result < 0) {
            throwError(result)
        }
        if (result > dictionary.size) {
            throw IllegalArgumentException("Dictionary ($result bytes) is larger than the provided buffer (${dictionary.size} bytes)")
        }
        return result.toUInt()
    }

    /**
     * Initializes the compression dictionary from the given byte sequence without producing any compressed output.
     * When using the zlib format, this function must be called immediately after [deflateInit] or [deflateReset], and
     * before any call of [deflate]. The compressor and decompressor must use exactly the same dictionary (see
     * [inflateSetDictionary]).
     *
     * The dictionary should consist of strings (byte sequences) that are likely to be encountered later in the data to
     * be compressed, with the most commonly used strings preferably put towards the end of the dictionary. Using a
     * dictionary is most useful when the data to be compressed is short and can be predicted with good accuracy; the
     * data can then be compressed better than with the default empty dictionary.
     *
     * Depending on the size of the compression data structures selected by [deflateInit], a part of the dictionary may
     * in effect be discarded, for example if the dictionary is larger than the window size.
     *
     * Upon return of this function, [adler] is set to the Adler-32 value of the dictionary; the decompressor may later
     * use this value to determine which dictionary has been used by the compressor.
     *
     * @param dictionary The buffer containing dictionary data
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun deflateSetDictionary(dictionary: MemorySegment): ZResult {
        return parseResult(
            zRef.deflateSetDictionary(dictionary)
        )
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            zRef.deflateSetDictionary(dictionary, indices.first, indices.last - indices.first + 1)
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
        val result = zRef.inflateGetDictionary(MemorySegment.NULL)
        if (result < 0) {
            throwError(result)
        }
        return result.toUInt()
    }

    /**
     * Returns the sliding dictionary being maintained by [inflateInit]. The provided buffer must have enough space
     * where 32768 bytes is always enough.
     *
     * @param dictionary The buffer in which to write data
     * @return The length of the retrieved dictionary
     * @throws ZException
     */
    fun inflateGetDictionary(dictionary: MemorySegment): UInt {
        val result = zRef.inflateGetDictionary(dictionary)
        if (result < 0) {
            throwError(result)
        }
        return result.toUInt()
    }

    override fun inflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef.inflateGetDictionary(dictionary)
        if (result < 0) {
            throwError(result)
        }
        if (result > dictionary.size) {
            throw IllegalArgumentException("Dictionary ($result bytes) is larger than the provided buffer (${dictionary.size} bytes)")
        }
        return result.toUInt()
    }

    /**
     * Initializes the decompression dictionary from the given uncompressed byte sequence. This function must be called
     * immediately after a call of [inflate], if that call returned [ZResult.NeedsDictionary].
     *
     * The dictionary chosen by the compressor can be determined from the Adler-32 value returned by that call of
     * inflate. The compressor and decompressor must use exactly the same dictionary (see [deflateSetDictionary]).
     * If the provided dictionary is smaller than the window and there is already data in the window, then the provided
     * dictionary will amend what's there. The application must ensure that the dictionary that was used for compression
     * is provided.
     *
     * @param dictionary The buffer containing dictionary data
     * @return [ZResult.Ok] if success
     * @throws ZException
     */
    fun inflateSetDictionary(dictionary: MemorySegment): ZResult {
        return parseResult(
            zRef.inflateSetDictionary(dictionary)
        )
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            zRef.inflateSetDictionary(dictionary, indices.first, indices.last - indices.first + 1)
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
