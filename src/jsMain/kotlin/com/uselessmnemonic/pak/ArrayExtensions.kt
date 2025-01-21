package com.uselessmnemonic.pak

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

internal val ByteArray.buffer get() = unsafeCast<Int8Array>().buffer

internal val EmptyUint8Buffer = Uint8Array(0)

internal fun ByteArray.asUInt8Array(indices: IntRange): Uint8Array {
    if (indices.isEmpty()) return EmptyUint8Buffer
    if (indices.first < 0 || indices.last >= size) {
        throw IllegalArgumentException("$indices exceeds array bounds (length $size)")
    }
    return Uint8Array(buffer, indices.first, indices.last - indices.first + 1)
}

internal fun ByteArray.asUInt8Array(): Uint8Array {
    return Uint8Array(buffer, 0, size)
}
