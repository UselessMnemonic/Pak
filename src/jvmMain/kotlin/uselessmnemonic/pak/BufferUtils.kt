package uselessmnemonic.pak

import java.nio.ByteBuffer

private fun ByteBuffer.ensureEnoughCapacity(atLeast: Int): ByteBuffer {
    val remaining = remaining()
    if (atLeast <= remaining) return this

    val required = atLeast - remaining
    val factor = if (required % 512 == 0) {
        required / 512
    } else {
        (required / 512) + 1
    }

    val newCapacity = capacity() + (factor * 512)
    val newBuffer = if (hasArray()) {
        ByteBuffer.allocate(newCapacity)
    } else {
        ByteBuffer.allocateDirect(newCapacity)
    }
    newBuffer.put(0, this, 0, position())
    return newBuffer
}
