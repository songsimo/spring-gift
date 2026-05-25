# 선물하기 (Spring Gift)

카카오 로그인 기반의 선물 주문 플랫폼입니다. 포인트 결제, 찜 목록, 카카오톡 주문 알림을 지원합니다.

> A gift-ordering platform with Kakao OAuth2 login, point-based payment, wishlist, and KakaoTalk order notifications.

---

## 기술 스택 (Tech Stack)

| 분류 | 기술 |
|---|---|
| Language | Kotlin 1.9.25 / Java 21 |
| Framework | Spring Boot 3.5.9 |
| ORM | Spring Data JPA + Hibernate |
| DB Migration | Flyway 12 |
| Database | MySQL (prod) / H2 (test) |
| Auth | JWT (JJWT 0.13) + Kakao OAuth2 |
| Build | Gradle (Kotlin DSL) |
| Lint | ktlint 14 |

---

## 시작하기 (Getting Started)

### 사전 조건 (Prerequisites)

- Java 21
- MySQL 8.x
- [Kakao Developers](https://developers.kakao.com) 앱 등록 (카카오 로그인 사용 시)

### 환경변수 설정 (Environment Variables)

| 변수명 | 설명 | 기본값 |
|---|---|---|
| `JWT_SECRET` | JWT 서명 키 (256비트 이상) | `a-string-secret-at-least-256-bits-long` |
| `JWT_EXPIRATION` | JWT 만료 시간 (ms) | `3600000` (1시간) |
| `KAKAO_CLIENT_ID` | 카카오 앱 REST API 키 | — |
| `KAKAO_CLIENT_SECRET` | 카카오 앱 시크릿 키 | — |
| `KAKAO_REDIRECT_URI` | 카카오 OAuth 콜백 URI | `http://localhost:8080/api/auth/kakao/callback` |

> 카카오 로그인 없이 일반 이메일/패스워드 로그인만 사용한다면 `KAKAO_*` 변수는 생략 가능합니다.

### 실행 (Run)

```bash
./gradlew bootRun
```

Flyway가 기동 시 DB 스키마를 자동 생성합니다. 초기 데이터(카테고리, 상품, 회원)도 함께 삽입됩니다.

### 빌드 / 테스트 / 린트 (Build / Test / Lint)

```bash
./gradlew build           # 전체 빌드
./gradlew test            # 전체 테스트
./gradlew test --tests "ClassName.methodName"  # 단일 테스트
./gradlew ktlintCheck     # 코드 스타일 검사
./gradlew ktlintFormat    # 코드 스타일 자동 수정
```

---

## API 엔드포인트 (API Endpoints)

인증이 필요한 엔드포인트는 `Authorization: Bearer <JWT>` 헤더를 포함해야 합니다.

### 인증 (Auth)

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/api/members/register` | 이메일/패스워드 회원가입 → JWT 반환 |
| `POST` | `/api/members/login` | 이메일/패스워드 로그인 → JWT 반환 |
| `GET` | `/api/auth/kakao/login` | 카카오 OAuth2 인증 페이지로 리다이렉트 |
| `GET` | `/api/auth/kakao/callback` | 카카오 OAuth2 콜백 → JWT 반환 |

### 카테고리 (Categories)

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/api/categories` | 전체 카테고리 조회 |
| `POST` | `/api/categories` | 카테고리 생성 |
| `PUT` | `/api/categories/{id}` | 카테고리 수정 |
| `DELETE` | `/api/categories/{id}` | 카테고리 삭제 |

### 상품 (Products)

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/api/products` | 상품 목록 조회 (페이지네이션) |
| `GET` | `/api/products/{id}` | 상품 단건 조회 |
| `POST` | `/api/products` | 상품 생성 |
| `PUT` | `/api/products/{id}` | 상품 수정 |
| `DELETE` | `/api/products/{id}` | 상품 삭제 |

### 상품 옵션 (Options)

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/api/products/{productId}/options` | 옵션 목록 조회 |
| `POST` | `/api/products/{productId}/options` | 옵션 추가 |
| `DELETE` | `/api/products/{productId}/options/{optionId}` | 옵션 삭제 (마지막 옵션 삭제 불가) |

### 찜 목록 (Wishes) `🔒 인증 필요`

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/api/wishes` | 내 찜 목록 조회 (페이지네이션) |
| `POST` | `/api/wishes` | 찜 추가 (중복 추가 시 기존 항목 반환) |
| `DELETE` | `/api/wishes/{id}` | 찜 삭제 (본인 소유 항목만 가능) |

### 주문 (Orders) `🔒 인증 필요`

| Method | Path | 설명 |
|---|---|---|
| `GET` | `/api/orders` | 내 주문 내역 조회 (페이지네이션) |
| `POST` | `/api/orders` | 주문 생성 |

---

## 주문 처리 흐름 (Order Flow)

`POST /api/orders` 호출 시 아래 순서로 처리됩니다.

```
1. JWT 검증 → 회원 조회
2. 옵션(재고) 존재 확인
3. 옵션 재고 차감
4. 회원 포인트 차감 (상품 가격 × 수량)
5. 주문 레코드 저장
6. 카카오톡 주문 알림 전송 (실패해도 주문은 정상 처리됨)
```

---

## 개발 협업 기록 (AI-Assisted Development Log)

> 문제를 정의하고 AI(Claude Code)와 상호작용하며 해결한 과정을 기록합니다.
> 항목은 시간순으로 아래에 추가됩니다 (최신이 맨 아래).

---

### [2026-05-11] README.md 작성

**1. 문제 정의**
프로젝트에 README가 없어 신규 기여자가 구조를 파악하고 실행하기까지의 온보딩 비용이 높다.

**2. 상호작용 타임라인**
- **Step 1**: 코드베이스 탐색 및 README 초안 작성 요청 → AI가 컨트롤러·엔티티·설정 파일·마이그레이션 SQL 분석 후 섹션 구조(기술 스택, 시작하기, API, 주문 흐름, DB 스키마) 제안 → 구조 수용, 작성 진행
- **Step 2**: 작성 언어 선택 요청 → AI가 한국어 / 영어 / 병행 3가지 제시 → **한국어 + 영어 병행** 채택 (학습 목적 + 영문 가독성 동시 확보)
- **Step 3**: AI 초안 검토 → Admin UI 관련 항목 삭제 (REST API 중심 문서에 불필요 판단)

**3. 결과 및 근거**
API 엔드포인트 전체 목록, 주문 처리 6단계 흐름, DB 스키마 다이어그램, 환경변수 기본값 포함 README.md 생성. 별도 테스트 없음 — 문서 검토로 대체.

---

---

### [2026-05-11 ~ 05-13] 카테고리 CRUD 기능 서비스 레이어 분리

**1. 문제 정의**
`CategoryController`가 `CategoryRepository`를 직접 호출하는 4개 기능(전체 조회, 생성, 수정, 삭제)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다.

- **Step 1 [전체 조회]**: `getAll()` 분리 요청 → AI가 `CategoryService` 클래스를 신규 생성하고 스트림 변환 로직을 이동, 컨트롤러는 `categoryService.getAll()` 위임으로 교체. `CategoryServiceTest` 파일과 H2 테스트용 `application.properties` 함께 생성. 테스트 `getAll_returnsAllCategories` 추가 → 수용.
- **Step 2 [생성]**: `create()` 분리 요청 → AI가 `categoryRepository.save()` 로직을 서비스로 이동, 컨트롤러의 Location 헤더 생성을 `CategoryResponse.id()` 기반으로 정리. 테스트 `create_returnsSavedCategory` 추가 → 수용.
- **Step 3 [수정]**: `update()` 분리 요청 → AI가 컨트롤러의 null 체크 방식을 서비스의 `orElseThrow(IllegalArgumentException)` 패턴으로 교체하고, 컨트롤러는 try-catch로 404 반환. 테스트 2개(`update_existingCategory_returnsUpdated`, `update_nonExistingCategory_throwsException`) 추가 → 수용.
- **Step 4 [삭제]**: `delete()` 분리 요청 → AI가 테스트 `delete_callsRepositoryDeleteById` 먼저 추가해 Red 확인, 서비스에 `delete()` 추가로 Green 전환, 컨트롤러에서 `categoryRepository` 직접 호출 및 필드를 제거해 Refactor 완료 → 수용.

**3. 결과 및 근거**
`CategoryService`에 `getAll()`, `create()`, `update()`, `delete()` 4개 메서드 완성. `CategoryController`에서 `CategoryRepository` 직접 의존성 완전 제거. `CategoryServiceTest` 5개 테스트 전부 통과.

---

### [2026-05-13] 상품 CRUD 기능 서비스 레이어 분리

**1. 문제 정의**
`ProductController`가 `ProductRepository`와 `CategoryRepository`를 직접 호출하는 5개 기능(목록 조회, 단건 조회, 생성, 수정, 삭제)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다.

- **Step 1 [목록 조회]**: `getProducts()` 분리 요청 → AI가 `ProductService` 클래스를 신규 생성하고 페이지 변환 로직을 이동, `ProductServiceTest` 파일 신규 생성. 테스트 `getProducts_returnsPageOfProducts` 추가 → 수용.
- **Step 2 [단건 조회]**: `getProduct()` 분리 요청 → AI가 컨트롤러의 null 체크를 `orElseThrow(IllegalArgumentException)` 패턴으로 교체, 컨트롤러는 try-catch로 404 반환. 테스트 2개(`getProduct_existingId_returnsProduct`, `getProduct_nonExistingId_throwsException`) 추가 → 수용.
- **Step 3 [생성]**: `createProduct()` 분리 요청 → AI가 서비스에 `CategoryRepository` 추가, 이름 검증·카테고리 조회·저장 로직을 서비스로 이동. 테스트 3개(`validRequest`, `invalidName`, `categoryNotFound`) 추가 → 수용.
- **Step 4 [수정]**: `updateProduct()` 분리 요청 → AI가 이름 검증·카테고리 조회·상품 조회·저장 로직을 서비스로 이동, 컨트롤러에서 `validateName()` 메서드와 `Category` import 제거. 테스트 4개(`validRequest`, `invalidName`, `categoryNotFound`, `productNotFound`) 추가 → 수용.
- **Step 5 [삭제]**: `deleteProduct()` 분리 요청 → AI가 테스트 `deleteProduct_callsRepositoryDeleteById` 먼저 추가해 Red 확인, 서비스에 `deleteProduct()` 추가로 Green 전환, 컨트롤러에서 `productRepository` 필드를 완전 제거해 Refactor 완료 → 수용.

**3. 결과 및 근거**
`ProductService`에 `getProducts()`, `getProduct()`, `createProduct()`, `updateProduct()`, `deleteProduct()` 5개 메서드 완성. `ProductController`에서 `ProductRepository`·`CategoryRepository` 직접 의존성 완전 제거. `ProductServiceTest` 10개 테스트 전부 통과.

---

### [2026-05-13] 옵션 CRUD 기능 서비스 레이어 분리

**1. 문제 정의**
`OptionController`가 `OptionRepository`·`ProductRepository`를 직접 호출하는 3개 기능(목록 조회, 생성, 삭제)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다.

- **Step 1 [목록 조회]**: `getOptions()` 분리 요청 → AI가 `OptionService` 클래스를 신규 생성하고 상품 존재 확인 + 옵션 목록 조회 로직을 이동. `OptionServiceTest` 파일 신규 생성. 테스트 2개(`existingProductId_returnsOptions`, `nonExistingProductId_throwsException`) 추가 → 수용.
- **Step 2 [생성]**: `createOption()` 분리 요청 → AI가 이름 검증·상품 조회·중복 확인·저장 로직을 서비스로 이동. 테스트 4개(`validRequest`, `invalidName`, `productNotFound`, `duplicateName`) 추가 → 수용.
- **Step 3 [삭제]**: `deleteOption()` 분리 요청 → AI가 상품 조회·마지막 옵션 보호·옵션 존재 확인·삭제 로직을 서비스로 이동. 테스트 4개(`notLastOption_deletesSuccessfully`, `lastOption_throwsException`, `productNotFound`, `optionNotFound`) 추가. 컨트롤러에서 `OptionRepository`·`ProductRepository` 직접 의존성 및 `validateName()` 메서드 완전 제거 → 수용.

**3. 결과 및 근거**
`OptionService`에 `getOptions()`, `createOption()`, `deleteOption()` 3개 메서드 완성. `OptionController`에서 `OptionRepository`·`ProductRepository` 직접 의존성 완전 제거. `OptionServiceTest` 8개 테스트 전부 통과.

---

### [2026-05-13] 찜 목록 CRUD 기능 서비스 레이어 분리

**1. 문제 정의**
`WishController`가 `WishRepository`·`ProductRepository`를 직접 호출하는 3개 기능(목록 조회, 추가, 삭제)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다. 인증 처리(`authenticationResolver.extractMember`)는 컨트롤러 책임으로 유지하고, 비즈니스 로직만 서비스로 이동했다.

- **Step 1 [목록 조회]**: `getWishes(memberId, pageable)` 분리 요청 → AI가 `WishService` 클래스를 신규 생성하고 페이지 변환 로직을 이동. `WishServiceTest` 파일 신규 생성. 테스트 `getWishes_returnsPaginatedWishes` 추가 → 수용.
- **Step 2 [추가]**: `addWish(memberId, productId)` 분리 요청 → AI가 상품 조회·중복 확인·저장 로직을 서비스로 이동, 중복 시 기존 항목 반환 처리를 `orElseGet` 패턴으로 구현. 테스트 3개(`newProduct`, `duplicate_returnsExisting`, `productNotFound`) 추가 → 수용.
- **Step 3 [삭제]**: `removeWish(memberId, wishId)` 분리 요청 → AI가 찜 조회·소유권 확인·삭제 로직을 서비스로 이동, 컨트롤러의 개별 null/403 체크를 서비스의 `IllegalArgumentException` 패턴으로 통일. 테스트 3개(`ownWish_deletesSuccessfully`, `notFound`, `notOwner`) 추가. 컨트롤러에서 `WishRepository`·`ProductRepository` 직접 의존성 완전 제거 → 수용.

**3. 결과 및 근거**
`WishService`에 `getWishes()`, `addWish()`, `removeWish()` 3개 메서드 완성. `WishController`에서 `WishRepository`·`ProductRepository` 직접 의존성 완전 제거. `WishServiceTest` 7개 테스트 전부 통과.

---

### [2026-05-13] 주문 기능 서비스 레이어 분리

**1. 문제 정의**
`OrderController`가 `OrderRepository`·`OptionRepository`·`MemberRepository`·`KakaoMessageClient`를 직접 호출하는 2개 기능(주문 목록 조회, 주문 생성)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다. 인증 처리(`authenticationResolver.extractMember`)는 컨트롤러 책임으로 유지하고, 비즈니스 로직만 서비스로 이동했다.

- **Step 1 [목록 조회]**: `getOrders(memberId, pageable)` 분리 요청 → AI가 `OrderService` 클래스를 신규 생성하고 페이지 변환 로직을 이동. `OrderServiceTest` 파일 신규 생성. 테스트 `getOrders_returnsPaginatedOrders` 추가 → 수용.
- **Step 2 [주문 생성]**: `createOrder(memberId, request)` 분리 요청 → AI가 옵션 조회·재고 차감·포인트 차감·주문 저장·카카오 알림(best-effort) 로직 전체를 서비스로 이동. `sendKakaoMessageIfPossible` private 메서드도 서비스로 이전. 테스트 3개(`validRequest`, `optionNotFound`, `insufficientPoints`) 추가. 컨트롤러에서 모든 레포지토리·클라이언트 직접 의존성 완전 제거 → 수용.

**3. 결과 및 근거**
`OrderService`에 `getOrders()`, `createOrder()` 2개 메서드 완성. `OrderController`에서 `OrderRepository`·`OptionRepository`·`MemberRepository`·`KakaoMessageClient` 직접 의존성 완전 제거, `AuthenticationResolver`와 `OrderService`만 의존. `OrderServiceTest` 4개 테스트 전부 통과.

---

### [2026-05-13] 회원 기능 서비스 레이어 분리

**1. 문제 정의**
`MemberController`가 `MemberRepository`·`JwtProvider`를 직접 호출하는 2개 기능(회원가입, 로그인)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다.

- **Step 1 [회원가입]**: `register()` 분리 요청 → AI가 한 사이클에 `register`와 `login` 2개를 동시 작성 → **거부**. 기능 하나씩 작업한다는 원칙 위반으로 롤백 요청.
- **Step 2 [회원가입 재작업]**: 롤백 후 `register()`만 요청 → AI가 `MemberService` 클래스를 신규 생성하고 이메일 중복 확인·저장·토큰 발급 로직을 이동. `MemberServiceTest`에 테스트 2개(`register_newEmail_returnsToken`, `register_duplicateEmail_throwsException`) 추가 후 Red → Green → Refactor 완료. 컨트롤러의 `register()`가 `memberService.register()`로 위임 → 수용.
- **Step 3 [로그인]**: `login()` 분리 요청 → AI가 `MemberServiceTest`에 테스트 3개(`login_validCredentials_returnsToken`, `login_emailNotFound_throwsException`, `login_wrongPassword_throwsException`) 추가해 Red 확인, `MemberService.login()` 추가로 Green 전환, 컨트롤러의 `login()`을 `memberService.login()` 위임으로 교체 후 `memberRepository`·`jwtProvider` 필드 완전 제거 → 수용.

**3. 결과 및 근거**
`MemberService`에 `register()`, `login()` 2개 메서드 완성. `MemberController`에서 `MemberRepository`·`JwtProvider` 직접 의존성 완전 제거, `MemberService`만 의존. `MemberServiceTest` 5개 테스트 전부 통과.

---

### [2026-05-13] 카카오 인증 기능 서비스 레이어 분리

**1. 문제 정의**
`KakaoAuthController`가 `KakaoLoginClient`·`MemberRepository`·`JwtProvider`·`KakaoLoginProperties`를 직접 호출하는 2개 기능(카카오 인가 URL 리다이렉트, 콜백 처리)이 서비스 레이어 없이 컨트롤러에 섞여 있어 테스트와 역할 분리가 어려웠다.

**2. 상호작용 타임라인**

기능 하나씩 TDD(Red → Green → Refactor) 사이클을 반복하며 분리했다.

- **Step 1 [콜백 처리]**: `processCallback()` 분리 요청 → AI가 `KakaoAuthService` 클래스를 신규 생성하고 토큰 교환·사용자 조회·신규 회원 자동 가입·카카오 토큰 갱신·JWT 발급 로직을 이동. `KakaoAuthServiceTest`에 테스트 2개(`processCallback_newMember_returnsToken`, `processCallback_existingMember_returnsToken`) 추가 → 수용.
- **Step 2 [인가 URL 생성]**: `getAuthorizationUrl()` 분리 요청 → AI가 `KakaoLoginProperties`를 서비스로 이동하고 URL 빌딩 로직을 `getAuthorizationUrl()`로 추출. 테스트 `getAuthorizationUrl_returnsKakaoUrl` 추가. 컨트롤러에서 `KakaoLoginProperties` 필드 제거 → 수용.

**3. 결과 및 근거**
`KakaoAuthService`에 `processCallback()`, `getAuthorizationUrl()` 2개 메서드 완성. `KakaoAuthController`에서 모든 직접 의존성 제거, `KakaoAuthService`만 의존. `KakaoAuthServiceTest` 3개 테스트 전부 통과.

---

### [2026-05-25] Product 도메인 검증 내재화

**1. 문제 정의**
`ProductNameValidator`가 길이·문자 불변식을 검증하지만 `ProductService`를 거치지 않으면 검증 없이 `Product`를 생성할 수 있어 도메인 불변식이 보장되지 않았다.

**2. 상호작용 타임라인**

- **Step 1**: "validation 검증을 ProductNameValidator 따로 만드는 것보다 도메인 안에 검증 로직이 있는 게 좋을 것 같다" → AI가 설계 분석: 길이·문자 불변식은 `Product` 내부로, 권한 기반 "카카오" 정책은 `ProductService`에 유지하는 방향 제안 → 수용.
- **Step 2**: `ProductTest` 신규 작성 (8개 케이스: null/blank/16자/특수문자 → 예외, 15자/정상 → 성공) → Red 확인.
- **Step 3**: `Product` 생성자·`update()`에 `validateName()` private 메서드 추가. `ProductNameValidator`는 `containsKakao()` 메서드만 남기고 나머지 제거. `ProductService`는 `validate()` 전체 호출 → `validateNotKakao()` 호출로 교체. 관리자 메서드(`adminCreateProduct`, `adminUpdateProduct`)는 카카오 제한 없으므로 체크 제거 → Green 전환 → 수용.

**3. 결과 및 근거**
`Product` 생성자·`update()` 호출 시 항상 불변식을 검증하여 `ProductService` 우회로 인한 잘못된 도메인 객체 생성이 불가능해졌다. `ProductTest` 8개, 전체 테스트 통과.

---

## 아키텍처 결정 기록 (Architecture Decision Records)

> 되돌리기 어렵거나 반복 규칙이 생기는 설계 결정을 기록합니다.

---

### ADR-001: 도메인 패키지 내 레이어 서브패키지 구조 채택

**상태**: 채택 (2026-05-24)

**컨텍스트**

각 도메인 패키지(category, member, option, order, product, wish)에 Entity, Repository, Service, Controller, DTO가 같은 깊이에 나열되어 있었다. 파일 수가 늘수록 역할 구분이 어렵고 IDE 탐색 비용이 높아졌다.

**선택지**

| 구조 | 장점 | 단점 |
|---|---|---|
| 현 구조 유지 (flat) | 변경 없음 | 파일이 많아지면 역할 구분 불가 |
| 레이어 먼저 → 도메인 (`service/product/`) | Spring 기본 예제와 유사 | 도메인 응집도 낮음; 한 도메인 파일이 여러 패키지에 분산 |
| 도메인 먼저 → 레이어 (`product/model/`) | 도메인 응집도 유지 + 역할 명시 | 패키지 이동 비용 발생 |

**결정**

`<domain>/model/`, `<domain>/repository/`, `<domain>/service/`, `<domain>/web/` 4개 서브패키지 구조를 채택한다.

- `model/`: Entity, Validator (도메인 핵심 객체)
- `repository/`: JpaRepository 인터페이스
- `service/`: Service + Request/Response DTO + 외부 Client
- `web/`: Controller

**결과**

패키지 이름만으로 파일의 역할을 즉시 파악할 수 있다. `auth`, `exception`은 도메인이 아닌 인프라 관심사이므로 서브패키지 적용 대상에서 제외한다.

---

### ADR-002: Request/Response DTO를 `service/` 패키지에 배치

**상태**: 채택 (2026-05-24)

**컨텍스트**

ADR-001 구조 설계 시 DTO(Request, Response)를 `service/`와 `web/` 중 어느 쪽에 둘지 결정해야 했다.

**선택지**

| 위치 | 설명 | 문제 |
|---|---|---|
| `web/` | 컨트롤러와 물리적으로 가까움 | `service/`가 `web/`의 타입을 반환해야 하므로 역방향 의존(service → web) 발생 |
| `service/` | 서비스 인터페이스의 일부로 DTO를 정의 | 컨트롤러가 서비스 패키지를 import → 단방향 의존(web → service) 유지 |

**결정**

DTO를 `service/` 패키지에 배치한다. 서비스가 자신의 입출력 계약(Contract)을 소유하며, 컨트롤러는 서비스 패키지만 import하면 된다.

**결과**

`web → service → model/repository` 단방향 의존 그래프가 성립한다. `service/`가 `web/` 타입을 참조하는 역방향 의존이 발생하지 않는다.

---

### ADR-003: 크로스 도메인 연관 표현 — 엔티티 참조 vs 원시 FK

**상태**: 채택 (2026-05-13)

**컨텍스트**

도메인 경계를 넘는 연관 관계(예: `Order → Member`, `Wish → Member`)를 JPA `@ManyToOne` 엔티티 참조로 표현할지, 원시 타입 FK(`Long memberId`)로 표현할지 결정해야 했다.

**선택지**

| 방식 | 장점 | 단점 |
|---|---|---|
| `@ManyToOne` 엔티티 참조 | 객체 그래프 탐색 가능, JPA 관계 명시 | 쿼리 시 JOIN 발생, 도메인 경계 결합도 증가 |
| 원시 FK (`Long memberId`) | 도메인 간 결합도 낮음, 필요 시에만 별도 조회 | 객체 탐색 불가, 코드에서 관계가 명시적이지 않음 |

**결정**

연관 엔티티의 **필드를 직접 탐색해야 하는 경우**에만 `@ManyToOne`을 사용하고, **ID만 필요한 경우**는 원시 FK를 사용한다.

- `Order.option` → `@ManyToOne Option` (옵션명·상품 가격 탐색 필요)
- `Wish.product` → `@ManyToOne Product` (상품명·가격·이미지 탐색 필요)
- `Order.memberId` → `Long` (멤버 ID만 저장, 별도 조회로 충분)
- `Wish.memberId` → `Long` (소유권 확인용 ID만 필요)

**결과**

도메인 경계를 넘는 불필요한 JOIN을 줄이고, 단방향 참조만 유지한다. 새 연관 추가 시 "필드 탐색 여부"를 기준으로 일관되게 판단한다.

---

### ADR-004: 카카오 알림 전송 실패 시 주문 롤백 안 함

**상태**: 채택 (2026-05-13)

**컨텍스트**

`OrderService.createOrder()`는 주문 저장 후 카카오톡 알림을 보낸다. 외부 API 호출이 실패하면 주문 자체를 롤백할지, 알림만 무시하고 주문은 성공으로 처리할지 결정해야 했다.

**선택지**

| 방식 | 장점 | 단점 |
|---|---|---|
| 실패 시 주문 롤백 | 알림과 주문의 일관성 보장 | 카카오 서버 장애가 결제 실패로 이어짐 — 사용자 피해 |
| 실패 시 로그만 남기고 주문 성공 유지 | 외부 의존성이 핵심 트랜잭션에 영향 안 미침 | 알림 미전송 시 사용자 인지 불가 |

**결정**

카카오 알림은 부가 기능으로 분류하여, 전송 실패(예외 발생)해도 주문 트랜잭션을 롤백하지 않는다. `sendKakaoMessageIfPossible()`에서 모든 예외를 catch하고 로그 없이 무시한다.

```java
private void sendKakaoMessageIfPossible(...) {
    try {
        kakaoMessageClient.sendToMe(...);
    } catch (Exception ignored) { }
}
```

**결과**

카카오 서버 장애·네트워크 오류가 주문 실패로 번지지 않는다. 카카오 액세스 토큰이 없는 일반 회원은 알림 자체를 시도하지 않는다.

---

### ADR-005: 도메인 검증 예외를 `IllegalArgumentException` 단일 타입으로 통일

**상태**: 채택 (2026-05-24)

**컨텍스트**

도메인 규칙 위반(존재하지 않는 리소스 조회, 중복 값, 잘못된 입력 등)을 표현하기 위한 예외 타입을 결정해야 했다. 사용자 정의 예외 계층을 만들 수도 있고, 표준 예외를 그대로 쓸 수도 있다.

**선택지**

| 방식 | 장점 | 단점 |
|---|---|---|
| 사용자 정의 예외 계층 (`NotFoundException`, `DuplicateException` 등) | 예외 종류별 세밀한 HTTP 상태 매핑 가능 | 클래스 수 증가; 일관성 유지 비용 |
| `IllegalArgumentException` 단일 타입 | 코드 단순; 추가 클래스 없음 | HTTP 상태 구분이 메시지에만 의존 |

**결정**

`IllegalArgumentException`을 모든 도메인 검증 예외에 사용하고, `GlobalExceptionHandler`에서 → HTTP 400 Bad Request로 일괄 변환한다.

현 단계에서 404 Not Found와 400 Bad Request를 구별해야 하는 클라이언트 요구사항이 없으므로, 사용자 정의 예외 계층 도입 비용이 실익보다 크다.

**결과**

예외 처리 로직이 `GlobalExceptionHandler` 1개로 집중된다. 새 도메인 규칙 위반은 `throw new IllegalArgumentException("...")` 한 줄로 추가된다. 향후 404 분리가 필요해지면 사용자 정의 예외를 도입하고 핸들러에 추가한다.

---

### ADR-006: 테스트 계층 전략 — 단위·슬라이스·DB 테스트 분리

**상태**: 채택 (2026-05-13)

**컨텍스트**

테스트를 어느 계층에서, 어떤 도구로 작성할지 결정해야 했다. `@SpringBootTest` 풀 컨텍스트 테스트를 기본으로 쓸 수도 있고, 계층별로 슬라이스 테스트를 쓸 수도 있다.

**선택지**

| 방식 | 속도 | 격리도 | 비고 |
|---|---|---|---|
| `@SpringBootTest` 풀 컨텍스트 | 느림 | 낮음 | 설정 오류를 통합적으로 잡을 수 있음 |
| 계층별 슬라이스 테스트 | 빠름 | 높음 | 계층 경계를 명확히 검증 |

**결정**

계층별로 적합한 도구를 선택한다.

| 계층 | 도구 | 이유 |
|---|---|---|
| Service (단위 테스트) | `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + BDDMockito | 외부 의존성 없이 비즈니스 로직만 검증 |
| Controller (슬라이스) | `@WebMvcTest` + `@MockitoBean` | HTTP 요청/응답 형식과 라우팅만 검증; 서비스는 Mock |
| Repository (DB 슬라이스) | `@DataJpaTest` + `TestEntityManager` | 제약 조건·쿼리 메서드를 H2 인메모리 DB로 검증 |

`@SpringBootTest`는 사용하지 않는다 — 모든 계층이 슬라이스 테스트로 충분히 커버되고, 부트 시간이 길어지는 비용이 없다.

**결과**

전체 테스트가 약 17초 이내에 완료된다. 서비스 로직 변경은 MockitoExtension 테스트에서, 컨트롤러 라우팅 변경은 WebMvcTest에서, DB 제약 변경은 DataJpaTest에서 즉시 감지된다.

---

## 데이터베이스 스키마 (Database Schema)

```
category ──< product ──< options
                             │
member ──< wish ──> product  │
member ──< orders ───────────┘
```

| 테이블 | 주요 컬럼 |
|---|---|
| `category` | id, name(unique), color, image_url, description |
| `product` | id, name(max 15자), price, image_url, category_id |
| `options` | id, product_id, name(max 50자), quantity |
| `member` | id, email(unique), password, kakao_access_token, point |
| `wish` | id, member_id, product_id |
| `orders` | id, option_id, member_id, quantity, message, order_date_time |

---

### [2026-05-24] 서비스 메서드에 @Transactional 추가

**1. 문제 정의**
복수 DB 쓰기 작업을 하나의 메서드에서 수행하는 5개 메서드에 `@Transactional`이 없어, 중간 단계에서 실패 시 일부 쓰기만 반영되는 데이터 정합성 문제가 있었다.
예: `OrderService.createOrder()`에서 옵션 재고는 차감됐지만 포인트 차감 단계에서 예외가 발생해도 재고 차감이 롤백되지 않는 상황.

**2. 상호작용 타임라인**
- **Step 1**: 복수 DB 쓰기 메서드 목록 식별 요청 → AI가 5개 대상 메서드(`OrderService.createOrder`, `CategoryService.update`, `MemberService.updateMember`, `MemberService.chargePoint`, `KakaoAuthService.processCallback`) 파악 → 수용
- **Step 2**: 각 메서드에 `@Transactional` 추가 요청 → AI가 메서드 단위로 어노테이션 적용, 클래스 레벨 사용 안 함 → 수용

**3. 결과 및 근거**
4개 서비스 파일에 `@Transactional` 추가 완료. 전체 테스트(CategoryServiceTest, MemberServiceTest, OrderServiceTest, KakaoAuthServiceTest 등) 통과.

---

### [2026-05-24] @RestControllerAdvice 전역 예외 처리기 도입

**1. 문제 정의**
컨트롤러마다 예외 처리 방식이 달라 API 에러 응답이 일관되지 않았다. `MemberController`·`ProductController`·`OptionController`는 `@ExceptionHandler`를 각각 선언하고, `CategoryController`는 try-catch로 `IllegalArgumentException`을 404로 잘못 매핑했으며, `WishController`·`OrderController`는 핸들러가 없어 서비스 예외 시 500을 반환했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `GlobalExceptionHandlerTest` 작성 — `CategoryController.update()`에서 `IllegalArgumentException` 발생 시 400을 기대. 기존 try-catch가 404를 반환하므로 FAIL → 수용
- **Step 2 [Green]**: `gift.exception.GlobalExceptionHandler`(`@RestControllerAdvice`) 생성 — `IllegalArgumentException` → 400 Bad Request + 메시지 바디
- **Step 3 [Refactor]**: 4개 컨트롤러에서 중복·불일치 코드 제거
  - `CategoryController`: `updateCategory()` try-catch 제거
  - `ProductController`: `getProduct()` try-catch + `@ExceptionHandler` 제거
  - `OptionController`: `getOptions()` try-catch + `@ExceptionHandler` 제거
  - `MemberController`: `@ExceptionHandler` 제거

**3. 결과 및 근거**
`GlobalExceptionHandler` 1개로 모든 `IllegalArgumentException`을 400으로 통일. `GlobalExceptionHandlerTest` 2개 케이스 포함 전체 테스트 통과.

---

### [2026-05-24] AdminProductController 서비스 레이어 분리

**1. 문제 정의**
`AdminProductController`가 `ProductRepository`와 `CategoryRepository`를 직접 주입해 서비스 레이어를 우회하고 있었다. REST API 컨트롤러(`ProductController`)는 이미 분리됐지만 어드민 컨트롤러만 남아 있던 구조적 불일치를 해소했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `ProductServiceTest`에 `getProductEntity`, `adminCreateProduct`, `adminUpdateProduct` 신규 메서드 테스트 10개 추가 → 컴파일 오류(RED) 확인 → 수용
- **Step 2 [Green]**: `ProductService`에 `getProductEntity()`, `adminCreateProduct()`, `adminUpdateProduct()` 3개 메서드 구현 → 테스트 GREEN 전환 → 수용. `adminCreate/Update`는 `allowKakao=true`로 카카오 상품명 허용
- **Step 3 [Red]**: `AdminProductControllerTest` 신규 작성 (list/newForm/create/editForm/update/delete 8개 케이스) → 수용
- **Step 4 [Refactor]**: `AdminProductController` 리팩토링 — `ProductRepository`, `CategoryRepository` 필드 제거, `CategoryService` 주입으로 교체 → 수용

**3. 결과 및 근거**
`AdminProductController`에서 `ProductRepository`, `CategoryRepository` 직접 의존성 완전 제거. `ProductService`와 `CategoryService`만 의존. `ProductServiceTest` 10개 신규 + `AdminProductControllerTest` 8개 신규 테스트 전부 통과.

---

### [2026-05-24] CategoryService.update() 포맷 버그 수정

**1. 문제 정의**
`CategoryService.update()` 메서드의 닫는 `}`가 4칸 들여쓰기 대신 컬럼 0에 위치해 있었다. Java 컴파일에는 영향이 없지만 ktlint 스타일 검사 실패 원인이며 코드 가독성을 해친다.

**2. 상호작용 타임라인**
- **Step 1**: git diff로 버그 위치 확인 요청 → AI가 32번 라인의 `}` 들여쓰기 오류(0칸 → 4칸) 식별 → 수용
- **Step 2**: IDE에서 파일을 열어 확인하는 사이 자동 포맷으로 수정 완료 → AI가 현재 파일 상태 재확인 후 이미 올바르게 수정됨을 확인 → 수용

**3. 결과 및 근거**
`CategoryService.update()` 닫는 `}` 들여쓰기 4칸으로 수정 완료. `CategoryServiceTest` 5개 테스트 전부 통과, `ktlintCheck` 오류 없음.

---

### [2026-05-24] 주문 시 찜 목록 자동 삭제

**1. 문제 정의**
주문이 완료됐을 때 해당 상품이 찜 목록에 있어도 자동으로 삭제되지 않았다. 사용자가 구매 완료 후 찜 목록을 직접 지워야 하는 UX 문제가 있었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OrderServiceTest`에 `createOrder_wishExists_removesWish`, `createOrder_noWish_proceedsNormally` 2개 테스트 추가. `WishRepository` Mock 미선언 상태여서 FAIL → 수용
- **Step 2 [Green]**: `OrderService`에 `WishRepository` 의존성 추가, `createOrder()` 내 `wishRepository.findByMemberIdAndProductId().ifPresent(wishRepository::delete)` 로직 추가로 GREEN 전환 → 수용

**3. 결과 및 근거**
`OrderService.createOrder()` 완료 시 해당 상품의 찜이 있으면 자동 삭제. `OrderServiceTest` 6개 테스트(기존 4 + 신규 2) 전부 통과.

---

### [2026-05-24] KakaoAuthService·AuthenticationResolver 의존성 정리

**1. 문제 정의**
`KakaoAuthService`와 `AuthenticationResolver`가 `MemberRepository`를 직접 주입해 서비스 레이어를 우회하고 있었다. 회원 관련 로직은 `MemberService`로 집중되어야 하지만, 두 클래스가 레포지토리에 직접 접근해 응집성을 해쳤다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `MemberServiceTest`에 `findByEmailOrNull`(2개), `findOrCreateKakaoMember`(2개) 테스트 추가 → 메서드 미존재로 컴파일 오류(RED) 확인 → 수용
- **Step 2 [Green]**: `MemberService`에 `findByEmailOrNull()`, `findOrCreateKakaoMember()` 2개 메서드 구현 → 테스트 GREEN 전환 → 수용
- **Step 3 [Refactor]**: `KakaoAuthService`에서 `MemberRepository` → `MemberService` 교체, `processCallback()`을 `memberService.findOrCreateKakaoMember()` 위임으로 단순화. `AuthenticationResolver`에서 `MemberRepository` → `MemberService` 교체, Javadoc 주석 제거. `KakaoAuthServiceTest`의 `@Mock MemberRepository` → `@Mock MemberService` 교체 → 수용

**3. 결과 및 근거**
`KakaoAuthService`와 `AuthenticationResolver`에서 `MemberRepository` 직접 의존성 완전 제거. `MemberService`만 의존. 전체 테스트 통과.

---

### [2026-05-24] wish 테이블 (member_id, product_id) 유니크 제약 추가

**1. 문제 정의**
`wish` 테이블에 `(member_id, product_id)` 유니크 제약이 없어, 동시 요청 시 애플리케이션 레벨 중복 체크를 통과하더라도 DB에 중복 찜이 삽입될 수 있었다. `WishService.addWish()`의 중복 방지 로직은 race condition에 취약하다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `@DataJpaTest` 기반 `WishRepositoryTest` 작성 — 동일 `(member_id, product_id)` 조합 두 번 저장 후 `DataIntegrityViolationException` 기대. 제약 미존재로 FAIL → 수용
- **Step 2 [Green]**: `Wish` 엔티티에 `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"}))` 추가 → H2 테스트 스키마에 유니크 제약 생성, 테스트 GREEN → 수용
- **Step 3 [Refactor]**: `V3__Add_wish_unique_constraint.sql` 생성 — MySQL 운영 DB에 `ALTER TABLE wish ADD CONSTRAINT uq_wish_member_product UNIQUE (member_id, product_id)` 적용 → 수용

**3. 결과 및 근거**
`Wish` 엔티티 `@UniqueConstraint` + Flyway `V3` 마이그레이션으로 애플리케이션·DB 양 레벨에서 중복 찜이 방지된다. `WishRepositoryTest` 1개 통과, 전체 테스트 GREEN.

---

### [2026-05-24] AdminMemberController 테스트 작성

**1. 문제 정의**
`AdminMemberController`에 테스트가 없어, 7개 엔드포인트(목록/추가폼/생성/수정폼/수정/포인트충전/삭제)의 동작이 코드 변경 시 검증되지 않았다.

**2. 상호작용 타임라인**
- **Step 1**: `AdminMemberControllerTest` 파일 작성 요청 → AI가 `@WebMvcTest(AdminMemberController.class)` + `@MockitoBean MemberService` 기반으로 7개 케이스 작성 → 수용
  - `list`: 200 + `member/list` 뷰 + `members` 모델
  - `newForm`: 200 + `member/new` 뷰
  - `create` (정상): redirect `/admin/members`
  - `create` (중복 이메일): 200 + `member/new` 뷰 + `error`, `email` 모델
  - `editForm`: 200 + `member/edit` 뷰 + `member` 모델
  - `update`: redirect `/admin/members`
  - `chargePoint`: redirect `/admin/members`
  - `delete`: redirect `/admin/members`

**3. 결과 및 근거**
`AdminMemberControllerTest` 7개 테스트 전부 통과. 전체 테스트 GREEN.

---

### [2026-05-24] 비밀번호 BCrypt 해싱 적용

**1. 문제 정의**
`MemberService`가 비밀번호를 평문으로 저장하고(`new Member(email, password)`), `login()`에서 `.equals(password)` 평문 비교를 사용했다. DB 유출 시 모든 비밀번호가 즉시 노출되는 보안 결함이었다.

**2. 상호작용 타임라인**
- **Step 1 [의존성]**: `build.gradle.kts`에 `spring-security-crypto` 추가 (Spring Security 전체가 아닌 crypto 모듈만)
- **Step 2 [Red]**: `MemberServiceTest`에 3개 케이스 추가·수정
  - `login_validCredentials_returnsToken` — 모의 Member의 비밀번호를 `encoder.encode("password")`로 교체 → `equals()` 비교로 FAIL 확인
  - `register_encodesPasswordBeforeSaving` — `ArgumentCaptor`로 저장 시 BCrypt 해시 여부 검증 → FAIL 확인
  - `adminCreate_encodesPasswordBeforeSaving` — 동일 패턴 → FAIL 확인
- **Step 3 [Green]**: `MemberService`에 `BCryptPasswordEncoder encoder` 필드 추가, `adminCreate()·register()` 저장 시 `encoder.encode()`, `login()` 비교 시 `encoder.matches()` 적용 → 3개 테스트 GREEN
- **Step 4 [Refactor]**: `V2__Insert_default_data.sql` seed 비밀번호에 개발용 평문임을 명시하는 주석 추가

**3. 결과 및 근거**
`register()`, `adminCreate()` 저장 시 BCrypt 인코딩, `login()` 비교 시 `matches()` 사용. 전체 테스트 통과.

---

### [2026-05-24] 상품이 있는 카테고리 삭제 시 FK 에러 방지

**1. 문제 정의**
`CategoryService.delete()`가 상품이 있는 카테고리를 그대로 `deleteById()`로 삭제 시도해, MySQL FK 제약(`product.category_id`)이 `DataIntegrityViolationException`(500)을 던졌다. 서비스 레벨에서 사전 검사가 없었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `CategoryServiceTest`에 `@Mock ProductRepository` 추가, 테스트 2개 교체
  - `delete_categoryWithNoProducts_deletesSuccessfully` — `existsByCategoryId` false 시 정상 삭제
  - `delete_categoryWithProducts_throwsException` — `existsByCategoryId` true 시 `IllegalArgumentException`
  → `ProductRepository.existsByCategoryId()` 미존재로 컴파일 오류 → 메서드 추가 후 런타임 FAIL(Red) 확인
- **Step 2 [Green]**: `CategoryService`에 `ProductRepository` 주입, `delete()` 첫 줄에 `existsByCategoryId` 검사 추가 → 테스트 GREEN

**3. 결과 및 근거**
상품이 있는 카테고리 삭제 시 `IllegalArgumentException` 발생 → `GlobalExceptionHandler`가 400으로 변환. DB FK 에러 없음. `CategoryServiceTest` 6개 테스트 전부 통과.

---

### [2026-05-24] 주문이 있는 상품·옵션 삭제 시 FK 에러 방지

**1. 문제 정의**
`ProductService.deleteProduct()`와 `OptionService.deleteOption()`이 주문이 존재하는 상품·옵션을 삭제 시도할 때 DB FK 제약(`orders.option_id`)이 `DataIntegrityViolationException`(500)을 던졌다. 서비스 레벨 사전 검사가 없었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OrderRepository`에 `existsByOptionId()`, `existsByOptionProductId()` 쿼리 메서드 추가 → `ProductServiceTest`에 `@Mock OrderRepository` + `deleteProduct_hasOrders_throwsException` 추가, `OptionServiceTest`에 `@Mock OrderRepository` + `deleteOption_hasOrders_throwsException` 추가, `deleteOption_notLastOption_deletesSuccessfully`에 `existsByOptionId` → false 스텁 추가 → 서비스에 검사 없어 FAIL(Red) 확인
- **Step 2 [Green]**: `ProductService`에 `OrderRepository` 주입, `deleteProduct()` 첫 줄에 `existsByOptionProductId` 검사 추가. `OptionService`에 `OrderRepository` 주입, `deleteOption()` option 조회 후 `existsByOptionId` 검사 추가 → 테스트 GREEN

**3. 결과 및 근거**
주문이 있는 상품·옵션 삭제 시 `IllegalArgumentException` 발생 → `GlobalExceptionHandler`가 400으로 변환. `ProductServiceTest` 3개 신규 + `OptionServiceTest` 1개 신규 테스트 포함 전체 테스트 통과.

---

### [2026-05-24] Option.subtractQuantity() 음수·0 수량 방어 추가

**1. 문제 정의**
`Option.subtractQuantity()`가 `amount <= 0` 검사 없이 재고를 차감했다. 0으로 호출 시 재고가 변하지 않는 silent no-op, 음수로 호출 시 재고가 오히려 증가하는 로직 버그가 있었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OptionTest` 신규 생성 — `subtractQuantity` 4개 케이스 작성. 0·음수 케이스 2개가 FAIL(Red) 확인
- **Step 2 [Green]**: `subtractQuantity()` 첫 줄에 `if (amount <= 0)` 검사 추가, `Member.deductPoint()` 와 동일 패턴 적용 → 전체 GREEN

**3. 결과 및 근거**
`subtractQuantity(0)`, `subtractQuantity(-1)` 모두 `IllegalArgumentException` 발생. `OptionTest` 4개 테스트 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] OrderRequest.message 길이 제한 추가

