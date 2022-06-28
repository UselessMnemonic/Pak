package uselessmnemonic.pak

expect class Deflater(level: Int) {
    val level: Int

    fun deflate()

    fun push(chunk: ByteArray)
    fun push(chunk: ByteArray, offset: Int, size: Int)
}
