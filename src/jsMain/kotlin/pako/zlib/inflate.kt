@file:JsModule("pako/lib/zlib/inflate.js")

package pako.zlib

external fun inflateInit(strm: zstream): Number
external fun inflateReset(strm: zstream): Number
external fun inflate(strm: zstream, flush: Number): Number
external fun inflateEnd(strm: zstream): Number
external fun inflateSetDictionary(strm: zstream, dictionary: ByteArray): Number
