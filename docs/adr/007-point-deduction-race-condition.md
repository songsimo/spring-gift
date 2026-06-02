# ADR-007: 포인트 차감 Race Condition — 원자적 UPDATE 채택

## Status
Accepted

## Context

### 발견된 버그

비관적 락(`SELECT FOR UPDATE`)을 구현한 뒤 JUnit 동시성 테스트(10 스레드)는 통과했으나,
MySQL 환경에서 k6 부하 테스트를 실행한 결과 Race Condition이 남아 있음이 확인됐다.

**k6 부하 테스트 결과 (수정 전)**

| 지표 | 값 |
|------|-----|
| 성공 주문 수 | 538건 |
| 이론적 최대 주문 수 | 200건 (5,000,000 ÷ 25,000) |
| 총 차감 포인트 | 13,450,000원 |
| user1 시작 포인트 | 5,000,000원 |
| p95 응답 시간 | 118ms |

→ 포인트 예산의 2.7배에 해당하는 주문이 커밋됨.

### 근본 원인 — OSIV + Hibernate L1 캐시

Spring Boot는 `spring.jpa.open-in-view=true`가 기본값이다.
OSIV는 HTTP 요청 전체 생명주기에 걸쳐 EntityManager(이하 EM)를 유지한다.

```
HTTP 요청 흐름
──────────────────────────────────────────────────────────
[요청 시작] OpenEntityManagerInViewInterceptor → EM 열고 스레드 바인딩

AuthMemberResolver.resolveArgument()
  └─ memberRepository.findByEmail(email)
       → SELECT * FROM member WHERE email=?
       → member 엔티티 EM L1 캐시 적재  (point = 5,000,000)

OrderService.create() @Transactional
  └─ 같은 EM 재사용 (OSIV)
       └─ memberRepository.findByIdForUpdate(id)
            → SQL: SELECT * FROM member WHERE id=? FOR UPDATE  ← 락 획득 ✓
            → Hibernate L1 캐시 확인: 이미 있음
            → DB 신선 값 무시, 캐시 값 반환  (point = 5,000,000, 스탈)
       └─ lockedMember.deductPoint(25_000)
            → 스탈 값 기준 검증: 5,000,000 >= 25,000 통과
            → UPDATE member SET point = 4,975,000  ← 잘못된 값
```

**JUnit 테스트가 통과한 이유**: 테스트는 HTTP 스택을 거치지 않으므로 OSIV가 적용되지 않는다.
각 스레드가 자체 `memberRepository.findById()`로 member를 로드하여 L1 캐시 오염이 없었다.

**스탈 쓰기 누적 메커니즘**

모든 VU가 자신의 요청 시작 시점에 캐시한 값(5,000,000)을 기준으로 4,975,000을 쓴다.
포인트가 0에 가까워질수록 새 요청들이 최신 DB 값(≈0)을 캐시하기 시작하면서
차감이 올바르게 작동하기 시작한다.
결국 200번의 실질적 차감으로 point=0에 도달하지만,
그 사이에 스탈 쓰기가 끼어들어 성공 주문이 200건 이상(538건)이 된다.

## Decision

`SELECT FOR UPDATE + 메모리 차감` 패턴을 **원자적 UPDATE 쿼리**로 교체한다.

```java
// MemberRepository
@Modifying
@Query("UPDATE Member m SET m.point = m.point - :amount " +
       "WHERE m.id = :id AND m.point >= :amount")
int deductPointAtomic(@Param("id") Long id, @Param("amount") int amount);

// OrderService.create()
if (memberRepository.deductPointAtomic(member.getId(), price) == 0) {
    throw new BadRequestException("포인트가 부족합니다.");
}
```

DB가 조건 체크(point >= amount)와 차감을 단일 문장에서 원자적으로 수행하므로
L1 캐시를 전혀 거치지 않는다. 반환값이 0이면 포인트가 부족한 것이다.

## Alternatives

### A. OSIV 비활성화 (`spring.jpa.open-in-view=false`)

비관적 락의 근본 원인을 제거하는 아키텍처 수준의 수정이다.
OSIV가 없으면 `@Transactional` 경계에서 EM이 새로 생성되어 L1 캐시가 비어 있고,
`findByIdForUpdate`가 DB에서 신선한 값을 읽어 락이 의도대로 동작한다.

**채택하지 않은 이유**: 여러 곳에서 lazy loading 위반이 존재한다.

- `OrderService.notifyOrder()` — `@Transactional` 없이 `order.getOption().getProduct()` lazy load
- `OrderResponse.from()` — `order.getOption().getId()` lazy load
- `ProductResponse.from()` — `category` lazy load

이들을 함께 수정해야 하므로 "하나에 한 개의 변경" 원칙에 어긋난다.
OSIV 비활성화는 별도 작업으로 분리한다.

### B. `entityManager.refresh()` 강제 재로드

락 획득 후 `entityManager.refresh(lockedMember, LockModeType.PESSIMISTIC_WRITE)`를 호출하면
L1 캐시를 무시하고 DB에서 신선한 값을 강제로 읽어 온다.

**채택하지 않은 이유**: OrderService에 EntityManager를 직접 주입해야 하고,
락+조회+refresh의 3단계가 남아 코드가 복잡해진다.
원자적 UPDATE가 더 단순하고 OSIV 상태에 완전히 무관하다.

## Trade-offs

| 항목 | 원자적 UPDATE (채택) | 비관적 락 + refresh |
|------|---------------------|---------------------|
| 구현 복잡도 | 낮음 (쿼리 1개) | 중간 (EM 주입 + refresh) |
| OSIV 의존성 | 없음 | 없음 (refresh 후) |
| 코드 가독성 | 높음 | 중간 |
| 트랜잭션 롤백 시 동작 | 자동 롤백 | 자동 롤백 |
| 락 범위 | 없음 (UPDATE 내부 잠금만) | 명시적 행 락 |
| 업계 사용 여부 | 은행·핀테크 표준 패턴 | JPA 직접 사용 시 일반적 |

원자적 UPDATE는 option의 `SELECT FOR UPDATE`가 이미 동일 옵션 주문을 직렬화하고 있으므로
member 행 락이 별도로 필요하지 않다. 다른 옵션을 동시에 주문하는 경우에도
atomic UPDATE가 DB 레벨에서 올바르게 처리한다.

## 부하 테스트 결과 비교

**환경**: Docker MySQL 8.0.46, Hikari pool 10 connections, k6 v2.0.0  
**시나리오**: warmup(10 VU, 15s) + spike(50 req/s, 35s)  
**조건**: user1 포인트 5,000,000, option9 수량 1,000, 상품 단가 25,000

| 지표 | 수정 전 (비관적 락) | 수정 후 (원자적 UPDATE) |
|------|-------------------|------------------------|
| 성공 주문 수 | **538건** | **200건** ✓ |
| 이론적 최대 | 200건 | 200건 |
| 총 차감 포인트 | 13,450,000 | **5,000,000** ✓ |
| 재고 차감 수 | 538 | **200** ✓ |
| p95 응답 시간 | 118ms | **42ms** |

수정 후 성공 주문 수가 이론적 최대(5,000,000 ÷ 25,000 = 200)와 정확히 일치하며,
총 차감 포인트도 초기 잔액과 동일하다.
