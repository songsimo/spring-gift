# ADR-006: Page<T> / Pageable Spring Data 타입 허용

## Status
Accepted

## Context

OrderController, WishController, ProductController가 `ResponseEntity<Page<XxxResponse>>`를
반환하고, Controller → Service → Repository 세 계층이 `Pageable`, `Page<T>`를 그대로 사용한다.

두 타입 모두 `org.springframework.data.domain` — Spring Data의 인프라 타입으로,
Service 계층까지 Spring Data 타입이 관통한다는 구조적 의존성 문제가 있다.

실제 JSON 응답에 불필요한 Spring Data 내부 메타데이터가 포함된다.

```json
{
  "content": [...],
  "pageable": {
    "paged": true,      ← 클라이언트 불필요
    "unpaged": false,   ← 클라이언트 불필요
    "offset": 0         ← 내부 구현 세부사항
  },
  "numberOfElements": 5,  ← content.length와 동일
  "empty": false          ← totalElements == 0과 동일
}
```

### Page<T> 응답 문제

실제 JSON 응답에 불필요한 Spring Data 내부 메타데이터가 포함된다.

```json
{
  "content": [...],
  "pageable": {
    "paged": true,      ← 클라이언트 불필요
    "unpaged": false,   ← 클라이언트 불필요
    "offset": 0         ← 내부 구현 세부사항
  },
  "numberOfElements": 5,  ← content.length와 동일
  "empty": false          ← totalElements == 0과 동일
}
```

`Page<T>` 응답 대안으로 검토한 방식:
- **커스텀 PageResponse<T> DTO**: 필요한 필드만 담은 래퍼 레코드
- **응답 헤더 방식**: `X-Total-Count`, `X-Total-Pages` 헤더 + 순수 배열 body
- **Slice<T>**: 전체 카운트 없이 다음 페이지 여부만 제공 (무한 스크롤)

### Pageable 파라미터 문제

`Pageable`은 입력 타입이라 클라이언트에 Spring Data 구조가 노출되지 않는다.
클라이언트는 `?page=0&size=20&sort=id,desc`만 인지한다.

그러나 `Pageable`만 분리해도 Spring Data 의존은 제거되지 않는다.

```
Service → Pageable          ← Pageable 제거 시 사라짐
Service → Page<XxxResponse> ← 여전히 Spring Data 타입
Repository → JpaRepository  ← 여전히 Spring Data 타입
```

**진짜 분리**(Repository Port 패턴)는 커스텀 포트 인터페이스 + JPA 어댑터 계층 전체가 필요하다.

```java
// 완전한 분리 시 필요한 구조
interface OrderQueryPort {
    PageResult<Order> findByMemberId(Long memberId, PageRequest req);
}
record PageRequest(int page, int size) { }
record PageResult<T>(List<T> content, long totalElements, int totalPages) { }

class OrderJpaAdapter implements OrderQueryPort { ... } // Spring Data를 여기서만 사용
```

## Decision

**`Pageable`과 `Page<T>`를 Controller → Service → Repository 전 계층에서 그대로 사용한다.**

이것은 "적절하기 때문"이 아니라, **Spring Data JPA를 인프라로 선택했을 때 수반되는
의존성 비용을 수용하기로 결정했기 때문**이다.

## Rationale

- 이 프로젝트는 Spring Data JPA(`JpaRepository`)를 인프라로 확정했다.
  Repository가 이미 `JpaRepository`에 의존하는 이상, `Pageable`만 분리해도
  Spring Data 의존이 제거되지 않는다. 불완전한 분리는 비용 대비 효과가 없다.
- 완전한 분리(Repository Port 패턴)는 현재 규모 대비 아키텍처 투자가 크다.
- `Page<T>` 응답은 내부 API 기준 관행으로 통용된다
  (Spring Boot 공식 가이드, Spring PetClinic 동일 방식 사용).
- Spring Data를 교체할 계획이 없다.

## Trade-offs

**Page<T> 응답:**
- Spring Data 버전 업그레이드 시 JSON 구조가 바뀔 수 있다
  (2.x → 3.x에서 `page` 필드명이 `number`로 바뀐 사례)
- OpenAPI 문서 자동 생성 시 `Page<T>` 스펙이 올바르게 표현되지 않아 별도 설정 필요
- `pageable.paged`, `pageable.unpaged` 등 불필요한 필드가 클라이언트에 노출됨

**Pageable 의존:**
- Service 계층이 인프라 타입(`org.springframework.data.domain.Pageable`)에 결합됨
- Spring Data 교체 시 Service 메서드 시그니처까지 변경 필요

## 전환 시점

다음 상황이 발생하면 Repository Port 패턴 + 커스텀 `PageResponse<T>`로 전환한다.

- Spring Data JPA를 다른 ORM 또는 데이터 접근 기술로 교체할 때
- 외부에 API를 공개하거나 OpenAPI 문서를 공유해야 할 때
- Spring Data 업그레이드로 응답 구조가 바뀌어 클라이언트가 깨지는 상황이 발생했을 때
