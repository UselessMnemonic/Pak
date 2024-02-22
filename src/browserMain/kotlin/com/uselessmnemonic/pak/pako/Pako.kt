@file:JsModule("pako")

package com.uselessmnemonic.pak.pako

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

external object constants {

    // Flush Values
    val Z_NO_FLUSH: Int
    val Z_PARTIAL_FLUSH: Int
    val Z_SYNC_FLUSH: Int
    val Z_FULL_FLUSH: Int
    val Z_FINISH: Int
    val Z_BLOCK: Int
    val Z_TREES: Int

    // Strategy Values
    val Z_FILTERED: Int
    val Z_HUFFMAN_ONLY: Int
    val Z_RLE: Int
    val Z_FIXED: Int
    val Z_DEFAULT_STRATEGY: Int

    // Return Codes
    val Z_OK: Int
    val Z_STREAM_END: Int
    val Z_NEED_DICT: Int
    val Z_ERRNO: Int
    val Z_STREAM_ERROR: Int
    val Z_DATA_ERROR: Int
    val Z_BUF_ERROR: Int
}

external fun deflate(data: Uint8Array): Uint8Array
external fun deflate(data: Uint8Array, options: DeflateOptions): Uint8Array
external fun deflate(data: ArrayBuffer): Uint8Array
external fun deflate(data: ArrayBuffer, options: DeflateOptions): Uint8Array
external fun deflate(data: String): Uint8Array
external fun deflate(data: String, options: DeflateOptions): Uint8Array

external fun deflateRaw(data: Uint8Array): Uint8Array
external fun deflateRaw(data: Uint8Array, options: DeflateOptions): Uint8Array
external fun deflateRaw(data: ArrayBuffer): Uint8Array
external fun deflateRaw(data: ArrayBuffer, options: DeflateOptions): Uint8Array
external fun deflateRaw(data: String): Uint8Array
external fun deflateRaw(data: String, options: DeflateOptions): Uint8Array

external fun gzip(data: Uint8Array): Uint8Array
external fun gzip(data: Uint8Array, options: DeflateOptions): Uint8Array
external fun gzip(data: ArrayBuffer): Uint8Array
external fun gzip(data: ArrayBuffer, options: DeflateOptions): Uint8Array
external fun gzip(data: String): Uint8Array
external fun gzip(data: String, options: DeflateOptions): Uint8Array

external fun inflate(data: Uint8Array): Uint8Array
external fun inflate(data: Uint8Array, options: InflateOptions): dynamic

external fun inflateRaw(data: Uint8Array): Uint8Array
external fun inflateRaw(data: Uint8Array, options: InflateOptions): dynamic

external fun ungzip(data: Uint8Array): Uint8Array
external fun ungzip(data: Uint8Array, options: InflateOptions): dynamic

open external class Deflate {
    val err: Number
    val msg: String
    val result: Uint8Array
    constructor()
    constructor(options: DeflateOptions)
    open fun onData(chunk: dynamic)
    open fun onEnd(status: Number)
    fun push(data: Uint8Array): Boolean
    fun push(data: Uint8Array, flushMode: Number): Boolean
    fun push(data: Uint8Array, flushMode: Boolean): Boolean
    fun push(data: ArrayBuffer): Boolean
    fun push(data: ArrayBuffer, flushMode: Number): Boolean
    fun push(data: ArrayBuffer, flushMode: Boolean): Boolean
    fun push(data: String): Boolean
    fun push(data: String, flushMode: Number): Boolean
    fun push(data: String, flushMode: Boolean): Boolean
}

open external class Inflate {
    val err: Number
    val msg: String
    val result: dynamic
    constructor()
    constructor(options: InflateOptions)
    fun onData(chunk: dynamic)
    fun onEnd(status: Number)
    fun push(data: ByteArray): Boolean
    fun push(data: ByteArray, flushMode: Number): Boolean
    fun push(data: Array<Byte>): Boolean
    fun push(data: Array<Byte>, flushMode: Number): Boolean
    fun push(data: Uint8Array): Boolean
    fun push(data: Uint8Array, flushMode: Number): Boolean
    fun push(data: Uint8Array, flushMode: Boolean): Boolean
    fun push(data: ArrayBuffer): Boolean
    fun push(data: ArrayBuffer, flushMode: Number): Boolean
    fun push(data: ArrayBuffer, flushMode: Boolean): Boolean
}
