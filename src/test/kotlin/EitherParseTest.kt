import arrow.core.Either
import aryumka.option.domain.insuranceRateQuote
import either.parseInsuranceRateQuote
import either.parseToInt
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

class EitherParseTest : BehaviorSpec({
  given("With correct inputs") {
    val age = "35"
    val speedingTickets = "2"

    `when`("parseInsuranceRateQuote is called") {
      val either = parseInsuranceRateQuote(age, speedingTickets)

      then("it should return a value") {
        either.isRight() shouldBe true
        either.map { it shouldBe 72.0 }
      }
    }
  }

  given("With incorrect speedingTickets") {
    val age = "35"
    val speedingTickets = "abc"

    `when`("parseInsuranceRateQuote is called") {
      val either = parseInsuranceRateQuote(age, speedingTickets)

      then("it should return an error") {
        either.isLeft() shouldBe true
        either.mapLeft { it shouldBe NumberFormatException("For input string: \"abc\"") }
      }
    }
  }

  given("With incorrect age") {
    val age = "cde"
    val speedingTickets = "2"
    mockkStatic(::insuranceRateQuote)
    every { insuranceRateQuote(any<Int>(), any<Int>()) } returns 0.0
    mockkStatic("either.EitherInsuranceKt")
    every { speedingTickets.parseToInt() } returns Either.Right(123)

    `when`("parseInsuranceRateQuote is called") {
      val either = parseInsuranceRateQuote(age, speedingTickets)

      then("it should return an error") {
        either shouldBeLeft NumberFormatException("For input string: \"cde\"")
      }

      then("it should not call insuranceRateQuote") {
        verify(exactly = 0) { insuranceRateQuote(any<Int>(), any<Int>()) }
      }

      then("it should not call parseToInt") {
        verify(exactly = 0) { speedingTickets.parseToInt() }
      }
    }
  }
})