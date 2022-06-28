package uselessmnemonic.pak

import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.nativeHeap
import platform.zlib.*

actual class Deflater {

    actual fun push(chunk: ByteArray) {
    }

    actual fun push(chunk: ByteArray, offset: Int, size: Int) {
    }

    actual fun deflate(): ByteArray {
        TODO("Not yet implemented")
    }
}
