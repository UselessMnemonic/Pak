package uselessmnemonic.pak

import kotlinx.cinterop.*
import platform.zlib.z_stream

class ZStream(val level: Int) {

    private var stream = nativeHeap.alloc<z_stream>()

    fun deflateInit(): Int {
        return platform.zlib.deflateInit(stream.ptr, level)
    }

    fun deflate(flush: Int): Int {
        TODO()
    }

    fun deflateEnd(): Int {
        TODO()
    }

    fun inflateInit(): Int {
        TODO()
    }

    fun inflate(flush: Int): Int {
        TODO()
    }

    fun inflateEnd(): Int {
        TODO()
    }

}
