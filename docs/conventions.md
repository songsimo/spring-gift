# Code Conventions

## Optional 처리

`orElse(null)` 사용 금지. null 체크 분기가 Controller에 흩어지고, 예외 의미가 사라진다.

```java
// 금지
Product product = productRepository.findById(id).orElse(null);
if (product == null) return ResponseEntity.notFound().build();

// 사용
Product product = productRepository.findById(id)
    .orElseThrow(() -> new NotFoundException("Product not found"));
```

예외가 `GlobalExceptionHandler`에서 자동으로 404 응답으로 변환된다.

## ResponseEntity 타입

`ResponseEntity<?>` 사용 금지. API 계약이 불분명해지고 타입 안전성이 사라진다.

```java
// 금지
public ResponseEntity<?> createOrder(...) { ... }

// 사용
public ResponseEntity<OrderResponse> createOrder(...) { ... }
public ResponseEntity<Page<OrderResponse>> getOrders(...) { ... }
```

## 생성자 주입

Spring Boot 3에서 단일 생성자는 자동 주입된다. `@Autowired` 불필요.

```java
// 금지
@Autowired
public MemberController(MemberRepository memberRepository) { ... }

// 사용
public MemberController(MemberRepository memberRepository) { ... }
```

## 주석 작성 기준

코드가 **무엇을 하는지**는 잘 지어진 이름이 설명한다. 주석은 **왜** 그렇게 했는지 비자명한 경우에만 작성한다.

```java
// 금지 — 코드가 이미 설명함
// auth check
var member = authenticationResolver.extractMember(authorization);

// 사용 — 숨겨진 제약을 설명
// Kakao access token이 없으면 알림 시도하지 않음 (카카오 미연동 회원)
if (member.getKakaoAccessToken() == null) return false;
```

Javadoc(`/** */`)은 외부 라이브러리 공개 API가 아닌 한 작성하지 않는다.

## 에러 메시지 언어

클라이언트 응답 메시지: 영어 (API 사용자가 다양할 수 있음)
서버 로그 메시지: 한국어 허용

```java
// 클라이언트 응답
throw new NotFoundException("Product not found");

// 서버 로그
log.warn("상품 조회 실패 — id: {}", id);
```

## 구조 변경 vs 동작 변경 분리

리팩토링(구조 변경)과 기능 수정(동작 변경)은 반드시 별도 커밋으로 분리한다.

| 종류 | 정의 | 검증 방법 | 커밋 타입 |
|------|------|----------|----------|
| 구조 변경 | 외부 동작 유지, 내부 구조 개선 | 기존 테스트 통과 | `refactor` |
| 동작 변경 | 새 기능 추가 또는 버그 수정 | TDD (실패 테스트 먼저) | `feat` / `fix` |

같은 커밋에 두 가지를 섞으면 어떤 변경이 테스트 실패를 유발했는지 추적이 불가능해진다.
