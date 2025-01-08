package com.uselessmnemonic.pak

import kotlinx.cinterop.*

/**
 * A simple interface that represents a buffer source.
 */
@OptIn(ExperimentalForeignApi::class)
internal interface ZBuffer {
    val byteSize: UInt
    fun pin(): CPointer<UByteVar>?
    fun unpin()
    fun slice(offset: UInt): ZBuffer
}

@OptIn(ExperimentalForeignApi::class)
internal object EmptyZBuffer : ZBuffer {

    override val byteSize: UInt = 0U

    override fun pin(): CPointer<UByteVar>? = null

    override fun unpin() {}

    override fun slice(offset: UInt): EmptyZBuffer {
        if (offset > 0U) {
            throw IllegalArgumentException("Empty segment cannot be sliced further")
        }
        return this
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class ByteArrayZBuffer(private val array: ByteArray, private val indices: IntRange) : ZBuffer {

    init {
        if (!indices.isEmpty() && (indices.first < 0 || indices.last >= array.size)) {
            throw IllegalArgumentException("$indices exceeds array bounds (length ${array.size})")
        }
    }

    // always bound by [0, Int.MAX_VALUE]
    override val byteSize: UInt = if (indices.isEmpty()) 0U else (indices.last - indices.first + 1).toUInt()
    private var pinnedArray: Pinned<ByteArray>? = null

    override fun pin(): CPointer<UByteVar>? {
        if (byteSize == 0U) {
            return null
        }
        if (pinnedArray == null) {
            pinnedArray = array.pin()
        }
        return pinnedArray!!.addressOf(indices.first).reinterpret()
    }

    override fun unpin() {
        pinnedArray?.unpin()
        pinnedArray = null
    }

    override fun slice(offset: UInt): ZBuffer {
        if (offset == byteSize) {
            return EmptyZBuffer
        }
        if (offset == 0U) {
            return this
        }
        if (offset > byteSize) {
            throw IllegalArgumentException("Offset exceeds maximum size")
        }
        // offset is bound by [1, byteSize)
        // and therefore by [1, Int.MAX_VALUE)
        return ByteArrayZBuffer(array, IntRange(indices.first + offset.toInt(), indices.last))
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class NativeZBuffer(private val pointer: CPointer<UByteVar>, override val byteSize: UInt) : ZBuffer {

    override fun pin(): CPointer<UByteVar>? {
        if (byteSize == 0U) {
            return null
        }
        return pointer
    }

    override fun unpin() {}

    override fun slice(offset: UInt): ZBuffer {
        if (offset == byteSize) {
            return EmptyZBuffer
        }
        if (offset == 0U) {
            return this
        }
        if (offset > byteSize) {
            throw IllegalArgumentException("Offset exceeds maximum size")
        }
        return NativeZBuffer(pointer.plus(offset.toLong())!!, byteSize - offset)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal inline fun <T> ZBuffer.pinning(crossinline action: (CPointer<UByteVar>?) -> T): T {
    try {
        val source = pin()
        return action(source)
    } finally {
        unpin()
    }
}
