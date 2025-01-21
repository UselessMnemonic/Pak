package com.uselessmnemonic.pak

/**
 * An implementation of ZStream which uses JNI.
 */
class JavaZStream : ZStream {
    private val zRef = ZStreamRefImpl()

    override val availIn get() = zRef.availIn.toUInt()
    override val availOut get() = zRef.availOut.toUInt()
    override val totalIn get() = zRef.totalIn.toULong()
    override val totalOut get() = zRef.totalOut.toULong()
    override val adler get() = zRef.adler.toULong()

    private fun parseResult(result: Int): ZResult {
        return ZResult.entries.find { it.value == result }
            ?: parseError(result).thrown(zRef.msg)
    }

    private fun parseError(result: Int): ZError {
        return ZError.entries.find { it.value == result }
            ?: throw IllegalStateException("Unrecognized result value $result")
    }

    override fun setInput(buffer: ByteArray, indices: IntRange) {
        if (indices.isEmpty()) {
            zRef.setInput(null)
        } else {
            zRef.setInput(buffer, indices.first, indices.last - indices.first + 1)
        }
    }

    override fun setOutput(buffer: ByteArray, indices: IntRange) {
        if (indices.isEmpty()) {
            zRef.setOutput(null)
        } else {
            zRef.setOutput(buffer, indices.first, indices.last - indices.first + 1)
        }
    }

    override fun deflateInit(level: ZCompressionLevel): ZResult {
        return parseResult(
            zRef.deflateInit(level.value)
        )
    }

    override fun deflateGetDictionaryLength(): UInt {
        val result = zRef.deflateGetDictionary(null)
        if (result < 0) {
            parseError(result).thrown(zRef.msg)
        }
        return result.toUInt()
    }

    override fun deflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef.deflateGetDictionary(dictionary)
        if (result < 0) {
            parseError(result).thrown(zRef.msg)
        }
        return result.toUInt()
    }

    override fun deflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            if (indices.isEmpty()) {
                zRef.deflateSetDictionary(dictionary, 0, 0)
            } else {
                zRef.deflateSetDictionary(dictionary, indices.first, indices.last - indices.first + 1)
            }
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
        val result = zRef.inflateGetDictionary(null)
        if (result < 0) {
            parseError(result).thrown(zRef.msg)
        }
        return result.toUInt()
    }

    override fun inflateGetDictionary(dictionary: ByteArray): UInt {
        val result = zRef.inflateGetDictionary(dictionary)
        if (result < 0) {
            parseError(result).thrown(zRef.msg)
        }
        return result.toUInt()
    }

    override fun inflateSetDictionary(dictionary: ByteArray, indices: IntRange): ZResult {
        return parseResult(
            if (indices.isEmpty()) {
                zRef.inflateSetDictionary(dictionary, 0, 0)
            } else {
                zRef.inflateSetDictionary(dictionary, indices.first, indices.last - indices.first + 1)
            }
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
