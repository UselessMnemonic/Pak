@file:JsModule("pako/lib/zlib/deflate.js")

package pako.zlib

external fun deflateInit(strm: zstream, level: Number): Number
external fun deflateReset(strm: zstream): Number
external fun deflate(strm: zstream, flush: Number): Number
external fun deflateEnd(strm: zstream): Number
external fun deflateSetDictionary(strm: zstream, dictionary: ByteArray): Number
