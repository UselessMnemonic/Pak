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
        return ZResult.entries.find { it.value == result }
            ?: ZError.entries.find { it.value == result }?.thrown(zRef?.msg)
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
        if (indices.first < 0 || indices.last >= buffer.size) {
            throw IllegalArgumentException("$indices exceeds array bounds (length ${buffer.size})")
        }
        setInput(buffer.asUInt8Array(indices))
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
        if (indices.first < 0 || indices.last >= buffer.size) {
            throw IllegalArgumentException("$indices exceeds array bounds (length ${buffer.size})")
        }
        setOutput(buffer.asUInt8Array(indices))
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            pako.zlib.deflateInit(zRef!!, level.value)
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        throw UnsupportedOperationException("Retrieving a deflate dictionary is not supported")
    }

    override fun deflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        throw UnsupportedOperationException("Retrieving a deflate dictionary is not supported")
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            pako.zlib.deflateSetDictionary(zRef!!, dictionary.asUInt8Array(indices))
        )
    }

    override fun deflateParams(level: ZCompressionLevel, strategy: ZCompressionStrategy): ZResult {
        throw UnsupportedOperationException("Setting deflate params is not supported")
    }

    override fun deflate(flush: ZFlush): ZResult {
        return parseResult(
            pako.zlib.deflate(zRef!!, flush.value)
        )
    }

    override fun deflateReset(): ZResult {
        return parseResult(
            pako.zlib.deflateReset(zRef!!)
        )
    }

    override fun deflateEnd(): ZResult {
        return parseResult(
            pako.zlib.deflateEnd(zRef!!)
        )
    }

    override fun inflateInit(): ZResult {
        return parseResult(
            pako.zlib.inflateInit(zRef!!)
        )
    }

    override fun inflateGetDictionaryLength(): UInt {
        throw UnsupportedOperationException("Retrieving an inflate dictionary is not supported")
    }

    override fun inflateGetDictionary(dictionary: ByteArray, indices: IntRange): IntRange {
        throw UnsupportedOperationException("Retrieving an inflate dictionary is not supported")
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            pako.zlib.inflateSetDictionary(zRef!!, dictionary.asUInt8Array(indices))
        )
    }

    override fun inflate(flush: ZFlush): ZResult {
        return parseResult(
            pako.zlib.inflate(zRef!!, flush.value)
        )
    }

    override fun inflateReset(): ZResult {
        return parseResult(
            pako.zlib.inflateReset(zRef!!)
        )
    }

    override fun inflateEnd(): ZResult {
        return parseResult(
            pako.zlib.inflateEnd(zRef!!)
        )
    }

    override fun close() {
        zRef = null
    }
}

actual fun ZStream(): ZStream = PakoZStream()
