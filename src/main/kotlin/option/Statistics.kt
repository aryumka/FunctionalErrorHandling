package aryumka.option

import kotlin.math.pow


/**
 * 평균을 계산함
 */
fun mean(xs: List<Double>): Option<Double> =
  if (xs.isEmpty()) Option.None
  else Option.Some(xs.sum() / xs.size)

/**
 * (x - m).pow(2)의 합을 계산함
 */
fun variance(xs: List<Double>): Option<Double> =
  // None.flatMap()
  mean(xs).flatMap { m ->
    mean(xs.map { x -> (x - m).pow(2) })
  }

val abs0: (Option<Double>) -> Option<Double> =
  Option.lift { kotlin.math.abs(it) }
