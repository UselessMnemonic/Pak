package com.uselessmnemonic.pak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import shouldBeExactly

class BasicDeflateTests : FunSpec ({
    test("deflate empty byte array") {
        val stream = ZStream()

        stream.deflateInit(ZCompressionLevel.DefaultCompression)
        stream.setInput(ByteArray(0))
        stream.setOutput(ByteArray(256))

        stream.deflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly 0UL
        // TODO check ADLER
        stream.close()
    }

    test("deflate input to over-compression then re-inflate it") {

        // deflate block
        val stream = ZStream()
        var deflateOutput = ByteArray(1024)

        stream.deflateInit(ZCompressionLevel.DefaultCompression)
        stream.setInput(HelloWorldUncompressed)
        stream.setOutput(deflateOutput)

        stream.deflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly HelloWorldUncompressed.size.toULong()
        stream.totalOut shouldBeGreaterThan stream.totalIn

        stream.deflateEnd()
        deflateOutput = deflateOutput.sliceArray(IntRange(0, stream.totalOut.toInt() - 1))

        // inflate block
        val inflateOutput = ByteArray(HelloWorldUncompressed.size)
        stream.inflateInit()
        stream.setInput(deflateOutput)
        stream.setOutput(inflateOutput)

        stream.inflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly deflateOutput.size.toULong()
        stream.totalOut shouldBeExactly HelloWorldUncompressed.size.toULong()
        inflateOutput.take(HelloWorldUncompressed.size) shouldContainExactly HelloWorldUncompressed.asIterable()
        stream.inflateEnd()

        stream.close()
    }

    test("deflate input to well-compressed then re-inflate it") {

        // deflate block
        val stream = ZStream()
        var deflateOutput = ByteArray(1024)

        stream.deflateInit(ZCompressionLevel.DefaultCompression)
        stream.setInput(LoremIpsumUncompressed)
        stream.setOutput(deflateOutput)

        stream.deflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly LoremIpsumUncompressed.size.toULong()
        stream.totalOut shouldBeLessThan stream.totalIn

        stream.deflateEnd()
        deflateOutput = deflateOutput.sliceArray(IntRange(0, stream.totalOut.toInt() - 1))

        // inflate block
        val inflateOutput = ByteArray(LoremIpsumUncompressed.size)
        stream.inflateInit()
        stream.setInput(deflateOutput)
        stream.setOutput(inflateOutput)

        stream.inflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly deflateOutput.size.toULong()
        stream.totalOut shouldBeExactly LoremIpsumUncompressed.size.toULong()
        inflateOutput.take(LoremIpsumUncompressed.size) shouldContainExactly LoremIpsumUncompressed.asIterable()
        stream.inflateEnd()

        stream.close()
    }
})
