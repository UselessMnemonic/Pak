package com.uselessmnemonic.pak

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

val ByteArray.buffer get() = unsafeCast<Int8Array>().buffer

fun ByteArray.asUInt8Array(indices: IntRange): Uint8Array {
    val byteLength = if (indices.isEmpty()) {
        0
    } else {
        indices.last - indices.first + 1
    }
    return Uint8Array(buffer, indices.first, byteLength)
}
