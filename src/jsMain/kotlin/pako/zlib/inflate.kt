@file:JsModule("pako/lib/zlib/inflate.js")

package pako.zlib

import org.khronos.webgl.Uint8Array

external fun inflateInit(strm: zstream): Number
external fun inflateReset(strm: zstream): Number
external fun inflate(strm: zstream, flush: Number): Number
external fun inflateEnd(strm: zstream): Number
external fun inflateSetDictionary(strm: zstream, dictionary: Uint8Array): Number
