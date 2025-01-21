package com.uselessmnemonic.pak

import kotlinx.cinterop.*

/**
 * An implementation of ZStream which uses Kotlin/Native's ZLib bindings.
 */
@OptIn(ExperimentalForeignApi::class)
class NativeZStream : ZStream {

    private val zRef = nativeHeap.alloc<platform.zlib.z_stream>()
    private var input: PinnableSource = EmptySource
    private var output: PinnableSource = EmptySource

    override val availIn get() = zRef.avail_in
    override val availOut get() = zRef.avail_out
    override val totalIn get() = zRef.total_in.toULong()
    override val totalOut get() = zRef.total_out.toULong()
    override val adler get() = zRef.adler.toULong()

    private fun parseResult(result: Int): ZResult {
        return ZResult.entries.find { it.value == result } ?: throwError(result)
    }

    private fun throwError(result: Int): Nothing {
        ZError.entries.find { it.value == result }?.thrown(zRef.msg?.toKString())
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    private inline fun <T> consumeBuffers(crossinline action: () -> T): T {
        val prevAvailIn = availIn
        val prevAvailOut = availOut

        val nextIn = input.pin()
        try {
            val nextOut = output.pin()
            try {
                zRef.next_in = nextIn
                zRef.next_out = nextOut
                return action()
            } finally {
                zRef.next_out = null
                output.unpin()
                output = output.slice(prevAvailOut - availOut)
            }
        } finally {
            zRef.next_in = null
            input.unpin()
            input = input.slice(prevAvailIn - availIn)
        }
    }

    /**
     * Provides input data to the compression engine from the given slice of the byte buffer. Once set, the engine will
     * continue to slice the same input buffer automatically until it is exhausted.
     *
     * @param pointer A pointer to the start of the input buffer
     * @param byteSize The number of bytes available to read
     */
    fun setInput(pointer: CPointer<UByteVar>, byteSize: UInt) {
        if (byteSize == 0U) {
            zRef.avail_in = 0U
            input = EmptySource
            return
        }

        zRef.avail_in = byteSize
        input = NativeSource(pointer)
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        val byteSize = indices.getRangeLength(buffer).toUInt()
        if (byteSize == 0U) {
            zRef.avail_in = 0U
            input = EmptySource
            return
        }
        zRef.avail_in = byteSize
        input = ByteArraySource(buffer, indices.first)
    }

    /**
     * Provides output space to the compression engine from the given slice of the byte buffer. Once set, the engine
     * will fill as much output as is permitted.
     *
     * @param pointer A pointer to the start of the output buffer
     * @param byteSize The number of bytes available in the buffer
     */
    fun setOutput(pointer: CPointer<UByteVar>, byteSize: UInt) {
        if (byteSize == 0U) {
            zRef.avail_out = 0U
            output = EmptySource
            return
        }

        zRef.avail_out = byteSize
        output = NativeSource(pointer)
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        val byteSize = indices.getRangeLength(buffer).toUInt()
        if (byteSize == 0U) {
            zRef.avail_out = 0U
            output = EmptySource
            return
        }
        zRef.avail_out = byteSize
        output = ByteArraySource(buffer, indices.first)
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef.deflateInit(level.value)
        )
    }

    override fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult {
        return parseResult(
            consumeBuffers {
                zRef.deflateParams(level.value, strategy.value)
            }
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                zRef.deflateGetDictionary(null, byteSize.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
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
     * @param dictionary The start of the buffer in which to write data
     * @return The length of the retrieved dictionary
     * @throws ZException
     */
    fun deflateGetDictionary(dictionary: CPointer<UByteVar>): UInt {
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                zRef.deflateGetDictionary(dictionary, byteSize.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
    }

    override fun deflateGetDictionary(dictionary: ByteArray): UInt {
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                dictionary.usePinned { pinnedDictionary ->
                    zRef.deflateGetDictionary(pinnedDictionary.addressOf(0).reinterpret(), byteSize.ptr)
                }
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
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
     * @param dictionary The start of the buffer containing dictionary data
     * @param length The length of dictionary data in the buffer
     * @return [ZResult.Ok]
     * @throws ZException
     */
    fun deflateSetDictionary(dictionary: CValuesRef<UByteVar>, length: UInt): ZResult {
        return parseResult(
            zRef.deflateSetDictionary(dictionary, length)
        )
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val byteSize = indices.getRangeLength(dictionary).toUInt()
        return parseResult(
            dictionary.usePinned { pinnedDictionary ->
                zRef.deflateSetDictionary(pinnedDictionary.addressOf(indices.first).reinterpret(), byteSize)
            }
        )
    }

    override fun deflate(flush: ZFlush): ZResult {
        return parseResult(
            consumeBuffers {
                zRef.deflate(flush.value)
            }
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
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                zRef.inflateGetDictionary(null, byteSize.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
    }

    /**
     * Returns the sliding dictionary being maintained by [inflateInit]. The provided buffer must have enough space
     * where 32768 bytes is always enough.
     *
     * @param dictionary The start of the buffer in which to write data
     * @return The length of the retrieved dictionary
     * @throws ZException
     */
    fun inflateGetDictionary(dictionary: CPointer<UByteVar>): UInt {
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                zRef.inflateGetDictionary(dictionary, byteSize.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
    }

    override fun inflateGetDictionary(dictionary: ByteArray): UInt {
        return memScoped {
            val byteSize = alloc<UIntVar>()
            val result = parseResult(
                dictionary.usePinned { pinnedDictionary ->
                    zRef.inflateGetDictionary(pinnedDictionary.addressOf(0).reinterpret(), byteSize.ptr)
                }
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            byteSize.value
        }
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
     * @param dictionary The start of the buffer containing dictionary data
     * @param length The length of dictionary data in the buffer
     * @return [ZResult.Ok] if success
     * @throws ZException
     */
    fun inflateSetDictionary(dictionary: CValuesRef<UByteVar>, length: UInt): ZResult {
        return parseResult(
            zRef.inflateSetDictionary(dictionary, length)
        )
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val byteSize = indices.getRangeLength(dictionary).toUInt()
        return parseResult(
            dictionary.usePinned { pinnedDictionary ->
                zRef.inflateSetDictionary(pinnedDictionary.addressOf(indices.first).reinterpret(), byteSize)
            }
        )
    }

    override fun inflate(flush: ZFlush): ZResult {
        return parseResult(
            consumeBuffers {
                zRef.inflate(flush.value)
            }
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
        try {
            inflateEnd()
            deflateEnd()
        } catch (_ : Throwable) {}
        nativeHeap.free(zRef)
    }
}

actual fun ZStream(): ZStream = NativeZStream()
