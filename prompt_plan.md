# Prompt Plan — spring-gift 개선 작업

> 각 작업은 TDD 사이클(Red → Green → Refactor)을 따른다.  
> 하나의 작업이 완전히 완료(테스트 통과 + README 로그 추가)된 뒤 다음 작업으로 이동한다.

---

## 분석 기준일: 2026-05-24

---

## 발견된 문제 목록

| # | 분류 | 위치 | 문제 |
|---|---|---|---|
| 1 | Bug | `CategoryService.java:32` | `update()` 메서드 닫는 `}` 들여쓰기 오류 (git diff 확인됨) |
| 2 | Architecture | `AdminProductController.java` | `ProductRepository`, `CategoryRepository`를 직접 주입 — 서비스 레이어 우회 |
| 3 | Data Integrity | `OrderService`, `CategoryService`, `MemberService`, `KakaoAuthService` | 복수 DB 쓰기 메서드에 `@Transactional` 누락 |
| 4 | Error Handling | 전체 컨트롤러 | 예외 처리 방식이 컨트롤러마다 다름 (`try-catch` / `@ExceptionHandler` / 미처리 혼재) |
| 5 | Missing Feature | `OrderService.createOrder()` | 주문 완료 후 찜 자동 제거 미구현 (컨트롤러 주석에 step 6 언급만 있음) |
| 6 | Architecture | `KakaoAuthService.java` | `MemberRepository`를 직접 사용 — 회원 관련 영속성 처리가 `MemberService` 바깥에 존재 |
| 7 | Architecture | `AuthenticationResolver.java` | `MemberRepository`를 직접 사용 — `MemberService`를 통해야 함 |
| 8 | Test Coverage | `AdminMemberController`, `AdminProductController` | 컨트롤러 단위 테스트 전무 |
| 9 | Data Safety | `wish` 테이블 | `(member_id, product_id)` 복합 유니크 제약 없음 — 동시 요청 시 중복 찜 가능 |
| 10 | Code Quality | `MemberService.findAll()`, `findById()` | 엔티티(`Member`)를 직접 반환 — DTO 변환 없이 password 필드가 외부에 노출될 위험 |

---

## 작업 순서 및 프롬프트

---

### Task 1 — `CategoryService.update()` 포맷 버그 수정

**문제**: `update()` 메서드의 닫는 `}` 가 메서드 내부가 아닌 클래스 레벨에 위치함.

**작업 범위**: `CategoryService.java` 1개 파일  
**테스트**: 기존 `CategoryServiceTest` 5개 테스트 그대로 통과 확인  
**완료 조건**: `./gradlew ktlintCheck` 통과, 기존 테스트 전부 GREEN

**프롬프트**:
```
CategoryService.java의 update() 메서드 닫는 중괄호가 잘못된 들여쓰기로 되어 있어.
현재 32번째 줄의 `}` 가 메서드 안이 아닌 클래스 레벨에 위치해 있어.
들여쓰기를 4칸(스페이스)으로 맞춰 수정해줘.
수정 후 기존 CategoryServiceTest 5개 테스트가 모두 통과하는지 확인하고, README에 작업 로그를 추가해.
```

---

### Task 2 — 서비스 메서드에 `@Transactional` 추가

**문제**: 복수 DB 쓰기가 포함된 메서드들이 트랜잭션 보장 없이 동작 중.  
원자성이 없으면 옵션 차감 후 포인트 차감 실패 시 재고만 줄어드는 상황 발생 가능.

**영향 범위**:
| 클래스 | 메서드 | DB 쓰기 횟수 |
|---|---|---|
| `OrderService` | `createOrder()` | option save + member save + order save (3회) |
| `CategoryService` | `update()` | findById + save (2회) |
| `MemberService` | `updateMember()` | findById + save (2회) |
| `MemberService` | `chargePoint()` | findById + save (2회) |
| `KakaoAuthService` | `processCallback()` | findByEmail + save (2회) |

**테스트**: 기존 서비스 단위 테스트 전부 통과 확인  
**완료 조건**: 위 5개 메서드에 `@Transactional` 추가, 기존 테스트 GREEN 유지

**프롬프트**:
```
여러 DB 쓰기 작업을 하나의 메서드에서 수행하지만 @Transactional이 없는 메서드들에 트랜잭션 어노테이션을 추가해줘.

대상 메서드:
- OrderService.createOrder() — option, member, order 3회 쓰기
- CategoryService.update() — 조회 후 저장
- MemberService.updateMember() — 조회 후 저장
- MemberService.chargePoint() — 조회 후 저장
- KakaoAuthService.processCallback() — 조회-or-생성 후 저장

각 클래스에 import org.springframework.transaction.annotation.Transactional을 추가하고,
해당 메서드에만 @Transactional을 붙여줘. 클래스 레벨 어노테이션은 사용하지 마.
수정 후 기존 테스트가 모두 통과하는지 확인하고, README에 작업 로그를 추가해.
```

---

### Task 3 — `AdminProductController` 서비스 레이어 분리 완성

**문제**: `AdminProductController`가 `ProductRepository`와 `CategoryRepository`를 직접 주입하여 서비스 레이어를 우회.  
REST API 컨트롤러(`ProductController`)는 이미 완전히 분리되어 있으나 어드민 컨트롤러만 남아 있음.

**필요한 `ProductService` 추가 메서드**:
| 메서드 | 역할 |
|---|---|
| `getProductEntity(Long id): Product` | 어드민 수정 폼 조회용 (엔티티 반환) |

