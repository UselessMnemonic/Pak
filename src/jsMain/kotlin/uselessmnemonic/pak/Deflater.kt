package uselessmnemonic.pak

actual class Deflater {

    private val deflater = pako.Deflate(null)

    actual fun push(chunk: ByteArray) {
        deflater.push(chunk, false)
    }

    actual fun push(chunk: ByteArray, offset: Int, size: Int) {
        deflater.push(chunk.sliceArray(offset..(offset+size)), false)
    }

    actual fun deflate(): ByteArray {
        deflater.push(ByteArray(0), true)
        return deflater.result.unsafeCast<ByteArray>()
    }
}
