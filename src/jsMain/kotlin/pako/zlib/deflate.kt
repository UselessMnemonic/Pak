@file:JsModule("pako/lib/zlib/deflate.js")

package pako.zlib

import org.khronos.webgl.Uint8Array

external fun deflateInit(strm: zstream, level: Number): Number
external fun deflateReset(strm: zstream): Number
external fun deflateParams(strm: zstream, level: Number, strategy: Number): Number
external fun deflateGetDictionary(strm: zstream, dictionary: Uint8Array?): Array<Number>
external fun deflate(strm: zstream, flush: Number): Number
external fun deflateEnd(strm: zstream): Number
external fun deflateSetDictionary(strm: zstream, dictionary: Uint8Array?): Number