또는 현재 `findAll()`이 이미 `List<Product>` 반환 중이므로 편집 폼에도 동일 패턴 적용.

**작업 범위**: `AdminProductController.java`, `ProductService.java`  
**테스트**: `AdminProductControllerTest` 신규 작성 (단위 테스트, MockMvc 또는 Mockito 기반)  
**완료 조건**: `AdminProductController`에서 `ProductRepository`, `CategoryRepository` 필드 완전 제거

**프롬프트**:
```
AdminProductController가 ProductRepository와 CategoryRepository를 직접 주입해 서비스 레이어를 우회하고 있어.
TDD 사이클로 다음 순서로 리팩토링해줘:

[Red] AdminProductControllerTest 파일을 새로 만들고, 컨트롤러가 ProductService만 의존하는 상태를 검증하는 테스트를 먼저 작성해.
[Green] AdminProductController에서 직접 Repository 의존성을 제거하고, 필요한 기능은 ProductService에 메서드를 추가해서 위임해.
[Refactor] AdminProductController 생성자에서 ProductRepository, CategoryRepository 파라미터를 제거해.

단, 한 번에 모든 기능을 바꾸지 말고, 기능 하나씩(list → newForm → create → editForm → update → delete) TDD 사이클을 반복해줘.
모든 기존 테스트가 통과한 뒤 README에 작업 로그를 추가해.
```

---

### Task 4 — 전역 예외 처리기(`@RestControllerAdvice`) 도입

**문제**: 컨트롤러마다 예외 처리 방식이 달라 API 에러 응답이 일관되지 않음.

| 컨트롤러 | 현재 방식 | 문제 |
|---|---|---|
| `MemberController` | `@ExceptionHandler` → 400 | 개별 선언, 중복 |
| `ProductController` | `@ExceptionHandler` → 400 | 개별 선언, 중복 |
| `OptionController` | `@ExceptionHandler` → 400 | 개별 선언, 중복 |
| `CategoryController` | try-catch per method → 404 | `IllegalArgumentException`을 404로 잘못 매핑 |
| `WishController` | 핸들러 없음 | 서비스 예외 시 500 반환 |
| `OrderController` | 핸들러 없음 | 서비스 예외 시 500 반환 |

**설계**:
- `gift.exception` 패키지에 `GlobalExceptionHandler` 클래스 추가
- `IllegalArgumentException` → 400 Bad Request + 메시지 바디
- `NoSuchElementException` (또는 커스텀 `NotFoundException`) → 404 Not Found
- 기존 컨트롤러의 `@ExceptionHandler`, try-catch 제거

**테스트**: `GlobalExceptionHandlerTest` 신규 작성  
**완료 조건**: 모든 컨트롤러에서 중복 `@ExceptionHandler` 제거, 전역 핸들러로 통일

**프롬프트**:
```
현재 컨트롤러마다 @ExceptionHandler나 try-catch로 예외를 처리하는 방식이 달라서 에러 응답이 일관되지 않아.
gift.exception 패키지에 GlobalExceptionHandler 클래스를 @RestControllerAdvice로 만들어서 전역 처리해줘.

처리 대상:
- IllegalArgumentException → 400 Bad Request (메시지 바디 포함)
- EntityNotFoundException 또는 IllegalArgumentException으로 던지는 "not found" 케이스 → 현재는 일단 400으로 통일 (나중에 커스텀 예외로 분리 가능)

작업 순서:
[Red] GlobalExceptionHandlerTest에서 핸들러가 올바른 상태 코드를 반환하는지 테스트 먼저 작성
[Green] GlobalExceptionHandler 클래스 구현
[Refactor] 각 컨트롤러의 개별 @ExceptionHandler 메서드와 try-catch 제거

기존 테스트 전부 통과 확인 후 README에 작업 로그 추가해.
```

---

### Task 5 — 주문 완료 후 찜 자동 제거 구현

**문제**: `OrderController` 주석에 "6. cleanup wish"가 명시되어 있으나 `OrderService.createOrder()`에 해당 로직이 없음.

**설계**:
- `OrderService`에 `WishRepository` 의존성 추가
- `createOrder()` 내 주문 저장 후 해당 `(memberId, productId)` 에 해당하는 찜이 있으면 제거
- 찜이 없어도 예외 없이 진행 (선택적 삭제)

**테스트**: `OrderServiceTest`에 케이스 추가
- "주문 시 해당 상품의 찜이 있으면 삭제된다"
- "주문 시 해당 상품의 찜이 없으면 그냥 진행된다"

**완료 조건**: `OrderController` 주석의 step 6이 실제로 동작함

**프롬프트**:
```
OrderController의 createOrder() 주석에 "6. cleanup wish"가 있지만 실제 구현이 없어.
주문이 완료되면 해당 회원의 해당 상품 찜이 자동으로 제거되어야 해.

TDD 순서:
[Red] OrderServiceTest에 두 케이스 추가:
  - createOrder_wishExists_removesWish: 찜이 있으면 삭제됨
  - createOrder_noWish_proceedsNormally: 찜이 없어도 정상 진행됨
[Green] OrderService에 WishRepository 의존성 추가 및 createOrder() 내 찜 제거 로직 구현
[Refactor] OrderController 주석 정리 (step 6이 실제로 동작하므로 주석 명확화)

기존 OrderServiceTest 4개 + 신규 2개 총 6개 테스트 통과 후 README에 작업 로그 추가해.
```

---

