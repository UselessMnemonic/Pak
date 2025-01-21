package com.uselessmnemonic.pak

import org.khronos.webgl.Uint8Array

/**
 * An implementation of ZStream which wraps around [Pako](https://github.com/nodeca/pako)
 */
class PakoZStream : ZStream {
    private var zRef: pako.zlib.zstream? = pako.zlib.zstream()

    override val availIn get() = zRef!!.avail_in.toInt().toUInt()
    override val availOut get() = zRef!!.avail_out.toInt().toUInt()
    override val totalIn get() = zRef!!.total_in.toLong().toULong()
    override val totalOut get() = zRef!!.total_out.toLong().toULong()
    override val adler get() = zRef!!.adler.toLong().toULong()

    private fun parseResult(result: Number): ZResult {
        return ZResult.entries.find { it.value == result } ?: throwError(result)
    }

    private fun throwError(result: Number): Nothing {
        return ZError.entries.find { it.value == result }?.thrown(zRef?.msg)
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    /**
     * Provides input to the compression engine from the given typed array. Once set, the engine will continue to slice
     * the same input buffer automatically until it is exhausted.
     *
     * @param buffer The input buffer
     */
    fun setInput(buffer: Uint8Array) {
        val zRef = zRef!!

        zRef.next_in = 0
        zRef.avail_in = buffer.byteLength
        if (zRef.avail_in == 0) {
            zRef.input = null
            return
        }
        zRef.input = buffer
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        val zRef = zRef!!
        val input = buffer.asUInt8Array(indices)

        zRef.input = input
        zRef.next_in = 0
        zRef.avail_in = input.byteLength
    }

    /**
     * Provides output space to the compression engine from the given typed array. Once set, the engine will fill as
     * much output as is permitted.
     *
     * @param buffer The output buffer
     */
    fun setOutput(buffer: Uint8Array) {
        val zRef = zRef!!

        zRef.next_out = 0
        zRef.avail_out = buffer.byteLength
        if (zRef.avail_out == 0) {
            zRef.output = null
            return
        }
        zRef.output = buffer
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        val zRef = zRef!!
        val output = buffer.asUInt8Array(indices)

        zRef.output = output
        zRef.next_out = 0
        zRef.avail_out = output.byteLength
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef!!.deflateInit(level.value)
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        val result = zRef!!.deflateGetDictionary(null).toInt()
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
    fun deflateGetDictionary(dictionary: Uint8Array): UInt {
        val result = zRef!!.deflateGetDictionary(dictionary).toInt()
        if (result < 0) {
            throwError(result)
        }
        if (result > dictionary.byteLength) {
            throw IllegalArgumentException("Dictionary ($result bytes) is larger than the provided buffer (${dictionary.byteLength} bytes)")
        }
        return result.toUInt()
    }

    override fun deflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef!!.deflateGetDictionary(dictionary.asUInt8Array()).toInt()
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
    fun deflateSetDictionary(dictionary: Uint8Array): ZResult {
        return parseResult(
            zRef!!.deflateSetDictionary(dictionary)
        )
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            zRef!!.deflateSetDictionary(dictionary.asUInt8Array(indices))
        )
    }

    override fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult {
        return parseResult(
            zRef!!.deflateParams(level.value, strategy.value)
        )
    }

    override fun deflate(flush: ZFlush): ZResult {
        return parseResult(
            zRef!!.deflate(flush.value)
        )
    }

    override fun deflateReset(): ZResult {
        return parseResult(
            zRef!!.deflateReset()
        )
    }

    override fun deflateEnd(): ZResult {
        return parseResult(
            zRef!!.deflateEnd()
        )
    }

    override fun inflateInit(): ZResult {
        return parseResult(
            zRef!!.inflateInit()
        )
    }

    override fun inflateGetDictionaryLength(): UInt {
        val result = zRef!!.inflateGetDictionary(null).toInt()
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
    fun inflateGetDictionary(dictionary: Uint8Array): UInt {
        val result = zRef!!.inflateGetDictionary(dictionary).toInt()
        if (result < 0) {
            throwError(result)
        }
        if (result > dictionary.byteLength) {
            throw IllegalArgumentException("Dictionary ($result bytes) is larger than the provided buffer (${dictionary.byteLength} bytes)")
        }
        return result.toUInt()
    }

    override fun inflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef!!.inflateGetDictionary(dictionary.asUInt8Array()).toInt()
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
    fun inflateSetDictionary(dictionary: Uint8Array): ZResult {
        return parseResult(
            zRef!!.inflateSetDictionary(dictionary)
        )
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            zRef!!.inflateSetDictionary(dictionary.asUInt8Array(indices))
        )
    }

    override fun inflate(flush: ZFlush): ZResult {
        return parseResult(
            zRef!!.inflate(flush.value)
        )
    }

    override fun inflateReset(): ZResult {
        return parseResult(
            zRef!!.inflateReset()
        )
    }

    override fun inflateEnd(): ZResult {
        return parseResult(
            zRef!!.inflateEnd()
        )
    }

    override fun close() {
        zRef = null
    }
}

actual fun ZStream(): ZStream = PakoZStream()
