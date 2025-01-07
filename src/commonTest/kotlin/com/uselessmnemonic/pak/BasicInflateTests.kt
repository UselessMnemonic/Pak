package com.uselessmnemonic.pak

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.FunSpec
import shouldBeExactly

class BasicInflateTests : FunSpec ({
    test("inflate empty byte array") {
        val stream = ZStream()
        stream.inflateInit()
        stream.setInput(EmptyStringCompressed) // e.g. empty string
        stream.setOutput(ByteArray(1))

        stream.inflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalOut shouldBeExactly 0UL
        // TODO check ADLER
        stream.close()
    }

    test("inflate over-compressed input") {
        val stream = ZStream()
        val inflateOutput = ByteArray(HelloWorldUncompressed.size)

        stream.inflateInit()
        stream.setInput(HelloWorldCompressed)
        stream.setOutput(inflateOutput)

        stream.inflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly HelloWorldCompressed.size.toULong()
        stream.totalOut shouldBeExactly HelloWorldUncompressed.size.toULong()
        inflateOutput.take(HelloWorldUncompressed.size) shouldContainExactly HelloWorldUncompressed.asList()
        // TODO check ADLER
        stream.close()
    }

    test("inflate well-compressed input") {
        val stream = ZStream()
        val inflateOutput = ByteArray(LoremIpsumUncompressed.size)
        stream.inflateInit()
        stream.setInput(LoremIpsumCompressed)
        stream.setOutput(inflateOutput)

        stream.inflate(ZFlush.Finish) shouldBe ZResult.StreamEnd
        stream.totalIn shouldBeExactly LoremIpsumCompressed.size.toULong()
        stream.totalOut shouldBeExactly LoremIpsumUncompressed.size.toULong()
        // TODO check ADLER
        inflateOutput.take(LoremIpsumUncompressed.size) shouldContainExactly LoremIpsumUncompressed.asList()
        stream.close()
    }
})
