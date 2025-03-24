package either

sealed class Either<out E, out A> {
  data class Left<out E>(val value: E) : Either<E, Nothing>()
  data class Right<out A>(val value: A) : Either<Nothing, A>()
}

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
  when (this) {
    is Either.Left -> this
    is Either.Right -> Either.Right(f(this.value))
  }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
  when (this) {
    is Either.Left -> this
    is Either.Right -> f(this.value)
  }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
  when (this) {
    is Either.Left -> f()
    is Either.Right -> this
  }

fun <A> catches(f: () -> A): Either<Exception, A> =
  try {
    Either.Right(f())
  } catch (e: Exception) {
    Either.Left(e)
  }