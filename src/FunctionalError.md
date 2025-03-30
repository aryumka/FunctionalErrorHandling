# 예외는 던져져야 할까?
# 1. 서론
한가로운 주말 아침, 유튜브 알고리즘이 날 한 영상으로 이끌었다.
왜 리눅스의 창시자인 리누스 토르발즈가 커널 개발에 C++를 절대 반대하는지에 대한 영상이었다. [리누스 토르발즈의 C++ 혐오](https://yarchive.net/comp/linux/c++.html)는 유명한데, 심지어 **근본적으로** 잘못된 언어라며, 대표적인 예로 **예외 처리(Exception Handling)** 를 들었다.
> C++의 예외 처리 어쩌구는 근본적으로 망가졌다. 특히 커널 개발 용으로는 아예 박살이 나있다.
> (the whole C++ exception handling thing is fundamentally broken. It's _especially_ broken for kernels.)
뒤통수를 맞은 듯 띵했다. 예외를 안던지고 개발을 할 수 있나?
그렇다면 C나 전통적인 절차적 개발에서는 **예외**라는 개념이 없었던 걸까?
# 2. 절차적 예외처리
그렇다. C에는 자바나 C++ 등 객체지향 언어들이 갖고 있는 `try carch`나 `Exception` 객체가 없다.
대신 에러 상태를 리턴 값으로 내려주고 호출부에서 이를 검증하는 방식이 일반적이다.
```c
int main() {
    FILE *file = fopen("data.txt", "r");
    if (file == NULL) {
        perror("파일 열기 실패");
        return 1; // 에러 코드 반환
    }

    // 파일 작업 수행
    fclose(file);
    return 0; // 정상 종료
}
```
위와 같은 **절차적 프로그래밍 스타일**은 자바의 레거시 코드에서도 흔히 볼 수 있다.
이런 방식의 에러 처리는 명확한 한계를 갖고 있다.
- 호출부에서 정상 값과 오류 값을 혼동해 버그 가능성
- 에러가 난 건 알아도 에러의 원인을 알 수 없음

만약 에러의 원인을 알고 싶다면 응답 시 Map 같은 구조체를 사용하여 에러 메시지를 `String`으로 반환할 수 있다.
```java
public class Divider {
  public static Map<String, Object> readFile(String path) {
    Map<String, Object> result = new HashMap<>();
    Path filePath = Paths.get(path);

    if (!Files.exists(filePath)) {
      return Map.of("success", false, "error", "파일이 존재하지 않습니다.");
    }

    if (!Files.isRegularFile(filePath)) {
      return Map.of("success", false, "error", "일반 파일이 아닙니다.");
    }

    if (!Files.isReadable(filePath)) {
      return Map.of("success", false, "error", "파일을 읽을 수 없습니다.");
    }

    List<String> lines = Files.readAllLines(filePath);
    
    return Map.of("success", true, "content", String.join("\n", lines));
  }
}
```
실제 외부 API와 통신할 때도 이런 식의 응답 방식을 자주 볼 수 있다.
만약 우리가 호출한 외부 API에서 null이나 비정상 값만 리턴한다면 우리의 서비스 흐름은 매번 알 수 없는 이유로 중단될 것이다. 그리고 화가 많이 날 것이다.
~~인포텍에서 실패 시 마다 흐름을 중단시켜버린다면?~~
오류 원인을 문자열로 받는다 해도 여전히 몇 가지 문제가 있다.
- 예외를 Map으로 넘기는 순간, 코드 전체가 Map을 요구하게 됨 -> 전염성
- 호출 후 결과를 사용하기 전 매번 if로 정상 값 검사 또는 가드문`guard clause` 사용해야 함 -> 보일러 플레이트 증가
- 에러 원인이 문자열로 주어져 상황에 맞는 대응이 어려움 -> 유연성 저하
- 에러가 발생한 경우에도 호출부에서 처리를 강제할 수 없고 복구가 불가능한 상황에서도 계속 진행을 시도할 수 있음 -> 예외 처리의 강제성 부재

**컴파일 타임에 알 수가 없음.** 실수로 처리를 빼먹을 수 있다는 것이 치명적.

어차피 실패인데 왜 끝까지 실행되어야 할까? 귀찮게 리턴을 꼬박꼬박 해줘야만 하는 걸까.
가끔은 그냥 빨리 포기해버리는 것이 나을 때가 있는 법. 빠른 손절은 익절이라고 누가 말했던가.

# 3. 자바의 예외처리 방식
## Fail fast and Throw
리눅스 아저씨가 극혐하던 C++나 자바 등 객체지향 언어에서는 예외를 던지고`throw` 받는`try catch` 방식을 사용한다.
자바는 특히나 **Fail Fast** 철학을 따른다. **프로그램이 잘못되었을 때 빠르게 실패하도록 하는 것**이다. `Return or Fail`
~~성공이 아니라면 모두 실패죠? 노빠꾸 상남자죠?~~
문제가 될 수 있는 상황을 조기에 감지하고, 빨리 실패하도록 하여 버그를 더 빨리 드러나게 하는 방식이다. 
~~더 이상의 자세한 설명은 생략한다.~~
`NullPointerException`이나 `IllegalArgumentException` 등을 통해 의도적으로 프로그램을 중단시켜 문제를 조기에 감지하도록 유도한다.

자바는 예외를 두 가지로 나눈다. `Checked Exception`과 `Unchecked Exception`이다.
## Checked Exception

`Checked Exception`은 C++ 등 대부분 객체지향 언어에는 존재하지 않는 자바만의 독특한 예외 처리 방식이다.
주로 일반적, 예측가능한 비즈니스 케이스에서 사용하라고 의도된 디자인이다. 그 증거로 스프링에서는 롤백도 되지 않는다.
```java
@Transactional
public void doSomething() throws IOException {
    try {
        // 파일 처리 등
    } catch (IOException e) {
        throw e; // Checked Exception → Spring은 롤백 안 함
    }
}
```
`Checked Exception`은 **컴파일러가 강제**로 예외 처리를 요구한다. 그래서 `Checked Exception`은 명시적이다. 호출하는 쪽에서 살패를 예측할 수 있게 한다.
이런 ~ 일이 발생할 수 있으니 예외 처리를 해라 라는 식.

API가 어떤 예외를 던질 수 있는지 문서화하는 효과가 있다.

대표적인 예로 `IOException`이 있다.

문제는 원격 API로 인해 발생한 예외가 domain 경계를 침범, 전염된다.
호출부에서 무의미한 `try catch`가 남용. ~~ 등등

이러한 이유로 이제는 `Checked Exception`을 사용하지 않는 추세이고 심지어 사용하지 말라고 권장한다.

비록 실패한 디자인이라 평가받지만 컴파일 타임에 보장되는 명시적인 예외를 표현하고 싶었던 자바 디자이너들의 의도는 이해할 수 있다. 
뒤에서 더 자세히 다루게 될 계약 위반에 대한 이야기를 생각해보면 이해가 될 것이다.

## Spring과 Exception
지금의 자바 트렌드는 모두 런타임 에러를 던지고 전역적 예외처리 핸들러로 처리하는 것이다. `ControllerAdvice`를 사용하여 전역적으로 예외를 처리할 수 있다.
```java
```
런타임 에러의 가장 큰 이점 중 하나는 자동 롤백이 주는 편안함이다.

자바로 작성한 프로그램에서 어떤 얘외가 발생할

# 4. 예외를 바라보는 다른 시각
## 계약 위반?
Method Signature(서명). **컴파일러가 메서드를 정확히 찾아가기 위한 고유 식별자**
**리턴 타입만 가지고 어떤 메서드인지 정확히 판단할 수 없어** 리턴 타입은 포함되지 않음.
하지만 난 인터페이스라는 계약에서 메서드의 고유한 서명이라고 봄.

자바 언어 디자이너들도 비슷한 생각으로 `Checked Exception`을 만들었다.
```java
public void doSomething() throws IOException; // 메서드 시그니처에서 IOException을 던진다는 계약
```

제어 상실과 예외 전파 - 처리되지 않은 예외를 던지는 것은 제어를 상실하는 것이다. 코드 흐름도 예측할 수 없게 만듦.

일단 던지고 보기 때문에 자신이 작성한 프로그램에서 어떤 상황에 어떤 예외가 발생할 지 정확하게 예측할 수 있는 경우는 드물다. 예외에 대한 문서화가 되어있을리도 없다.
이 API에서 어떤 종류의 오류가 발생할 수 있나요? 라고 물어보면 대부분의 경우 "모르겠다"라고 대답할 것이다. 그걸 파악하기 위해서는 코드를 타고 타고 들어가 자신이 던지는 모든 예외는 물론 외부 라이브러리에서 던지는 런타임 예외까지 모두 파악해야 한다.


만약 모니터링 중 예외가 발생했다는 알림을 받는다면 이게 정말 예상 가능한 예외인지, 아니면 예외가 발생한 상황을 예측하지 못한 것인지 판단하기 어렵다.
그 말은 즉 정말 처리되어야 하는 예외도 무시될 수 있다는 것이다.
모니터링 채널에서 안볼거라면 그냥 처음부터 던지지 말아야 한다. 일단 던지고 잡는다면? Checked Exception의 무의미한 던지기와 잡기가 될 것이다.

이런 경우를 함수형 프로그래밍에서는 **부수 효과**라고 부름. (함수가 결과를 리턴하는 것 이외에 다른 일을 하는 것)

예외를 아예 던지지 않는 것은 아니다. 예를 들어 프로그램 부팅 시 포트가 이미 사용 중이라거나 하는 복구가 불가능한 상황이라면 프로그램을 종료하는 것이 더 낫다.
하지만 찾고 있는 유저의 ID가 DB에 없는 상황이거나 사용자가 입력한 날짜가 존재하지 않는 날짜라면? 이건 완벽하게 예상 가능한 비즈니스 케이스에 가깝다.

## 값으로서의 예외
### Optional
자바 8부터 등장한 Optional<T>는 값이 있을 수도 있고 없을 수도 있다는 것을 표현하는 타입이다. null을 직접 다루지 않고, 안전한 방식으로 결과를 표현할 수 있다.
```java
Optional<String> name = findUserNameById(id);
name.ifPresent(System.out::println);
```

하지만 Optional은 실패에 대한 이유를 담지 못한다는 한계가 있다. 단순한 존재 여부만 표현할 수 있기 때문에, "왜 실패했는가"가 중요할 때는 적절하지 않다.
### Either
### Higher Kinded Type
### 오류 처리와 복구 패턴
- 기본값 제공 (orElse, getOrElse)
- 대체 흐름으로 분기 (fold, mapLeft, recover)
- 재시도 (retry)
- 지연된 처리 (lazy fallback)
- 실패 누적 (Validation)
### 값으로서의 예외 vs Checked Exception

## 예외는 던지기 싫지만 롤백은 됐으면 좋겠어
## 그래도 예외는 던지고 싶어 - 번외: Try Catch의 비용

# 결론
예외는 예외적인 상황에서만
신뢰가 깨졌다면 던지고, 규칙이 어긋났다면 값으로 말하자.
