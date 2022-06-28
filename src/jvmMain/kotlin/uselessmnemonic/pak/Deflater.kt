package uselessmnemonic.pak

import java.nio.ByteBuffer

actual class Deflater actual constructor(actual val level: Int) {

    private val deflater = java.util.zip.Deflater(0)

    actual fun push(chunk: ByteArray, offset: Int, size: Int) {
        if (offset < 0) throw IllegalArgumentException("offset cannot be negative")
        if (offset > chunk.size) throw IllegalArgumentException("offset cannot exceed size of chunk")

        if (size < 0) throw IllegalArgumentException("size cannot be negative")
        val remaining = chunk.size - offset
        if (size > remaining) throw IllegalStateException("size cannot exceed remaining")


    }

    actual fun push(chunk: ByteArray) {
    }

    actual fun deflate(): ByteArray {
        val input = inBuffer.slice(0, inBuffer.position())
        deflater.setInput(input)

        var outBuffer = ByteBuffer.allocateDirect(0)
        do {
            outBuffer = outBuffer.ensureEnoughCapacity(512)
            deflater.deflate(outBuffer)
        } while (!deflater.finished())

        val result = ByteArray(outBuffer.position())
        outBuffer.get(0, result)
        return result
    }
}
