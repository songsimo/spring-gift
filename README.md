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