import aryumka.option.Outcome.*
import aryumka.option.domain.insuranceRateQuote
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify

class InsuranceQuoteTest : StringSpec({
  "should succeed when both inputs are valid" {
    val result = parseInsuranceRateQuote("30", "2")
    result shouldBeSuccess 62.0
  }

  "should fail fast and not call insuranceRateQuote when age is invalid" {
    val age = "abc"
    val tickets = "2"

    mockkStatic(::insuranceRateQuote)
    every { insuranceRateQuote(any<Int>(), any<Int>()) } returns 0.0

    val result = parseInsuranceRateQuote(age, tickets)

    result shouldBeFailure "For input string: \"abc\""

    verify(exactly = 0) { insuranceRateQuote(any(), any()) }

    unmockkStatic(::insuranceRateQuote)
  }
})