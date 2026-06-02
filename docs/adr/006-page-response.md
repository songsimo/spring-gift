# ADR-006: Page<T> 직접 응답 방식 유지

## Status
Accepted

## Context

OrderController, WishController, ProductController가 `ResponseEntity<Page<XxxResponse>>`를
반환하고 있다. `Page<T>`는 `org.springframework.data.domain.Page` — Spring Data의 인프라 타입으로,
API 응답 스펙이 Spring Data 내부 표현과 결합된다는 구조적 문제가 있다.

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

대안으로 검토한 방식:
- **커스텀 PageResponse<T> DTO**: 필요한 필드만 담은 래퍼 레코드
- **응답 헤더 방식**: `X-Total-Count`, `X-Total-Pages` 헤더 + 순수 배열 body
- **Slice<T>**: 전체 카운트 없이 다음 페이지 여부만 제공 (무한 스크롤)

## Decision

**현재 `Page<T>` 직접 반환 방식을 유지한다.**

## Rationale

- 내부 API이며, 현재 클라이언트가 응답 구조에 의존하고 있지 않다
- 내부 API / 단순 CRUD 규모에서 `Page<T>` 직접 반환은 관행으로 통용된다
  (Spring Boot 공식 가이드, Spring PetClinic 동일 방식 사용)
- 커스텀 래퍼 도입 비용(`PageResponse<T>` + 전 계층 map 적용) 대비 지금 당장 얻는 이익이 없다

## Trade-offs

- **단점**: Spring Data 버전 업그레이드 시 JSON 응답 구조가 변경될 수 있다
  (2.x → 3.x에서 `page` 필드명이 `number`로 바뀐 사례)
- **단점**: OpenAPI 문서 자동 생성 시 `Page<T>` 스펙이 올바르게 표현되지 않아
  별도 설정이 필요하다
- **단점**: `pageable.paged`, `pageable.unpaged` 등 불필요한 필드가 노출된다

## 전환 시점

다음 상황이 발생하면 커스텀 `PageResponse<T>` DTO로 전환한다.

- 외부에 API를 공개해야 할 때
- OpenAPI 문서를 자동 생성해서 외부와 공유해야 할 때
- Spring Data 업그레이드로 응답 구조가 바뀌어 클라이언트가 깨지는 상황이 발생했을 때

전환 시 변경 범위: `PageResponse<T>` 레코드 1개 신설 +
각 서비스 반환 타입 `Page<T>` → `PageResponse<T>` + `.map(PageResponse::from)` 추가.
