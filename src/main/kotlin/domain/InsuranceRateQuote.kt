package aryumka.option.domain

fun insuranceRateQuote(
  age: Int,
  speedingTickets: Int
): Double =
  (age * 2.0) + speedingTickets
