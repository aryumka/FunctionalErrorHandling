package option

import aryumka.option.Option
import aryumka.option.catches
import aryumka.option.domain.insuranceRateQuote
import aryumka.option.map2

fun parseInsuranceRateQuote(
  age: String,
  speedingTickets: String
): Option<Double> {
  val optAge: Option<Int> = catches { age.toInt() }

  val optTicket: Option<Int> = catches { speedingTickets.toInt() }

  return map2(optAge, optTicket) { a, b ->
    insuranceRateQuote(a, b)
  }
}

