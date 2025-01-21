package com.uselessmnemonic.pak

internal inline fun IntRange.getRangeLength(array: ByteArray): Int {
    if (isEmpty()) return 0
    if (first < 0 || last >= array.size) {
        throw IllegalArgumentException("$this exceeds array bounds (length ${array.size})")
    }
    return last - first + 1
}
