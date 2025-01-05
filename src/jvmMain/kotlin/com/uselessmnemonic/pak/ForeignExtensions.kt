package com.uselessmnemonic.pak

import java.lang.foreign.MemorySegment

internal fun ByteArray.asMemorySegment(range: IntRange): MemorySegment {
    val first = range.first.toLong()
    val last = range.last.toLong()

    if (last < first) { // empty
        return MemorySegment.NULL
    }
    if (first < 0 || last >= size) {
        throw IllegalArgumentException("$range exceeds array bounds (length $size)")
    }

    return MemorySegment.ofArray(this).asSlice(first, last - first + 1)
}

internal inline fun MemorySegment.takeIfNotNull() = takeIf { address() != 0L }
