import aryumka.option.Outcome.Failure
import aryumka.option.Outcome.Outcome
import aryumka.option.Outcome.OutcomeError
import aryumka.option.Outcome.Success
import io.kotest.matchers.shouldBe
import kotlin.test.DefaultAsserter.fail

infix fun <T> Outcome<*, T>.shouldBeSuccess(expected: T) =
  this shouldBe Success(expected)

infix fun <E : OutcomeError> Outcome<E, *>.shouldBeFailure(expectedMsg: String) =
  when (this) {
    is Failure -> error.msg shouldBe expectedMsg
    is Success -> fail("Expected Failure but got Success(${value})")
  }
