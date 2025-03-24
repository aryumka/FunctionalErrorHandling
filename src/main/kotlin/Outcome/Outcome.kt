package aryumka.option.Outcome

interface OutcomeError {
  val msg: String
}

sealed class Outcome<out E: OutcomeError, out T> {
  fun <U> transform(f: (T) -> U): Outcome<E, U> =
    when (this) {
    is Success -> Success(f(value))
    is Failure -> this
  }

  fun <T, U, E: OutcomeError> lift(f: (T) -> U): (Outcome<E, T>) -> Outcome<E, U> =
    { o -> o.transform{ f(it) } }
}

data class Success<out T>(val value: T) : Outcome<Nothing, T>()
data class Failure<out E: OutcomeError>(val error: E) : Outcome<E, Nothing>()

fun <E: OutcomeError> E.asFailure(): Outcome<E, Nothing> = Failure(this)

fun <T> T.asSuccess(): Outcome<Nothing, T> = Success(this)

fun <E: OutcomeError, T, U> Outcome<E, T>.flatMap(f: (T) -> Outcome<E, U>): Outcome<E, U> =
  when (this) {
    is Success -> f(value)
    is Failure -> this
  }

// 비지역 반환: 함수가 반환되는 위치가 아닌 함수가 호출되는 위치로 반환을 전달하는 것을 의미.
inline fun <E: OutcomeError, T> Outcome<E, T>.onFailure(
  exitBlock: (E) -> Nothing
): T =
  when (this) {
    is Success -> value
    is Failure -> exitBlock(error)
  }

fun <T> catches(f: () -> T): Outcome<OutcomeError, T> =
  try {
    f().asSuccess()
  } catch (e: Exception) {
    Failure(object : OutcomeError {
      override val msg: String = e.message ?: "An error occurred"
    })
  }
