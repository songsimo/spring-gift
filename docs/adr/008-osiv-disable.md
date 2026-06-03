# ADR-008: OSIV 비활성화 — @Transactional 경계 명시 및 fetch join 도입

## Status
Accepted

## Context

### 배경 — ADR-007에서 분리된 작업

ADR-007에서 포인트 Race Condition의 근본 원인이 `spring.jpa.open-in-view=true`(OSIV)로 인한
Hibernate L1 캐시 오염임을 확인했다. 당시 원자적 UPDATE로 Race Condition은 수정했으나,
OSIV 자체는 "별도 작업으로 분리" 결정에 따라 비활성화하지 않았다.

### OSIV가 초래하는 문제

OSIV는 HTTP 요청 전체 생명주기에 EntityManager를 유지한다.
이 상태에서 Service 메서드 밖(Controller 반환 직전 직렬화 시점, 혹은 Argument Resolver)에서도
LAZY 연관을 로드할 수 있다. 이는 다음 문제를 낳는다:

1. **DB 커넥션 점유 연장**: View 렌더링·직렬화 중에도 커넥션이 살아 있다.
2. **트랜잭션 경계 모호**: 어디서든 쿼리가 발생할 수 있어 N+1 감지와 성능 예측이 어렵다.
3. **L1 캐시 오염 위험**: ADR-007이 재현한 것처럼 요청 초반에 적재한 스탈 엔티티가 Service까지 흘러든다.

### 현재 코드에서 OSIV에 의존하는 LAZY 로딩 지점

| 위치 | LAZY 접근 | OSIV 없으면 |
|------|-----------|-------------|
| `WishService.getWishes()` → `WishResponse.from()` | `wish.getProduct().*` | LazyInitializationException |
| `WishService.addWish()` → `WishResponse.from()` | `wish.getProduct().*` | LazyInitializationException |
| `ProductService.getAll()` → `ProductResponse.from()` | `product.getCategory().getId()` | LazyInitializationException |
| `ProductService.getById()` → `ProductResponse.from()` | `product.getCategory().getId()` | LazyInitializationException |
| `ProductService.create()` → `ProductResponse.from()` | `product.getCategory().getId()` | LazyInitializationException |
| `ProductService.update()` → `ProductResponse.from()` | `product.getCategory().getId()` | LazyInitializationException |
| `OptionService.delete()` | `option.getProduct().getId()` | LazyInitializationException |
| `OrderService.getOrders()` → `OrderResponse.from()` | `order.getOption().getId()` | LazyInitializationException |
| `OrderService.notifyOrder()` | `order.getOption().getProduct()` | LazyInitializationException |

`notifyOrder()`는 ADR-002에 따라 외부 HTTP 호출(카카오 알림) 중 DB 커넥션을
점유하지 않기 위해 `@Transactional`이 의도적으로 없다.

## Decision

`spring.jpa.open-in-view=false`로 OSIV를 비활성화하되,
LAZY 로딩이 필요한 모든 Service 메서드에 `@Transactional` 경계를 명시한다.
`notifyOrder()`는 `@Transactional` 없이 fetch join으로 LAZY 로딩을 해결한다.

### 구조 변경 (refactor 커밋)

**`@Transactional` 추가**

| 파일 | 메서드 | 어노테이션 |
|------|--------|-----------|
| `WishService` | `getWishes()` | `@Transactional(readOnly=true)` |
| `WishService` | `addWish()`, `removeWish()` | `@Transactional` |
| `ProductService` | `getAll()`, `getById()` | `@Transactional(readOnly=true)` |
| `ProductService` | `create()`, `delete()`, `update()` | `@Transactional` |
| `OptionService` | `getOptions()` | `@Transactional(readOnly=true)` |
| `OptionService` | `create()`, `delete()` | `@Transactional` |
| `OrderService` | `getOrders()` | `@Transactional(readOnly=true)` |

**`notifyOrder()` — fetch join으로 해결**

```java
// OrderRepository
@Query("SELECT o FROM Order o JOIN FETCH o.option opt JOIN FETCH opt.product WHERE o.id = :id")
Optional<Order> findByIdWithDetails(@Param("id") Long id);

// OrderService.notifyOrder() — @Transactional 없음 (ADR-002 유지)
public boolean notifyOrder(Member member, Long orderId) {
    var order = orderRepository.findByIdWithDetails(orderId)
        .orElseThrow(() -> new NotFoundException("Order not found."));
    Product product = order.getOption().getProduct();  // 이미 로드됨
    return notificationPort.notify(member, order, product);
}
```

### 동작 변경 (feat 커밋)

```properties
# application.properties
spring.jpa.open-in-view=false
```

## Alternatives

### A. OSIV 유지 + 현상 방치

Race Condition은 원자적 UPDATE로 수정됐으므로 즉각적 장애는 없다.
그러나 트랜잭션 경계가 암묵적으로 유지되어 쿼리 발생 지점 예측이 계속 어렵다.

**채택하지 않은 이유**: OSIV는 Spring Boot 공식 문서에서도 안티패턴으로 분류한다.
커넥션 점유 연장과 L1 캐시 오염 위험을 그대로 남기는 것은 수용하기 어렵다.

### B. LAZY → EAGER 페치 전환

`@ManyToOne(fetch = EAGER)`로 모두 바꾸면 OSIV 없이도 로딩된다.

**채택하지 않은 이유**: 항상 연관 엔티티를 로드하므로 불필요한 JOIN이 발생한다.
연관이 깊어질수록 N+1이 EAGER N+1로 바뀔 뿐이다.

## Trade-offs

| 항목 | 이번 결정 (OSIV off + @Transactional) | OSIV 유지 |
|------|--------------------------------------|-----------|
| 트랜잭션 경계 | 명시적, 코드에서 바로 파악 가능 | 암묵적, HTTP 요청 단위 |
| 커넥션 점유 | `@Transactional` 범위만 점유 | View 렌더링·직렬화까지 점유 |
| L1 캐시 오염 위험 | 없음 (EM이 요청마다 새로 생성) | 남아 있음 |
| N+1 감지 용이성 | 높음 (쿼리가 예측 가능한 곳에서만 발생) | 낮음 |
| 코드 변경 비용 | LAZY 접근마다 @Transactional 명시 필요 | 없음 |
| ADR-002 준수 | fetch join으로 양립 가능 | 그대로 양립 가능 |
