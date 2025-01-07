package pako.zlib

@JsModule("pako/lib/zlib/zstream.js")
external class zstream {
    var input: dynamic
    var next_in: Number
    var avail_in: Number
    var total_in: Number
    var output: dynamic
    var next_out: Number
    var avail_out: Number
    var total_out: Number
    var msg: String
    //var state: dynamic
    //var data_type: Number
    var adler: Number
}