### Task 6 — `KakaoAuthService` / `AuthenticationResolver` 의존성 정리

**문제**: 두 클래스가 `MemberService`를 우회하고 `MemberRepository`를 직접 사용.  
회원 관련 영속성 처리가 `MemberService` 한 곳에 모여야 한다는 원칙 위반.

**대상**:
| 클래스 | 현재 의존 | 변경 후 |
|---|---|---|
| `KakaoAuthService` | `MemberRepository` | `MemberService` |
| `AuthenticationResolver` | `MemberRepository` | `MemberService` |

**필요한 `MemberService` 신규 메서드**:
- `findOrCreateKakaoMember(String email, String kakaoAccessToken): Member`  
  (기존 `processCallback` 로직 이관)
- `findByEmail(String email): Optional<Member>`  
  (AuthenticationResolver용, 또는 기존 `findById` 패턴 활용)

**테스트**: 기존 `KakaoAuthServiceTest` 3개 통과 유지 + `MemberServiceTest`에 신규 케이스 추가  
**완료 조건**: `KakaoAuthService`, `AuthenticationResolver`에서 `MemberRepository` 필드 제거

**프롬프트**:
```
KakaoAuthService와 AuthenticationResolver가 MemberRepository를 직접 주입받아 사용하고 있어.
모든 회원 관련 영속성 처리는 MemberService를 통해야 해.

작업 순서:
[Task 6-1] MemberService에 카카오 로그인용 메서드 추가
  [Red] MemberServiceTest에 findOrCreateKakaoMember() 케이스 추가
  [Green] MemberService.findOrCreateKakaoMember(String email, String kakaoAccessToken) 구현
  [Refactor] KakaoAuthService가 memberRepository 대신 memberService.findOrCreateKakaoMember() 호출하도록 수정, MemberRepository 필드 제거

[Task 6-2] AuthenticationResolver 의존성 정리
  [Red] AuthenticationResolver가 MemberService.findByEmail()을 통해 회원을 조회하는 테스트 추가
  [Green] MemberService에 findByEmail(String email): Optional<Member> 추가, AuthenticationResolver 수정
  [Refactor] AuthenticationResolver에서 MemberRepository 필드 제거

기존 KakaoAuthServiceTest, MemberServiceTest 모두 통과 후 README에 작업 로그 추가해.
```

---

### Task 7 — `wish` 테이블 복합 유니크 제약 추가

**문제**: `wish` 테이블에 `(member_id, product_id)` 복합 유니크 제약이 없어  
동시 요청 시 서비스 레벨 중복 검사를 통과하여 중복 찜 레코드가 생성될 수 있음.

**작업 범위**:
- `V3__Add_wish_unique_constraint.sql` 신규 Flyway 마이그레이션 파일 추가
- `Wish` 엔티티에 `@Table(uniqueConstraints = ...)` 추가 (JPA 레벨 선언)

**테스트**: 마이그레이션 적용 후 `WishServiceTest` 기존 7개 통과 확인  
**완료 조건**: H2 테스트 DB에서 마이그레이션 정상 적용

**프롬프트**:
```
wish 테이블에 (member_id, product_id) 조합에 대한 유니크 제약이 없어서
동시 요청 시 중복 찜 레코드가 생길 수 있어.  

작업:
1. src/main/resources/db/migration/V3__Add_wish_unique_constraint.sql 파일을 새로 만들어
   ALTER TABLE wish ADD CONSTRAINT uq_wish_member_product UNIQUE (member_id, product_id);

2. Wish 엔티티 클래스에 @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"})) 추가

3. 기존 WishServiceTest 7개 테스트가 모두 통과하는지 확인

README에 작업 로그 추가해.
```

---

### Task 8 — 어드민 컨트롤러 테스트 작성

**문제**: `AdminMemberController`와 `AdminProductController`에 테스트가 전혀 없음.  
Task 3에서 `AdminProductController` 리팩토링이 완료된 후 진행.

**테스트 대상**: 
- `AdminMemberController` — list, newForm, create, editForm, update, chargePoint, delete
- `AdminProductController` — list, newForm, create, editForm, update, delete

**테스트 방식**: `@WebMvcTest` + Mockito로 컨트롤러 단위 테스트  
**완료 조건**: 각 핸들러 메서드마다 정상 케이스 + 에러 케이스 1개 이상

**프롬프트**:
```
AdminMemberController와 AdminProductController에 테스트가 없어.
@WebMvcTest를 사용해 각 컨트롤러의 단위 테스트를 작성해줘.

AdminMemberControllerTest 대상:
- GET /admin/members → 회원 목록 뷰 반환
- GET /admin/members/new → 새 회원 폼 반환
- POST /admin/members (정상) → /admin/members 리다이렉트
- POST /admin/members (중복 이메일) → 에러와 함께 new 폼 반환
- GET /admin/members/{id}/edit → 수정 폼 반환
- POST /admin/members/{id}/edit → 리다이렉트
- POST /admin/members/{id}/charge-point → 리다이렉트
- POST /admin/members/{id}/delete → 리다이렉트

AdminProductControllerTest 대상 (Task 3 완료 후):
- GET /admin/products → 상품 목록 뷰
- POST /admin/products (정상) → 리다이렉트
- POST /admin/products (유효하지 않은 상품명) → 에러와 함께 new 폼 반환
- POST /admin/products/{id}/delete → 리다이렉트

기능 하나씩 TDD(Red → Green → Refactor) 사이클로 작성하고, README에 작업 로그 추가해.
```

---

