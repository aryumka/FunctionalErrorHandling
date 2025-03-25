import aryumka.option.Outcome.*
import aryumka.option.domain.insuranceRateQuote
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlin.test.DefaultAsserter.fail

class InsuranceQuoteTest : StringSpec({
  "should succeed when both inputs are valid" {
    val result = parseInsuranceRateQuote("30", "2")
    result shouldBeSuccess 62.0
  }


  "should fail fast and not call insuranceRateQuote when age is invalid" {
    val age = "abc"
    val tickets = "2"

    // top-level 함수 mocking
    mockkStatic(::insuranceRateQuote)
    every { insuranceRateQuote(any<Int>(), any<Int>()) } returns 0.0

    val result = parseInsuranceRateQuote(age, tickets)

    // 결과 검증
    result shouldBeFailure "For input string: \"abc\""

    // 호출되지 않았는지 확인
    verify(exactly = 0) { insuranceRateQuote(any(), any()) }

    unmockkStatic(::insuranceRateQuote)
  }
})

infix fun <T> Outcome<*, T>.shouldBeSuccess(expected: T) =
  this shouldBe Success(expected)

infix fun <E : OutcomeError> Outcome<E, *>.shouldBeFailure(expectedMsg: String) =
  when (this) {
    is Failure -> error.msg shouldBe expectedMsg
    is Success -> fail("Expected Failure but got Success(${value})")
  }