**1. 문제 정의**
`OrderRequest.message`에 길이 제한이 없어, 255자를 초과하는 메시지 전송 시 DB `varchar(255)` 컬럼 초과로 런타임 오류가 발생했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OrderRequestTest` 신규 생성 — Jakarta `Validator`로 `message` 256자 케이스 검증 실패 기대 → FAIL(Red) 확인
- **Step 2 [Green]**: `OrderRequest.message`에 `@Size(max = 255)` 추가 → 검증 통과, 전체 GREEN

**3. 결과 및 근거**
`message` 256자 이상 시 Jakarta Validation이 400을 반환. `null`·255자 케이스는 정상 통과. `OrderRequestTest` 3개 테스트 전부 통과.

---

### [2026-05-24] KakaoMessageClient JSON 이스케이프 처리

**1. 문제 정의**
`KakaoMessageClient.buildTemplate()`이 상품명·옵션명·메시지를 `String.formatted()`로 JSON 문자열에 직접 삽입했다. `"` `\` `\n` 등 특수문자가 포함되면 JSON 파싱 오류가 발생하는 안정성 결함이 있었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `KakaoMessageClientTest` 신규 작성. `buildTemplate()`을 package-private으로 변경, `ObjectMapper` 생성자 주입 추가. 메시지에 `"hello"` 포함 시 유효한 JSON인지 검증 → FAIL(Red) 확인
- **Step 2 [Green]**: `escapeJson(value)` private 헬퍼 추가 — `objectMapper.writeValueAsString(value)`로 Jackson이 이스케이프한 뒤 외곽 따옴표를 제거. 상품명·옵션명·메시지 세 곳에 적용 → 전체 GREEN

