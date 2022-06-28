@file:JsModule("pako")
@file:JsNonModule
package pako

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

external class Deflate(options: dynamic) {
    var err: Int
    var msg: String?
    var result: Uint8Array?

    var onData: (Uint8Array) -> Unit
    var onEnd: (Int) -> Unit

    fun push(data: Uint8Array, flushMode: Int): Boolean
    fun push(data: ByteArray, flushMode: Int): Boolean
    fun push(data: String, flushMode: Int): Boolean

    fun push(data: Uint8Array, flushMode: Boolean): Boolean
    fun push(data: ByteArray, flushMode: Boolean): Boolean
    fun push(data: String, flushMode: Boolean): Boolean
}

external fun deflate(input: Uint8Array, options: dynamic): Uint8Array
external fun deflate(input: String, options: dynamic): Uint8Array

external fun deflateRaw(input: Uint8Array, options: dynamic): Uint8Array
external fun deflateRaw(input: String, options: dynamic): Uint8Array

external fun gzip(input: Uint8Array, options: dynamic): Uint8Array
external fun gzip(input: String, options: dynamic): Uint8Array

external class Inflate(options: dynamic) {
    var err: Int
    var msg: String?
    var result: dynamic

    var onData: (Uint8Array) -> Unit
    var onEnd: (Int) -> Unit

    fun push(data: Uint8Array, flushMode: Int): Boolean
    fun push(data: ArrayBuffer, flushMode: Int): Boolean
    fun push(data: String, flushMode: Int): Boolean

    fun push(data: Uint8Array, flushMode: Boolean): Boolean
    fun push(data: ArrayBuffer, flushMode: Boolean): Boolean
    fun push(data: String, flushMode: Boolean): Boolean

    fun push(data: Uint8Array): Boolean
    fun push(data: ArrayBuffer): Boolean
    fun push(data: String): Boolean
}

external fun inflate(data: Uint8Array, options: dynamic): dynamic
external fun inflateRaw(data: Uint8Array, options: dynamic): dynamic
external fun ungzip(data: Uint8Array, options: dynamic): dynamic
