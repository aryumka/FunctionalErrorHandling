import aryumka.option.Option
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import option.parseInsuranceRateQuote

class OptionParseTest : BehaviorSpec({
  given("With correct inputs") {
    val age = "35"
    val speedingTickets = "2"

    `when`("parseInsuranceRateQuote is called") {
      val option = parseInsuranceRateQuote(age, speedingTickets)

      then("it should return a value") {
        option shouldBe Option.Some(72.0)
      }
    }
  }

  given("With incorrect inputs") {
    val age = "35"

    val speedingTickets = "abc"

    `when`("parseInsuranceRateQuote is called") {
      val option = parseInsuranceRateQuote(age, speedingTickets)

      then("it should return None") {
        option shouldBe Option.None
      }
    }
  }
})