**3. 결과 및 근거**
`"` `\` 등 JSON 특수문자를 Jackson이 안전하게 이스케이프. `KakaoMessageClientTest` 3개 케이스(정상·따옴표포함·null) 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] OrderResponse 개선 (상품명·옵션명·총금액 추가)

**1. 문제 정의**
`OrderResponse`가 `optionId`만 반환해 클라이언트가 상품명·옵션명·총금액을 알려면 추가 API를 호출해야 했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OrderServiceTest` 두 케이스에 `productName()`, `optionName()`, `totalPrice()` 검증 추가 → 필드 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `OrderResponse` 레코드에 `productName`, `optionName`, `totalPrice` 추가. `from()` 팩토리에서 `option.getProduct().getName()`, `option.getName()`, `product.getPrice() * quantity` 로 채움 → 전체 GREEN

**3. 결과 및 근거**
클라이언트가 단일 주문 응답만으로 상품명·옵션명·총금액 확인 가능. `OrderServiceTest` 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] ProductResponse에 categoryName 추가

**1. 문제 정의**
`ProductResponse`가 `categoryId`만 반환해 클라이언트가 카테고리 이름을 표시하려면 별도 카테고리 API를 호출해야 했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `ProductServiceTest`의 `getProduct_existingId_returnsProduct`, `createProduct_validRequest_returnsCreatedProduct` 두 케이스에 `categoryName()` 검증 추가 → 필드 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `ProductResponse` 레코드에 `categoryName` 필드 추가, `from()` 팩토리에서 `product.getCategory().getName()`으로 채움 → 전체 GREEN

