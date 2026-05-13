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