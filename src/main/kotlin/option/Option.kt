package aryumka.option

/*
- 매칭을 사용해도 좋다. 하지만 map과 getorElse 이외의 모든 함수를 매칭 없이 구현 할 수 있다.
- map과 flatMap의 경우 타입 시그니처만으로 구현을 결정할 수 있다.
- getorElse는 Option이 Some인 경우 결과를 반환하지만 Option이 None인 경우 주어진 디폴트 값을 반환한다.
- orElse는 첫 번째 Option의 값이 정의된 경우(즉, Some인 경우) 그 option을 반환한다.
그렇지 않은 경우 두 번째 option을 반환한다.
*/
sealed class Option<out T> {
  data class Some<out T>(val value: T) : Option<T>()
  object None : Option<Nothing>()

  companion object {
    fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> =
      { it.map(f) }

    /**
     * 원소가 모두 Some인 리스트를 Some으로 반환하고, 그렇지 않은 경우 None을 반환한다.
     */
    fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
      traverse(xs) { it }
//      xs.foldRight(Option.Some(emptyList())) { x, acc ->
//        map2(x, acc) { a, b -> buildList {
//          add(a)
//          addAll(b)
//        } }
//      }

    /**
     * 원소에 f를 적용해서 모두 Some이면 리스트를 Some으로 반환하고, 그렇지 않은 경우 None을 반환한다.
     */
    fun <A, B> traverse(
      xa: List<A>,
      f: (A) -> Option<B>
    ): Option<List<B>> =
      xa.foldRight(Some(emptyList())) { x, acc ->
        map2(f(x), acc) { a, b ->
          buildList {
            add(a)
            addAll(b)
          }
        }
      }
  }
}

/**
 * Option이 None이 아닌 아닌 경우 f를 적용해 A 타입 값을 B 타입으로 변환함
 */
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
  when (this) {
    is Option.None -> Option.None
    is Option.Some -> Option.Some(f(value))
  }

/**
 * Option이 None인 경우, 디폴트 값을 반환함
 */
fun <A> Option<A>.getOrElse(default: () -> A): A =
  when (this) {
    is Option.None -> default()
    is Option.Some -> value
  }

/**
 * Option이 None이 아닌 경우, 실패할 수도 있는 f를 적용해 A 타입 값을 B 타입으로 변환함
 */
fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
  map { f(it) }.getOrElse { Option.None }
//  // 매칭을 사용하면 아래와 같이 구현
//  when (this) {
//    is Option.None -> Option.None
//    is Option.Some -> f(value)
//  }

/**
 * Option이 None인 경우, 디폴트 옵션을 반환함
 */
fun <A> Option<A>.orElse(default: () -> Option<A>): Option<A> =
  map { Option.Some(it) }.getOrElse(default)
//  // 매칭을 사용하면 아래와 같이 구현
//  when (this) {
//    is Option.None -> default()
//    is Option.Some -> this
//  }

/**
 * f를 만족하는 경우 Some을 반환하고, 아닌 경우 None을 반환함
 */
fun <A> Option<A>.filter(p: (A) -> Boolean): Option<A> =
  flatMap { if (p(it)) this else Option.None }
//  // 매칭을 사용하면 아래와 같이 구현
//  when (this) {
//    is Option.None -> Option.None
//    is Option.Some -> if (p(value)) this else Option.None
//  }

/**
 * 두 Option 값을 이항함수를 통해 조합하는 제네릭 함수. 두 Option 중 하나라도 None이면 None을 반환한다.
 *   - 이항함수? 두 개의 인자를 받아서 하나의 결과를 반환하는 함수
 * 그런데 None이 반환됐을 때, 두 Option 중 어떤 것이 None 인지 알수 없다.
 */
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
  a.flatMap { a -> b.map { b -> f(a, b) } }

/**
 * 지연인자를 사용한다. () -> A는 비엄격성을 표현.
 * 지연인자를 계산하려면 호출자가 호출해야 한다.
 */
fun <A> catches(a: () -> A): Option<A> =
  try {
    Option.Some(a())
  } catch (e: Exception) {
    Option.None
  }