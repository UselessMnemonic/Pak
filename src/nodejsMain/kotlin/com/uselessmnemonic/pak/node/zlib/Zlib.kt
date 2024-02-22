@file:JsModule("zlib")

package com.uselessmnemonic.pak.node.zlib

import com.uselessmnemonic.pak.node.Buffer
import org.khronos.webgl.Uint8Array

external interface ZlibOptions {
    val flush: Number
    val finishFlush: Number
    val chunkSize: Number
    val windowBits: Number
    val level: Number
    val memLevel: Number
    val strategy: Number
    val dictionary: dynamic
    val info: Boolean
    val maxOutputLength: Number
}

external fun inflateSync(buf: Uint8Array): Buffer

external fun deflateSync(buf: Uint8Array): Buffer

external object constants {
    // Flush Values
    val Z_NO_FLUSH: Number
    val Z_PARTIAL_FLUSH: Number
    val Z_SYNC_FLUSH: Number
    val Z_FULL_FLUSH: Number
    val Z_FINISH: Number
    val Z_BLOCK: Number
    val Z_TREES: Number

    // Return Values
    val Z_OK: Number
    val Z_STREAM_END: Number
    val Z_NEED_DICT: Number
    val Z_ERRNO: Number
    val Z_STREAM_ERROR: Number
    val Z_DATA_ERROR: Number
    val Z_MEM_ERROR: Number
    val Z_BUF_ERROR: Number
    val Z_VERSION_ERROR: Number

    // Compression Levels
    val Z_NO_COMPRESSION: Number
    val Z_BEST_SPEED: Number
    val Z_BEST_COMPRESSION: Number
    val Z_DEFAULT_COMPRESSION: Number

    // Compression Strategies
    val Z_FILTERED: Number
    val Z_HUFFMAN_ONLY: Number
    val Z_RLE: Number
    val Z_FIXED: Number
    val Z_DEFAULT_STRATEGY: Number

    val ZLIB_VERNUM: Number
}