## 작업 의존 관계

```
Task 1 (포맷 버그)
    │
Task 2 (@Transactional)
    │
Task 3 (AdminProductController 분리)
    │
    ├── Task 4 (전역 예외 처리기)
    │
    ├── Task 5 (주문 후 찜 제거)
    │
    ├── Task 6 (KakaoAuth / AuthResolver 의존성)
    │
    ├── Task 7 (wish 유니크 제약)
    │
    └── Task 8 (어드민 컨트롤러 테스트) ← Task 3 완료 필요
```

---

## 완료 체크리스트 — 코드 품질 / 아키텍처

- [ ] Task 1: `CategoryService.update()` 포맷 버그 수정
- [ ] Task 2: 서비스 메서드 `@Transactional` 추가
- [ ] Task 3: `AdminProductController` 서비스 레이어 분리
- [ ] Task 4: `@RestControllerAdvice` 전역 예외 처리기
- [ ] Task 5: 주문 후 찜 자동 제거
- [ ] Task 6: `KakaoAuthService` / `AuthenticationResolver` 의존성 정리
- [ ] Task 7: `wish` 테이블 유니크 제약 추가
- [ ] Task 8: 어드민 컨트롤러 테스트 작성

---

---

### Task 9 — `Product` 도메인 검증 내재화

**문제**: `ProductNameValidator`가 길이·문자 불변식과 "카카오" 권한 정책을 함께 처리. `ProductService`를 거치지 않으면 검증 없이 `Product`를 생성할 수 있어 불변식이 보장되지 않음.

**설계**:
- `Product` 생성자 / `update()`: null/blank, 길이(≤15자), 허용 문자 검증 → 위반 시 `IllegalArgumentException`
- `ProductNameValidator`: "카카오" 검사 메서드만 유지 (길이·문자 메서드 제거)
- `ProductService`: `validate()` 전체 호출 제거, "카카오" 체크만 유지 (allowKakao 분기 그대로)

**작업 범위**: `Product.java`, `ProductNameValidator.java`, `ProductService.java`, `ProductTest.java`(신규)  
**완료 조건**: `Product` 생성자에서 잘못된 이름으로 예외 발생, 기존 테스트 전부 GREEN

**프롬프트**:
```
ProductNameValidator의 길이·문자 불변식 검증을 Product 생성자와 update()로 이동해줘.

TDD 순서:
[Red] src/test/java/gift/product/ProductTest.java 신규 작성:
  - null 이름 → 예외
  - blank 이름 → 예외
  - 16자 이름 → 예외
  - 허용되지 않는 특수문자 포함 → 예외
  - 정상 이름(15자 이하, 허용 문자) → 생성 성공
[Green] Product 생성자와 update()에 검증 로직 추가
[Refactor] ProductNameValidator에서 길이·문자 검증 메서드 제거,
           ProductService에서 validate() 전체 호출 → 카카오 체크만 호출로 변경
           기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

### Task 12 — 카카오 알림 실패 응답 포함 (`OrderResponse.notificationSent`)

**문제**: `sendKakaoMessageIfPossible()`이 예외를 무시(catch ignored)하여 고객은 알림 미수신 사실을 모름.  
주문 완료 응답에 알림 성공 여부를 포함해야 함.

**설계 (Option B)**:
- `OrderResponse`에 `notificationSent: boolean` 필드 추가
- `sendKakaoMessageIfPossible()` → `boolean` 반환 (성공 true, 미전송·실패 false)
- 알림 실패 시 `log.warn(...)` 기록
- 카카오 액세스 토큰 없으면 → `false` (전송 시도 안 함)
- 주문 자체는 항상 성공 처리 유지

**작업 범위**: `OrderResponse.java`, `OrderService.java`, `OrderServiceTest.java`  
**완료 조건**: `createOrder()` 응답에 `notificationSent` 포함, 기존 테스트 GREEN

---

### Task 11 — `MemberService`에서 JWT 의존 제거 (`AuthService` 분리)

**문제**: `MemberService`(도메인 서비스)가 `JwtProvider`(인프라)를 직접 주입받아 `TokenResponse`를 반환.  
도메인 레이어가 인프라 레이어에 의존하는 레이어 위반이며, 토큰 발급 책임이 도메인 서비스에 혼재.

**설계**:
- `gift.auth.AuthService` (신규) — 토큰 발급 전담
  - `register(email, password): TokenResponse` — MemberService.registerMember() 후 JWT 발급
  - `login(email, password): TokenResponse` — MemberService.authenticate() 후 JWT 발급
- `MemberService` 수정
  - `registerMember(email, password): Member` — 회원 저장만, TokenResponse 반환 안 함
  - `authenticate(email, password): Member` — 자격 증명 검증 후 Member 반환, JWT 발급 안 함
  - `JwtProvider` 필드 제거
- `MemberController` — `AuthService` 에만 위임 (register/login)

**작업 범위**: `AuthService.java`(신규), `MemberService.java`, `MemberController.java`  
**테스트**: `AuthServiceTest` 신규 작성, `MemberServiceTest` 수정  
**완료 조건**: `MemberService`에 `JwtProvider` 필드 없음, 기존 테스트 전부 GREEN

---

### Task 10 — 커스텀 예외 계층 도입

**문제**: 현재 모든 예외(not found, duplicate, unauthorized)가 `IllegalArgumentException`으로 통일되어 HTTP 400만 반환. 클라이언트가 에러 종류를 구분할 수 없음.

**설계**:
- `gift.exception.BusinessException` (추상 베이스)
  - `NotFoundException` → HTTP 404
  - `DuplicateException` → HTTP 409
- `GlobalExceptionHandler`에 각 예외 타입별 핸들러 추가
- 서비스 레이어에서 상황에 맞는 예외 사용으로 교체

**작업 범위**: `gift/exception/` 패키지 + 서비스 클래스들 + `GlobalExceptionHandlerTest`  
**테스트**: `GlobalExceptionHandlerTest`에 NotFoundException→404, DuplicateException→409 케이스 추가  
**완료 조건**: `GlobalExceptionHandler`가 세 타입 모두 올바른 상태 코드 반환

**프롬프트**:
```
모든 서비스 예외가 IllegalArgumentException으로 통일되어 HTTP 400만 반환 중이야.
커스텀 예외 계층을 도입해 404/409를 구분해줘.

