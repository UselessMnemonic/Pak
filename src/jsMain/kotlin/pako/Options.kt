package pako

data class DeflateOptions(
    val level: Int = Constants.Z_DEFAULT_COMPRESSION,
    val method: Int = Constants.Z_DEFLATED,
    val chunkSize: Int = 16384,
    val windowBits: Int = 15,
    val memLevel: Int = 8,
    val strategy: Int? = Constants.Z_DEFAULT_STRATEGY
)

data class InflateOptions (
    val chunkSize: Int = 1024 * 64,
    val windowBits: Int = 15,
    val to: String = ""
)
