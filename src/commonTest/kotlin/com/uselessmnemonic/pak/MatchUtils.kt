import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe

fun exactly(x: ULong) = object : Matcher<ULong> {
    override fun test(value: ULong) = MatcherResult(
        value == x,
        { "$value should be equal to $x" },
        { "$value should not be equal to $x" })
}

infix fun ULong.shouldBeExactly(x: ULong): ULong {
    this shouldBe exactly(x)
    return this
}
