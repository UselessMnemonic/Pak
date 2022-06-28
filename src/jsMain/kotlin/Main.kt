import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import pako.*

fun main() {
    val deflate = Deflate(null)
    deflate.push("Hello".encodeToByteArray(), false)
    deflate.push(", ".encodeToByteArray(), false)
    deflate.push("World!".encodeToByteArray(), true)

    if (deflate.err != 0) {
        console.log(deflate.msg)
        return
    }

    val output = deflate.result!!

    val inflate = Inflate(null)
    inflate.push(output, true)

    if (inflate.err != 0) {
        console.log(inflate.msg)
        return
    }

    val result = when (inflate.result) {
        is ByteArray -> inflate.result.decodeToString()
        is Uint8Array -> (inflate.result.unsafeCast<ByteArray>()).decodeToString()
        is String -> inflate.result
        else -> "ERROR!!"
    }

    console.log(result)
}