TDD 순서:
[Red] GlobalExceptionHandlerTest에 두 케이스 추가:
  - NotFoundException 발생 → 404 반환
  - DuplicateException 발생 → 409 반환
[Green] BusinessException, NotFoundException, DuplicateException 클래스 생성
        GlobalExceptionHandler에 각 타입별 @ExceptionHandler 추가
[Refactor] 서비스 레이어에서 "not found" 케이스 → NotFoundException,
           "duplicate" 케이스 → DuplicateException으로 교체
기존 테스트 전부 통과 확인 후 README 로그 추가해.
```

---

# 기능 개선 작업 (Functional Improvements)

> 코드 품질 작업(Task 1~8)과 별도로 관리한다.  
> 우선순위는 **버그성 기능 결함 → 입력 검증 강화 → API 응답 개선 → 신규 기능** 순이다.

---

## 기능 문제 목록

| # | 분류 | 위치 | 문제 |
|---|---|---|---|
| F-1 | 보안 결함 | `MemberService`, `V2 seed` | 비밀번호 평문 저장 및 평문 비교 |
| F-2 | 런타임 오류 | `CategoryService.delete()` | 상품이 있는 카테고리 삭제 시 DB FK 에러 (서비스 레벨 검사 없음) |
| F-3 | 런타임 오류 | `ProductService.deleteProduct()`, `OptionService.deleteOption()` | 주문이 있는 상품/옵션 삭제 시 DB FK 에러 (서비스 레벨 검사 없음) |
| F-4 | 로직 버그 | `Option.subtractQuantity()` | 음수/0 amount 방어 없음 — 0으로 호출 시 재고 불변, 음수 호출 시 재고 증가 |
| F-5 | 입력 검증 미비 | `OrderRequest.message` | 길이 제한 없음 — DB `varchar(255)` 초과 시 런타임 오류 |
| F-7 | 안정성 결함 | `KakaoMessageClient.buildTemplate()` | 주문 메시지에 `"` `\` 등 특수 문자 포함 시 JSON 파싱 오류 발생 |
| F-8 | API 불완전 | `OrderResponse` | `optionId`만 반환 — 상품명·옵션명·총금액 없어 클라이언트가 재조회 필요 |
| F-9 | API 불완전 | `ProductResponse` | `categoryId`만 반환 — 카테고리명 없어 클라이언트가 재조회 필요 |
| F-10 | 기능 부재 | 없음 | 회원 본인 정보 조회 API 없음 (`GET /api/members/me` — 포인트 잔액 확인 불가) |
| F-11 | 기능 부재 | 없음 | 옵션 수정 API 없음 (`PUT /api/products/{id}/options/{optionId}` — 재고/이름 수정 불가) |
| F-12 | DB 무결성 | `wish` 테이블 FK | 상품 삭제 시 `wish.product_id` FK 에러 — `ON DELETE CASCADE` 없음 |

---

## 기능 개선 Task

---

### Task F-1 — 비밀번호 BCrypt 해싱

**문제**:
- `Member` 엔티티에 비밀번호가 평문 저장됨
- `MemberService.login()`에서 `member.getPassword().equals(password)` 평문 비교
- `V2__Insert_default_data.sql`에도 평문 패스워드 ('admin1234', 'password1' 등)

**영향 범위**: `MemberService`, `Member`, `build.gradle` (BCrypt 의존성), `V2` seed

**설계**:
- `build.gradle`에 `spring-security-crypto` 의존성 추가 (Spring Security 전체 아님, crypto 모듈만)
- `MemberService.register()`, `adminCreate()` — 저장 전 `BCryptPasswordEncoder.encode(password)`
- `MemberService.login()` — `BCryptPasswordEncoder.matches(rawPassword, encodedPassword)` 비교
- `V2` seed는 해시값으로 교체 불필요 (개발 편의용 — 단, 주석으로 평문 노출임을 명시)

**테스트**: `MemberServiceTest`에 BCrypt 검증 케이스 추가  
**완료 조건**: 평문 비교 코드 제거, 기존 테스트 통과

**프롬프트**:
```
MemberService에서 비밀번호를 평문으로 저장하고 비교하고 있어. BCrypt 해싱을 적용해줘.

작업:
1. build.gradle에 'org.springframework.security:spring-security-crypto' 의존성 추가
2. MemberService에 BCryptPasswordEncoder 빈 주입 (생성자 주입)
3. register()와 adminCreate()에서 저장 전 passwordEncoder.encode(password) 적용
4. login()에서 passwordEncoder.matches(rawPassword, storedHash) 비교로 교체

TDD 순서:
[Red] MemberServiceTest에서 기존 login 테스트가 BCrypt 비교를 검증하도록 수정
[Green] 위 변경 구현
[Refactor] 기존 테스트 전부 통과 확인

V2 seed 데이터의 평문 패스워드는 개발 편의상 그대로 두되,
SQL 파일에 "개발용 평문 패스워드 — 운영 환경에서는 반드시 교체" 주석 추가.
완료 후 README에 작업 로그 추가해.
```

