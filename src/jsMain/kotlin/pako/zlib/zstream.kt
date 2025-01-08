package pako.zlib

import org.khronos.webgl.Uint8Array

@JsModule("pako/lib/zlib/zstream.js")
external class zstream {
    var input: Uint8Array?
    var next_in: Number
    var avail_in: Number
    var total_in: Number
    var output: Uint8Array?
    var next_out: Number
    var avail_out: Number
    var total_out: Number
    var msg: String?
    //var state: dynamic
    //var data_type: Number
    var adler: Number
}
