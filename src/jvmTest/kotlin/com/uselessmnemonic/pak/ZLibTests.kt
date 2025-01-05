package com.uselessmnemonic.pak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith

object ZLibTests : FunSpec({
    test("version should be 1.x") {
        ZLib.version shouldStartWith "1."
    }
})