---

### Task F-2 — 카테고리/상품/옵션 삭제 시 연관 데이터 검사

**문제**:
- `CategoryService.delete()` — 카테고리에 상품이 있으면 `DataIntegrityViolationException` (DB FK 에러)
- `ProductService.deleteProduct()` — 해당 상품의 옵션에 주문이 있으면 `DataIntegrityViolationException`
- `OptionService.deleteOption()` — 해당 옵션에 주문이 있으면 `DataIntegrityViolationException`

**설계**:
| 삭제 대상 | 검사 조건 | 예외 메시지 |
|---|---|---|
| 카테고리 | 해당 카테고리의 상품이 1개 이상 존재 | "상품이 있는 카테고리는 삭제할 수 없습니다." |
| 상품 | 해당 상품의 옵션에 주문이 1개 이상 존재 | "주문이 있는 상품은 삭제할 수 없습니다." |
| 옵션 | 해당 옵션에 주문이 1개 이상 존재 | "주문이 있는 옵션은 삭제할 수 없습니다." |

**필요한 Repository 메서드 추가**:
- `ProductRepository.existsByCategoryId(Long categoryId): boolean`
- `OrderRepository.existsByOptionId(Long optionId): boolean`
- `OrderRepository.existsByOption_ProductId(Long productId): boolean`

**테스트**: 각 서비스 테스트에 "연관 데이터 있을 때 삭제 시 예외" 케이스 추가  
**완료 조건**: DB FK 에러 대신 `IllegalArgumentException`으로 사전 차단

**프롬프트**:
```
카테고리/상품/옵션 삭제 시 연관 데이터가 있으면 DB FK 에러가 발생해.
서비스 레벨에서 사전 검사 후 명확한 예외를 던지도록 수정해줘.

[Task F-2-1] CategoryService.delete()
  [Red] CategoryServiceTest에 "상품이 있는 카테고리 삭제 시 예외" 테스트 추가
  [Green] ProductRepository에 existsByCategoryId() 추가, CategoryService.delete()에 검사 로직 추가
  [Refactor] 기존 테스트 포함 전부 통과 확인

[Task F-2-2] OptionService.deleteOption()
  [Red] OptionServiceTest에 "주문이 있는 옵션 삭제 시 예외" 테스트 추가
  [Green] OrderRepository에 existsByOptionId() 추가, OptionService.deleteOption()에 검사 로직 추가

[Task F-2-3] ProductService.deleteProduct()
  [Red] ProductServiceTest에 "주문이 있는 상품 삭제 시 예외" 테스트 추가
  [Green] OrderRepository에 existsByOption_ProductId() 추가, ProductService.deleteProduct()에 검사 로직 추가

각 단계마다 기존 테스트 전부 통과 후 README에 작업 로그 추가해.
```

---

### Task F-3 — `Option.subtractQuantity()` 음수/0 방어

**문제**: 현재 코드가 `amount > quantity`만 검사하고 `amount <= 0` 검사가 없음.

```java
// 현재
public void subtractQuantity(int amount) {
    if (amount > this.quantity) { ... }
    this.quantity -= amount;
}
// amount = 0 → 재고 불변 (정상처럼 보이지만 무의미한 주문)
// amount = -1 → 재고 1 증가 (버그)
```

**테스트**: `OptionServiceTest` 또는 `Option` 단위 테스트에 케이스 추가  
**완료 조건**: `amount <= 0`이면 `IllegalArgumentException`

**프롬프트**:
```
Option.subtractQuantity()에서 amount가 0 이하일 때 방어 로직이 없어.

TDD:
[Red] amount = 0, amount = -1일 때 IllegalArgumentException이 발생하는 테스트 추가
[Green] if (amount <= 0) throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다."); 추가
[Refactor] 기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

### Task F-4 — 입력 검증 강화 (메시지 길이) ✅ 완료

**문제**: `OrderRequest.message`: DB `varchar(255)` 제한인데 DTO에 `@Size` 없음

**설계**: `@Size(max = 255) String message`

**완료**: `OrderRequest.message`에 `@Size(max = 255)` 추가, `OrderRequestTest` 3개 케이스 통과.

---

### Task F-5 — `KakaoMessageClient` JSON 이스케이프 처리

**문제**: 메시지 본문을 문자열 템플릿으로 직접 JSON을 조합하는데,  
주문 메시지에 `"` `\` `\n` 등 특수 문자가 들어오면 JSON 파싱 오류가 발생함.

```java
// 현재 — 특수 문자 무방비
"text": "...%s".formatted(..., order.getMessage(), ...)
```

**설계**: `ObjectMapper`(Jackson)로 JSON 직렬화  
**테스트**: 특수 문자 포함 메시지로 `buildTemplate()` 호출 시 올바른 JSON이 생성되는지 검증  
**완료 조건**: `"`, `\`, 줄바꿈 포함 메시지도 정상 처리

**프롬프트**:
```
KakaoMessageClient.buildTemplate()이 문자열 템플릿으로 JSON을 직접 조합하고 있어.
주문 메시지에 큰따옴표나 역슬래시가 있으면 JSON 파싱 오류가 발생할 수 있어.

