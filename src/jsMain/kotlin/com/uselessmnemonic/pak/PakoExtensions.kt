package com.uselessmnemonic.pak

import org.khronos.webgl.Uint8Array
import pako.zlib.*

inline fun zstream.deflateInit(level: Number) = deflateInit(this, level)
inline fun zstream.deflateParams(level: Number, strategy: Number): Number = deflateParams(this, level, strategy)
inline fun zstream.deflateReset() = deflateReset(this)
inline fun zstream.deflate(flush: Number) = deflate(this, flush)
inline fun zstream.deflateEnd() = deflateEnd(this)
inline fun zstream.deflateGetDictionary(dictionary: Uint8Array?): Array<Number> = deflateGetDictionary(this, dictionary)
inline fun zstream.deflateSetDictionary(dictionary: Uint8Array) = deflateSetDictionary(this, dictionary)

inline fun zstream.inflateInit() = inflateInit(this)
inline fun zstream.inflateReset() = inflateReset(this)
inline fun zstream.inflate(flush: Number) = inflate(this, flush)
inline fun zstream.inflateEnd() = inflateEnd(this)
inline fun zstream.inflateGetDictionary(dictionary: Uint8Array?): Array<Number> = inflateGetDictionary(this, dictionary)
inline fun zstream.inflateSetDictionary(dictionary: Uint8Array) = inflateSetDictionary(this, dictionary)
