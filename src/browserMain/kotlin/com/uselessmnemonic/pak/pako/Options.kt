package com.uselessmnemonic.pak.pako

interface DeflateOptions {
    val level: Int?
    val windowBits: Int?
    val memLevel: Int?
    val strategy: Int?
    val dictionary: dynamic
    val raw: Boolean?
    val chunkSize: Int?
    val gzip: Boolean?
    val header: Header?

    interface Header {
        val text: Boolean?
        val time: Int?
        val os: Int?
        val extra: IntArray?
        val name: String?
        val comment: String?
        val hcrc: Boolean?
    }
}

interface InflateOptions {
    val windowBits: Int?
    val dictionary: dynamic
    val raw: Boolean?
    val to: String?
    val chunkSize: Int?
}