ObjectMapper를 사용해 JSON을 안전하게 직렬화하도록 수정해줘.

TDD:
[Red] 메시지가 "안녕 \"홍길동\"" 같은 특수 문자를 포함할 때 buildTemplate()이 유효한 JSON을 반환하는 테스트 추가
[Green] KakaoMessageClient에 ObjectMapper를 주입해 text 필드를 Jackson으로 직렬화
[Refactor] 기존 동작 유지 확인 후 README 로그 추가
```

---

### Task F-6 — `OrderResponse` 개선 (상품명·옵션명·총금액 포함)

**문제**: 현재 `OrderResponse`는 `optionId`만 반환.  
주문 내역 목록에서 "어떤 상품을 얼마에 샀는지" 알려면 클라이언트가 추가 API 요청이 필요.

**현재**:
```json
{ "id": 1, "optionId": 3, "quantity": 2, "orderDateTime": "...", "message": "..." }
```

**개선 후**:
```json
{
  "id": 1,
  "optionId": 3,
  "optionName": "블루 / 256GB",
  "productId": 2,
  "productName": "아이폰 16",
  "quantity": 2,
  "unitPrice": 1350000,
  "totalPrice": 2700000,
  "orderDateTime": "...",
  "message": "..."
}
```

**테스트**: `OrderServiceTest.getOrders_*` 케이스에서 새 필드 검증  
**완료 조건**: `OrderResponse`가 상품명, 옵션명, 단가, 총액을 포함

**프롬프트**:
```
OrderResponse에 상품명, 옵션명, 단가, 총액이 없어서 클라이언트가 주문 내역을 표시하려면 추가 조회가 필요해.

OrderResponse에 다음 필드를 추가해줘:
- productId: Long
- productName: String
- optionName: String
- unitPrice: int
- totalPrice: int (unitPrice × quantity)

TDD:
[Red] OrderServiceTest의 getOrders 케이스에서 새 필드 검증 추가
[Green] OrderResponse.from(Order)에서 order.getOption().getProduct()를 통해 필드 채움
[Refactor] 기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

### Task F-7 — `ProductResponse`에 카테고리명 포함

**문제**: `ProductResponse`가 `categoryId`만 반환.  
상품 목록에서 카테고리명을 표시하려면 클라이언트가 카테고리 API를 별도 호출해야 함.

**현재**: `{ "id": 1, "name": "...", "price": 1000000, "imageUrl": "...", "categoryId": 1 }`  
**개선 후**: `categoryId` 유지 + `"categoryName": "전자기기"` 추가

**테스트**: `ProductServiceTest`의 상품 조회 케이스에서 `categoryName` 검증  
**완료 조건**: `ProductResponse`가 `categoryName`을 포함

**프롬프트**:
```
ProductResponse에 categoryId만 있고 categoryName이 없어.
상품 목록 응답에 categoryName 필드를 추가해줘.

TDD:
[Red] ProductServiceTest에서 getProduct(), getProducts() 결과의 categoryName을 검증하는 케이스 추가
[Green] ProductResponse에 categoryName: String 필드 추가, from() 메서드에서 product.getCategory().getName() 채움
[Refactor] 기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

### Task F-8 — 회원 본인 정보 조회 API (`GET /api/members/me`)

**문제**: 로그인한 회원이 자신의 이메일, 잔여 포인트를 확인하는 API가 없음.  
주문 전 포인트 잔액 확인이나 마이페이지 구현이 불가능한 상태.

**설계**:
```
GET /api/members/me
Authorization: Bearer <JWT>
→ 200 { "email": "...", "point": 5000 }
```

**구현 위치**: `MemberController`, `MemberService.getMyInfo(String email)` 추가  
**테스트**: `MemberServiceTest`에 `getMyInfo` 케이스 추가  
**완료 조건**: 인증된 회원만 자신의 정보를 조회 가능, 미인증 시 401

**프롬프트**:
```
로그인한 회원이 자신의 이메일과 포인트 잔액을 조회하는 API가 없어.
GET /api/members/me 엔드포인트를 추가해줘.

응답 형식:
{ "email": "test@example.com", "point": 5000 }

TDD:
[Red] MemberServiceTest에 getMyInfo() 케이스 추가 (이메일로 회원 조회 후 이메일·포인트 반환)
[Green] MemberService.getMyInfo(String email): MemberResponse 구현
        MemberController에 GET /api/members/me 핸들러 추가 (AuthenticationResolver로 회원 추출)
[Refactor] 미인증(null 반환) 시 401 응답 확인, 기존 테스트 전부 통과 후 README 로그 추가
```

---

### Task F-9 — 옵션 수정 API (`PUT /api/products/{productId}/options/{optionId}`)

**문제**: 옵션은 생성/삭제만 가능하고 수정 API가 없음.  
관리자가 재고를 보정하거나 옵션명을 변경하는 것이 불가능.

**설계**:
```
PUT /api/products/{productId}/options/{optionId}
Body: { "name": "새 옵션명", "quantity": 50 }
→ 200 { "id": 1, "name": "새 옵션명", "quantity": 50 }
```

**검증 규칙**:
- 이름: 기존 `OptionNameValidator` 동일 적용
- 다른 옵션과 이름 중복 불가 (단, 자기 자신과 같은 이름은 허용)
- 수량: `@Min(0)` (재고 0으로 수정은 허용, 음수는 불가)

**테스트**: `OptionServiceTest`에 updateOption 케이스 추가  
**완료 조건**: `OptionService.updateOption()` + `OptionController PUT` 핸들러 추가

**프롬프트**:
```
옵션 수정 API가 없어. PUT /api/products/{productId}/options/{optionId} 를 추가해줘.

