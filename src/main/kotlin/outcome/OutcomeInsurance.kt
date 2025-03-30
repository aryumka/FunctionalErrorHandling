package aryumka.option.Outcome

import aryumka.option.domain.insuranceRateQuote


fun String.parseToInt(): Outcome<OutcomeError, Int> =
  catches { this.toInt() }

fun parseInsuranceRateQuote(
  age: String,
  speedingTickets: String
): Outcome<OutcomeError, Double> {
  return age.parseToInt().flatMap { a ->
    speedingTickets.parseToInt().flatMap { b ->
      insuranceRateQuote(a, b).asSuccess()
    }
  }
}