**3. 결과 및 근거**
클라이언트가 상품 조회 한 번으로 카테고리 이름까지 확인 가능. `ProductServiceTest` 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] 상품 삭제 시 연관 찜 먼저 삭제 (F-10)

**1. 문제 정의**
`ProductService.deleteProduct()`가 상품을 삭제할 때 연관된 `wish` 레코드를 제거하지 않아, 찜이 있는 상품 삭제 시 DB FK 제약 위반 오류가 발생했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `ProductServiceTest`에 `deleteProduct_withWishes_removesWishesFirst` 테스트 추가. `WishRepository.deleteByProductId()` 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `WishRepository`에 `deleteByProductId(Long productId)` 추가, `ProductService`에 `WishRepository` 의존성 주입, `deleteProduct()`에서 주문 검사 후 `wishRepository.deleteByProductId(id)` → `productRepository.deleteById(id)` 순으로 실행

**3. 결과 및 근거**
DB CASCADE 대신 서비스 레이어에서 명시적으로 찜을 먼저 삭제해 의도가 코드에 드러남. `ProductServiceTest` 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] 옵션 수정 API 추가 (F-11)

**1. 문제 정의**
옵션 생성·삭제만 가능하고 수정 API가 없어, 관리자가 재고를 보정하거나 옵션명을 변경할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OptionServiceTest`에 `updateOption()` 케이스 5개 추가 (유효 요청·유효하지 않은 이름·중복명·자기 자신과 같은 이름·존재하지 않는 옵션). `OptionService.updateOption()` 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `Option.update(name, quantity)` 엔티티 메서드 추가. `OptionRepository`에 `existsByProductIdAndNameAndIdNot()` 추가(자기 자신 제외 중복 체크). `OptionService.updateOption()` 구현. `OptionController`에 `PUT /{optionId}` 핸들러 추가 → 전체 GREEN

**3. 결과 및 근거**
`PUT /api/products/{productId}/options/{optionId}`로 옵션명·재고 수정 가능. 자기 자신과 동일한 이름은 허용, 타 옵션과 중복명은 차단. `OptionServiceTest` 14개 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] 회원 본인 정보 조회 API 추가 (F-8, GET /api/members/me)

**1. 문제 정의**
로그인한 회원이 자신의 이메일과 포인트 잔액을 조회하는 API가 없어 마이페이지·주문 전 포인트 확인이 불가능했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `MemberServiceTest`에 `getMyInfo_existingEmail_returnsEmailAndPoint` 추가. `MemberResponse`·`getMyInfo()` 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `MemberResponse(email, point)` 레코드 신규 생성. `MemberService.getMyInfo()` 구현(이메일로 회원 조회 후 DTO 반환). `MemberController`에 `GET /me` 핸들러 추가 — `AuthenticationResolver`로 회원 추출, 미인증 시 401 반환 → 전체 GREEN

**3. 결과 및 근거**
`GET /api/members/me`에 유효한 JWT를 보내면 `{ "email": "...", "point": 5000 }` 응답. 토큰이 없거나 유효하지 않으면 401 반환. `MemberServiceTest` 전부 통과, 전체 테스트 GREEN.

---

### [2026-05-24] 도메인 패키지 세분화 (model/repository/service/web)

**1. 문제 정의**
각 도메인 패키지(category, member, option, order, product, wish) 안에 Entity, Repository, Service, Controller, DTO가 모두 같은 깊이에 나열되어 있어 파일이 많아질수록 역할 구분이 어렵다.

**2. 상호작용 타임라인**
- **Step 1**: 패키지 세분화 방향 제안 요청 → AI가 `model/`, `repository/`, `service/`, `web/` 4개 서브패키지 구조 + DTO는 `service/`에 배치(컨트롤러가 서비스 인터페이스를 import하는 단방향 의존) 제안 → 수용
- **Step 2**: 도메인별 순서(category → wish → option → order → product → member) 확정 → 각 도메인마다 파일 이동 + package 선언 수정 + 크로스 도메인 import 수정 + 테스트 GREEN 확인 후 커밋 진행
- **Step 3**: 예상치 못한 이슈들 처리 — 같은 패키지에 있던 테스트 파일은 이동 없이 explicit import 추가; `KakaoMessageClient.buildTemplate()`은 package-private이었으나 패키지가 달라져 `public`으로 변경

**3. 결과 및 근거**
6개 도메인 모두 세분화 완료. 각 도메인 커밋마다 전체 테스트 GREEN 확인. 최종 `./gradlew ktlintCheck` 통과.

---

### [2026-05-25] 커스텀 예외 계층 도입 — NotFoundException(404) / DuplicateException(409) (Task 10)

**1. 문제 정의**
`GlobalExceptionHandler`가 모든 예외를 `IllegalArgumentException`으로 통일하여 HTTP 400만 반환하고 있어 클라이언트가 "찾을 수 없음(404)"과 "중복(409)"을 구분할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `GlobalExceptionHandlerTest`에 `NotFoundException → 404`, `DuplicateException → 409` 케이스 추가 — `NotFoundException`·`DuplicateException` 클래스 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `gift.exception` 패키지에 `BusinessException(abstract)`, `NotFoundException`, `DuplicateException` 생성. `GlobalExceptionHandler`에 타입별 핸들러 추가 → `GlobalExceptionHandlerTest` 4개 전부 GREEN
- **Step 3 [Refactor]**: 서비스 레이어 6개 클래스(`CategoryService`, `MemberService`, `OrderService`, `OptionService`, `WishService`, `ProductService`) 에서 "not found" 케이스 → `NotFoundException`, 중복 이메일·옵션명 케이스 → `DuplicateException`으로 교체. 기존 테스트 25개 예외 타입 어노테이션 업데이트(단, 로그인 실패·비즈니스 규칙 위반은 `IllegalArgumentException` 유지)

**3. 결과 및 근거**
HTTP 상태 코드가 의미에 맞게 분리됨: 리소스 없음 → 404, 중복 등록 → 409, 유효하지 않은 입력 → 400. 전체 테스트 123개 전부 GREEN.

---

### [2026-05-25] MemberService JWT 의존 제거 — AuthService 분리 (Task 11)

**1. 문제 정의**
`MemberService`(도메인 서비스)가 `JwtProvider`(인프라)를 직접 주입받아 `TokenResponse`를 반환하고 있어 레이어 위반이었고, 토큰 발급 책임이 도메인 서비스에 혼재되어 있었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `AuthServiceTest` 신규 작성 — `AuthService.register()`, `AuthService.login()` 메서드가 없어 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `MemberService`에 `registerMember(email, password): Member`, `authenticate(email, password): Member` 추가. `gift.auth.AuthService` 신규 생성 — `MemberService` 호출 후 `JwtProvider`로 토큰 발급 → `AuthServiceTest` 5개 전부 GREEN
- **Step 3 [Refactor]**: `MemberController`에서 register/login 위임 대상을 `MemberService` → `AuthService`로 변경. `MemberService`에서 구 `register()`, `login()`, `JwtProvider` 필드 완전 제거. `MemberServiceTest`에서 JwtProvider Mock 제거, 관련 테스트를 `registerMember`/`authenticate` 기반으로 재작성

**3. 결과 및 근거**
`MemberService`에 `JwtProvider` 의존 없음. 토큰 발급은 `auth` 계층(`AuthService`, `KakaoAuthService`)에서만 수행하는 대칭 구조 완성. 전체 테스트 GREEN.

---

### [2026-05-25] 카카오 알림 전송 결과를 OrderResponse에 반영 (Task 12)

**1. 문제 정의**
`OrderService.createOrder()`가 카카오 알림 전송 성공 여부를 클라이언트에게 알려주지 않아, 알림 실패 시 사용자가 인지할 방법이 없었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `OrderServiceTest`에 `notificationSent()` 필드를 검증하는 테스트 3개 추가 — 필드 미존재로 컴파일 오류(Red) 확인
- **Step 2 [Green]**: `OrderResponse`에 `notificationSent: boolean` 필드 추가, `of(Order, boolean)` 팩토리 메서드 신규 작성. `sendKakaoMessageIfPossible()`의 반환 타입을 `void` → `boolean`으로 변경하여 `createOrder()`에서 결과를 캡처하고 `OrderResponse.of(saved, notificationSent)`로 응답 조립 → 전체 테스트 GREEN

**3. 결과 및 근거**
카카오 토큰 존재 + 전송 성공 시 `notificationSent=true`, 토큰 없거나 전송 실패 시 `false`로 응답. 알림 실패가 주문 실패로 전파되지 않는 정책은 그대로 유지. `OrderServiceTest` 8개 전부 GREEN, 전체 테스트 GREEN.

---

### [2026-05-25] updateMember 비밀번호 BCrypt 인코딩 누락 수정 (Task 13)

**1. 문제 정의**
`MemberService.updateMember()`가 비밀번호를 plain text로 `Member.update()`에 전달해 DB에 그대로 저장하고 있었다. `registerMember()`와 `adminCreate()`는 `encoder.encode()`를 적용하고 있어 인증 흐름에서만 비밀번호가 안전하게 저장되는 일관성 문제였다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `MemberServiceTest`에 `updateMember_encodesPasswordBeforeSaving` 테스트 추가 — `member.getPassword()`가 BCrypt 해시가 아니어서 실패(Red) 확인
- **Step 2 [Green]**: `MemberService.updateMember()`에서 `member.update(email, encoder.encode(password))` 로 변경 → 테스트 GREEN

**3. 결과 및 근거**
`updateMember` 호출 후 저장되는 비밀번호가 BCrypt 해시임을 테스트로 검증. `registerMember`, `adminCreate`, `updateMember` 세 경로 모두 동일하게 인코딩 적용. 전체 테스트 GREEN.

---

### [2026-05-25] 쓰기 메서드 @Transactional 누락 보완 (Task 14)

**1. 문제 정의**
`ProductService.deleteProduct()`(wish + product 두 쓰기), `OptionService.createOption/updateOption/deleteOption()`, `MemberService.registerMember/adminCreate()`(존재 여부 확인 + save)가 `@Transactional` 없이 실행되어, 중간 실패 시 DB 상태가 불일관해질 수 있었다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: 각 테스트 파일(`ProductServiceTest`, `OptionServiceTest`, `MemberServiceTest`)에 리플렉션으로 `@Transactional` 어노테이션 존재를 검사하는 테스트 8개 추가 → 어노테이션 미존재로 실패(Red) 확인
- **Step 2 [Green]**: `ProductService` 3개(`createProduct`, `updateProduct`, `deleteProduct`), `OptionService` 3개(`createOption`, `updateOption`, `deleteOption`), `MemberService` 2개(`registerMember`, `adminCreate`)에 `@Transactional` 추가 → 전체 테스트 GREEN

**3. 결과 및 근거**
쓰기 작업이 있는 서비스 메서드에 `@Transactional`을 일괄 보완. 기존에 선언된 `updateMember`, `chargePoint`, `findOrCreateKakaoMember`, `OrderService.createOrder`와 일관성 확보. 전체 테스트 GREEN.

---

### [2026-05-25] WishController @WebMvcTest 테스트 작성 (Task 15)

**1. 문제 정의**
`WishController`(GET/POST/DELETE `/api/wishes`)에 컨트롤러 단위 테스트가 없어, 인증 실패 시 401 반환 / 유효 요청 시 올바른 HTTP 상태 코드 반환 여부를 자동화된 방법으로 검증할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1**: `@WebMvcTest(WishController.class)`로 `WishControllerTest` 신규 작성 — `AuthenticationResolver`, `WishService`를 `@MockitoBean`으로 주입. 유효 토큰/무효 토큰 시나리오 7개 테스트 → 첫 실행부터 GREEN (기존 구현이 올바르게 동작함을 검증)

**3. 결과 및 근거**
GET(목록), POST(추가), DELETE(삭제) 세 엔드포인트 모두 정상 경로(200/201/204)와 인증 실패 경로(401), 입력 검증 실패(400) 총 7개 시나리오 커버. 전체 테스트 GREEN.

---

### [2026-05-25] OrderController, OptionController @WebMvcTest 테스트 작성 (Task 16)

**1. 문제 정의**
`OrderController`(GET/POST `/api/orders`)와 `OptionController`(GET/POST/PUT/DELETE `/api/products/{id}/options`)에 컨트롤러 단위 테스트가 없어, HTTP 상태 코드와 입력 검증 동작을 자동화된 방법으로 확인할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1**: `OrderControllerTest` 작성 — 인증 포함 엔드포인트, 유효/무효 토큰·입력 검증 실패 6개 시나리오 → GREEN
- **Step 2**: `OptionControllerTest` 작성 — 인증 없는 CRUD 엔드포인트, 정상 경로·404·400 총 8개 시나리오 → GREEN

**3. 결과 및 근거**
`OrderController` 6개, `OptionController` 8개 총 14개 테스트 추가. `WishController`(Task 15)와 함께 인증 필요 REST API 전체 컨트롤러 커버. 전체 테스트 GREEN.

---

### [2026-05-25] MemberController @WebMvcTest 테스트 작성 (Task 17)

**1. 문제 정의**
`MemberController`(GET `/me`, POST `/register`, POST `/login`)에 테스트가 없어 회원가입·로그인 API의 HTTP 상태 코드, 입력 검증 동작을 자동화된 방법으로 확인할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1**: `@WebMvcTest(MemberController.class)`로 `MemberControllerTest` 작성 — `AuthService`, `MemberService`, `AuthenticationResolver`를 `@MockitoBean`으로 주입. 7개 시나리오 → 첫 실행부터 GREEN

**3. 결과 및 근거**
`/me`(유효/무효 토큰), `/register`(정상/이메일 형식 오류/비밀번호 누락), `/login`(정상/미존재 이메일) 7개 시나리오 커버. Task 15~17로 REST API 컨트롤러 전체에 @WebMvcTest 적용 완료. 전체 테스트 GREEN.

---

### [2026-05-25] UnauthorizedException 도입 — 컨트롤러 null check 제거 (Task 18)

**1. 문제 정의**
`WishController`, `OrderController`, `MemberController` 세 곳에 `authenticationResolver.extractMember()` 호출 후 null 체크 → 401 반환 패턴이 동일하게 반복되었다. 예외 계층(`BusinessException`)과도 불일치했다.

**2. 상호작용 타임라인**
- **Step 1 [Red]**: `GlobalExceptionHandlerTest`에 `UnauthorizedException → 401` 테스트 추가 → 클래스 미존재로 컴파일 실패
- **Step 2 [Green]**: `UnauthorizedException extends BusinessException` 생성. `GlobalExceptionHandler`에 핸들러 추가(401). `AuthenticationResolver.extractMember()`가 null이거나 예외 발생 시 `UnauthorizedException` throw로 변경. 컨트롤러 3개에서 null check 4줄씩 제거. 컨트롤러 테스트에서 `willReturn(null)` → `willThrow(UnauthorizedException)` 업데이트.

**3. 결과 및 근거**
컨트롤러 3개에서 null check 분기 완전 제거. 인증 실패는 예외 계층(`NotFoundException`, `DuplicateException`과 동일 구조)으로 처리되어 일관성 확보. 전체 테스트 GREEN.

---

### [2026-05-25] CategoryController, ProductController @WebMvcTest 테스트 작성 (Task 19)

**1. 문제 정의**
`CategoryController`(GET/POST/PUT/DELETE `/api/categories`)와 `ProductController`(GET/POST/PUT/DELETE `/api/products`)에 컨트롤러 단위 테스트가 없어, HTTP 상태 코드와 입력 검증 동작을 자동화된 방법으로 확인할 수 없었다.

**2. 상호작용 타임라인**
- **Step 1**: `CategoryControllerTest` 작성 — CRUD 7개 시나리오(정상/404/400) → GREEN
- **Step 2**: `ProductControllerTest` 작성 — CRUD 9개 시나리오(정상/404/400) → GREEN

**3. 결과 및 근거**
`CategoryController` 7개, `ProductController` 9개 총 16개 테스트 추가. Task 15~19로 프로젝트 내 모든 REST API 컨트롤러와 어드민 컨트롤러에 @WebMvcTest 완비. 전체 테스트 GREEN.
