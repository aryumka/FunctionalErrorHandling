package either

import arrow.core.Either
import arrow.core.raise.either
import aryumka.option.domain.insuranceRateQuote


fun String.parseToInt(): Either<Throwable, Int> =
  Either.catch { this.toInt() }

fun parseInsuranceRateQuote(
  age: String,
  speedingTickets: String
): Either<Throwable, Double> =
  either {
    val a = age.parseToInt().bind()
    val b = speedingTickets.parseToInt().bind()
    insuranceRateQuote(a, b)
  }