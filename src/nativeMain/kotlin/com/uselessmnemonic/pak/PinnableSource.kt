package com.uselessmnemonic.pak

import kotlinx.cinterop.*

/**
 * A simple interface that represents a buffer source.
 * Very few checks are made, so safety is number 1 priority.
 */
@OptIn(ExperimentalForeignApi::class)
internal abstract class PinnableSource {
    abstract fun pin(): CPointer<UByteVar>?
    abstract fun unpin()
    abstract fun slice(offset: UInt): PinnableSource
}

@OptIn(ExperimentalForeignApi::class)
internal object EmptySource : PinnableSource() {
    override fun pin(): CPointer<UByteVar>? = null
    override fun unpin() {}
    override fun slice(offset: UInt): PinnableSource {
        if (offset > 0U) {
            throw IllegalArgumentException("Empty source cannot be sliced further")
        }
        return this
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class ByteArraySource(private val array: ByteArray, private val start: Int) : PinnableSource() {
    private lateinit var pinnedArray: Pinned<ByteArray>

    override fun pin(): CPointer<UByteVar>? {
        pinnedArray = array.pin()
        return pinnedArray.addressOf(start).reinterpret()
    }

    override fun unpin() {
        pinnedArray.unpin()
    }

    override fun slice(offset: UInt): PinnableSource {
        if (offset == array.size.toUInt()) {
            return EmptySource
        }
        if (offset == 0U) {
            return this
        }
        // offset is bound by [1, array.size)
        // and therefore by [1, Int.MAX_VALUE)
        return ByteArraySource(array, start + offset.toInt())
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class NativeSource(private val pointer: CPointer<UByteVar>) : PinnableSource() {

    override fun pin(): CPointer<UByteVar>? {
        return pointer
    }

    override fun unpin() {}

    override fun slice(offset: UInt): PinnableSource {
        if (offset == 0U) {
            return this
        }
        return NativeSource(pointer.plus(offset.toLong())!!)
    }
}

@OptIn(ExperimentalForeignApi::class)
internal inline fun <T> PinnableSource.pinning(crossinline action: (CPointer<UByteVar>?) -> T): T {
    try {
        val source = pin()
        return action(source)
    } finally {
        unpin()
    }
}
