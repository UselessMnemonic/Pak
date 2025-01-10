package com.uselessmnemonic.pak

import kotlinx.cinterop.*

/**
 * An implementation of ZStream which uses Kotlin/Native's ZLib bindings.
 */
@OptIn(ExperimentalForeignApi::class)
class NativeZStream : ZStream {

    private val zRef = nativeHeap.alloc<platform.zlib.z_stream>()

    private var input: ZBuffer = EmptyZBuffer
    private var output: ZBuffer = EmptyZBuffer
    override val availIn get() = input.byteSize
    override val availOut get() = output.byteSize
    override val totalIn get() = zRef.total_in.toULong()
    override val totalOut get() = zRef.total_out.toULong()
    override val adler get() = zRef.adler.toULong()

    private fun parseResult(result: Int): ZResult {
        return ZResult.entries.find { it.value == result }
            ?: ZError.entries.find { it.value == result }?.thrown(zRef.msg?.toKString())
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    private inline fun <T> pinningBuffers(crossinline action: () -> T): T {
        return input.pinning { inputPtr ->
            output.pinning { outputPtr ->
                zRef.next_in = inputPtr
                zRef.next_out = outputPtr
                zRef.avail_in = input.byteSize
                zRef.avail_out = output.byteSize
                val result = action()
                zRef.next_in = null
                zRef.next_out = null
                zRef.avail_in = 0U
                zRef.avail_out = 0U
                result
            }
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
        input = NativeZBuffer(pointer, byteSize)
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        input = ByteArrayZBuffer(buffer, indices)
    }

    /**
     * Provides output space to the compression engine from the given slice of the byte buffer. Once set, the engine
     * will fill as much output as is permitted.
     *
     * @param pointer A pointer to the start of the output buffer
     * @param byteSize The number of bytes available in the buffer
     */
    fun setOutput(pointer: CPointer<UByteVar>, byteSize: UInt) {
        output = NativeZBuffer(pointer, byteSize)
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        output = ByteArrayZBuffer(buffer, indices)
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef.deflateInit(level.value)
        )
    }

    override fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult {
        return parseResult(
            pinningBuffers {
                zRef.deflateParams(level.value, strategy.value)
            }
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        return memScoped {
            val size = alloc<UIntVar>()
            val result = parseResult(
                zRef.deflateGetDictionary(null, size.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.value
        }
    }

    override fun deflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        if (indices.isEmpty()) {
            return indices
        }

        val dictionary = ByteArrayZBuffer(dictionary, indices)
        val size = memScoped {
            val size = alloc<UIntVar>()
            val result = parseResult(
                dictionary.pinning { dictionaryPtr ->
                    zRef.deflateGetDictionary(dictionaryPtr?.reinterpret(), size.ptr)
                }
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.value
        }.toInt()

        if (size == 0) {
            return IntRange.EMPTY
        }
        return IntRange(indices.first, indices.first + size - 1)
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val dictionary = ByteArrayZBuffer(dictionary, indices)
        return parseResult(
            dictionary.pinning { dictionaryPtr ->
                zRef.deflateSetDictionary(dictionaryPtr?.reinterpret(), dictionary.byteSize)
            }
        )
    }

    override fun deflate(flush: ZFlush): ZResult {
        val prevTotalIn = totalIn
        val prevTotalOut = totalOut
        val result = parseResult(
            pinningBuffers {
                zRef.deflate(flush.value)
            }
        )
        val totalRead = totalIn - prevTotalIn
        val totalWrite = totalOut - prevTotalOut
        input = input.slice(totalRead.toUInt())
        output = output.slice(totalWrite.toUInt())
        return result
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
            val size = alloc<UIntVar>()
            val result = parseResult(
                zRef.inflateGetDictionary(null, size.ptr)
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.value
        }
    }

    override fun inflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        if (indices.isEmpty()) {
            return indices
        }

        val dictionary = ByteArrayZBuffer(dictionary, indices)
        val size = memScoped {
            val size = alloc<UIntVar>()
            val result = parseResult(
                dictionary.pinning { dictionaryPtr ->
                    zRef.inflateGetDictionary(dictionaryPtr?.reinterpret(), size.ptr)
                }
            )
            if (result != ZResult.Ok) {
                ZError.StreamError.thrown("Unexpected result $result")
            }
            size.value
        }.toInt()

        if (size == 0) {
            return IntRange.EMPTY
        }
        return IntRange(indices.first, indices.first + size - 1)
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        val dictionary = ByteArrayZBuffer(dictionary, indices)
        return parseResult(
            dictionary.pinning { dictionaryPtr ->
                zRef.inflateSetDictionary(dictionaryPtr?.reinterpret(), dictionary.byteSize)
            }
        )
    }

    override fun inflate(flush: ZFlush): ZResult {
        val prevTotalIn = totalIn
        val prevTotalOut = totalOut
        val result = parseResult(
            pinningBuffers {
                zRef.inflate(flush.value)
            }
        )
        val totalRead = totalIn - prevTotalIn
        val totalWrite = totalOut - prevTotalOut
        input = input.slice(totalRead.toUInt())
        output = output.slice(totalWrite.toUInt())
        return result
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
