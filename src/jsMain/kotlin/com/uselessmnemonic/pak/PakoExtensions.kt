package com.uselessmnemonic.pak

import org.khronos.webgl.Uint8Array
import pako.zlib.*

internal inline fun zstream.deflateInit(level: Number) = deflateInit(this, level)
internal inline fun zstream.deflateParams(level: Number, strategy: Number): Number = deflateParams(this, level, strategy)
internal inline fun zstream.deflateReset() = deflateReset(this)
internal inline fun zstream.deflate(flush: Number) = deflate(this, flush)
internal inline fun zstream.deflateEnd() = deflateEnd(this)
internal inline fun zstream.deflateGetDictionary(dictionary: Uint8Array?): Number = deflateGetDictionary(this, dictionary)
internal inline fun zstream.deflateSetDictionary(dictionary: Uint8Array) = deflateSetDictionary(this, dictionary)

internal inline fun zstream.inflateInit() = inflateInit(this)
internal inline fun zstream.inflateReset() = inflateReset(this)
internal inline fun zstream.inflate(flush: Number) = inflate(this, flush)
internal inline fun zstream.inflateEnd() = inflateEnd(this)
internal inline fun zstream.inflateGetDictionary(dictionary: Uint8Array?): Number = inflateGetDictionary(this, dictionary)
internal inline fun zstream.inflateSetDictionary(dictionary: Uint8Array) = inflateSetDictionary(this, dictionary)