TDD:
[Red] OptionServiceTest에 updateOption() 케이스 추가:
  - 유효한 요청으로 수정 성공
  - 유효하지 않은 이름으로 수정 시 예외
  - 다른 옵션과 이름 중복 시 예외
  - 자기 자신과 같은 이름은 허용
  - 존재하지 않는 옵션 수정 시 예외
[Green] Option 엔티티에 update(name, quantity) 메서드 추가
        OptionService.updateOption(productId, optionId, request) 구현
        OptionController에 PUT 핸들러 추가
[Refactor] 기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

### Task F-10 — `wish` 테이블 `ON DELETE CASCADE` 추가 (상품 삭제 연동)

**문제**: 상품 삭제 시 `wish.product_id` FK 제약으로 에러 발생.  
현재 `wish` 테이블에 `ON DELETE CASCADE`가 없음.

**설계**: Flyway 마이그레이션으로 FK 재생성  
(또는 `ProductService.deleteProduct()` 에서 먼저 찜 삭제 후 상품 삭제)

**선택**: 서비스 레벨에서 먼저 찜 삭제 → 명시적이고 테스트 가능  
(DB CASCADE는 숨겨진 동작이라 서비스 레이어 의도가 불명확해짐)

**테스트**: `ProductServiceTest`에 "상품 삭제 시 연관 찜도 삭제" 케이스 추가  
**완료 조건**: 상품 삭제 시 연관된 찜 레코드가 먼저 제거됨

**프롬프트**:
```
상품 삭제 시 wish.product_id FK 제약 때문에 연관된 찜이 있으면 에러가 발생해.

서비스 레벨에서 명시적으로 찜을 먼저 삭제한 뒤 상품을 삭제하도록 수정해줘.
(DB CASCADE보다 서비스에서 명시적으로 처리하는 게 의도가 명확함)

TDD:
[Red] ProductServiceTest에 deleteProduct_withWishes_removesWishesFirst 테스트 추가
[Green] ProductService에 WishRepository 의존성 추가
        deleteProduct()에서 wishRepository.deleteByProductId(id) 후 productRepository.deleteById(id) 호출
[Refactor] WishRepository에 deleteByProductId(Long productId) 메서드 추가
           기존 테스트 전부 통과 확인 후 README 로그 추가
```

---

## 기능 개선 작업 의존 관계

```
F-1 (BCrypt)          ← 독립, 가장 먼저 권장
F-3 (subtractQuantity 방어) ← 독립
F-4 (입력 검증 강화)  ← 독립
F-5 (Kakao JSON)      ← 독립

F-2 (삭제 연관 검사)  ← F-3 완료 후 권장 (OrderRepository 메서드 공유)
F-10 (wish cascade)   ← F-2와 함께 진행 가능

F-6 (OrderResponse)   ← 독립
F-7 (ProductResponse) ← 독립
F-8 (GET /me)         ← F-1 완료 후 권장
F-9 (옵션 수정 API)   ← 독립
```

---

## 전체 완료 체크리스트

### 코드 품질 / 아키텍처
- [x] Task 1: `CategoryService.update()` 포맷 버그 수정
- [x] Task 2: 서비스 메서드 `@Transactional` 추가
- [x] Task 3: `AdminProductController` 서비스 레이어 분리
- [x] Task 4: `@RestControllerAdvice` 전역 예외 처리기
- [x] Task 5: 주문 후 찜 자동 제거
- [x] Task 6: `KakaoAuthService` / `AuthenticationResolver` 의존성 정리
- [x] Task 7: `wish` 테이블 유니크 제약 추가
- [x] Task 8: 어드민 컨트롤러 테스트 작성
- [x] Task 9: `Product` 도메인 검증 내재화
- [x] Task 10: 커스텀 예외 계층 도입 (`NotFoundException` → 404, `DuplicateException` → 409)
- [x] Task 11: `MemberService` JWT 의존 제거 (`AuthService` 분리)
- [x] Task 12: 카카오 알림 실패 응답 포함 (`OrderResponse.notificationSent`)
- [x] Task 13: `updateMember` 비밀번호 BCrypt 인코딩 누락 수정 (보안 버그)
- [x] Task 14: 쓰기 메서드 `@Transactional` 누락 보완 (`ProductService`, `OptionService`, `MemberService`)
- [x] Task 15: `WishController` `@WebMvcTest` 테스트 작성

### 기능 개선
- [x] Task F-1: 비밀번호 BCrypt 해싱
- [x] Task F-2: 카테고리/상품/옵션 삭제 시 연관 데이터 검사
- [x] Task F-3: `Option.subtractQuantity()` 음수/0 방어
- [x] Task F-4: 입력 검증 강화 (메시지 길이)
- [x] Task F-5: `KakaoMessageClient` JSON 이스케이프 처리
- [x] Task F-6: `OrderResponse` 개선 (상품명·옵션명·총금액)
- [x] Task F-7: `ProductResponse`에 카테고리명 포함
- [x] Task F-8: 회원 본인 정보 조회 API (`GET /api/members/me`)
- [x] Task F-9: 옵션 수정 API (`PUT` 엔드포인트)
- [x] Task F-10: 상품 삭제 시 연관 찜 제거
