package com.uselessmnemonic.pak

import kotlinx.cinterop.*
import platform.zlib.*

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateInit(level: Int) = deflateInit(this.ptr, level)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateParams(level: Int, strategy: Int) = deflateParams(this.ptr, level, strategy)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateGetDictionary(dictionary: CValuesRef<UByteVar>?, dictLength: CValuesRef<UIntVar>?) = deflateGetDictionary(this.ptr, dictionary, dictLength)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateSetDictionary(dictionary: CValuesRef<UByteVar>?, dictLength: UInt) = deflateSetDictionary(this.ptr, dictionary, dictLength)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflate(flush: Int) = deflate(this.ptr, flush)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateReset() = deflateReset(this.ptr)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.deflateEnd() = deflateEnd(this.ptr)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflateInit() = inflateInit(this.ptr)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflateGetDictionary(dictionary: CValuesRef<UByteVar>?, dictLength: CValuesRef<UIntVar>?) = inflateGetDictionary(this.ptr, dictionary, dictLength)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflateSetDictionary(dictionary: CValuesRef<UByteVar>?, dictLength: UInt) = inflateSetDictionary(this.ptr, dictionary, dictLength)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflate(flush: Int) = inflate(this.ptr, flush)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflateReset() = inflateReset(this.ptr)

@OptIn(ExperimentalForeignApi::class)
inline fun z_stream.inflateEnd() = inflateEnd(this.ptr)
