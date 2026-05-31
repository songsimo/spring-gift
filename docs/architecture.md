# Architecture

## 패키지 구조

기능(feature) 단위로 패키지를 구성한다. 각 패키지에 Entity, Repository, Request/Response, Controller, Service가 함께 위치한다.

```
gift/
├── member/      — 회원 가입·로그인·관리
├── product/     — 상품 CRUD, 이름 유효성 검사
├── category/    — 카테고리 CRUD
├── option/      — 상품 옵션 CRUD, 재고 관리
├── order/       — 주문 생성·조회, 카카오 메시지
├── wish/        — 위시리스트 CRUD
├── auth/        — JWT 발급, 카카오 OAuth2
└── exception/   — 커스텀 예외 계층, GlobalExceptionHandler
```

## 계층 구조와 의존 방향

```
Controller → Service → Repository
                ↓
           (domain logic: Entity methods)
```

- **Controller**: HTTP 처리, 인증, Service 위임만 담당. 비즈니스 로직 없음.
- **Service**: 비즈니스 로직 집중. 트랜잭션 경계. Repository 조합.
- **Repository**: JPA 데이터 접근. 커스텀 쿼리 메서드.
- **Entity**: 핵심 도메인 규칙 (예: `Option.subtractQuantity()`, `Member.deductPoint()`).

## API 구분

### REST API (`/api/**`)
JSON 요청/응답. JWT 인증.

| prefix | 설명 |
|--------|------|
| `/api/members` | 회원 가입·로그인 |
| `/api/auth/kakao` | 카카오 OAuth2 |
| `/api/products` | 상품 |
| `/api/categories` | 카테고리 |
| `/api/products/{id}/options` | 상품 옵션 |
| `/api/wishes` | 위시리스트 |
| `/api/orders` | 주문 |

### Admin MVC (`/admin/**`)
Thymeleaf HTML 폼. 인증 없음 (내부망 전용 가정).

| prefix | 설명 |
|--------|------|
| `/admin/members` | 회원 목록·수정·포인트 충전 |
| `/admin/products` | 상품 목록·등록·수정 |

## 현재 상태 (리팩토링 진행 중)

- **Service 레이어 없음**: 비즈니스 로직이 Controller에 집중되어 있음.
- **예외 처리 불일치**: Controller마다 `@ExceptionHandler` 중복, HTTP 상태 코드 혼재.
- **리팩토링 방향**: 구조 변경(서비스 추출) → 동작 변경(예외 계층, 버그 수정) 순서로 진행.
