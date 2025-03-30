package benchmark

import either.Either
import org.openjdk.jmh.annotations.*
import kotlin.random.Random

@State(Scope.Thread)
open class TryVsEitherBenchmark {

  private val random = Random(1234)
  private val errorProbability = 0.1
  private lateinit var errorScenarios: BooleanArray
  private var index = 0

  @Setup
  fun setup() {
    // 미리 랜덤 시나리오 생성
    errorScenarios = BooleanArray(10_000) { random.nextDouble() < errorProbability }
  }

  private fun nextScenario(): Boolean {
    val result = errorScenarios[index]
    index = (index + 1) % errorScenarios.size
    return result
  }

  @Benchmark
  fun eitherMixed(): Either<CustomError, Int> {
    return if (nextScenario()) Either.Left(CustomError("fail"))
    else Either.Right(1 + 1)
  }

  @Benchmark
  fun tryCatchMixed(): Int {
    return try {
      if (nextScenario()) throw RuntimeException("fail")
      1 + 1
    } catch (e: Exception) {
      -1
    }
  }

  @Benchmark
  fun tryWithNoCatch(): Int {
    return if (nextScenario()) throw RuntimeException("fail")
    else 1 + 1
  }

  @Benchmark
  fun plain(): Int = 1 + 1
}

@JvmInline
value class CustomError(val message: String)