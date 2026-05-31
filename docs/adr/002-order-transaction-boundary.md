# ADR-002: 주문 생성 트랜잭션 경계 설계

## Status
Proposed

## Context
현재 OrderController에 @Transactional이 없어 재고 차감(save) 후
포인트 차감 실패 시 부분 커밋이 발생한다.
카카오 알림은 외부 HTTP 호출이므로 트랜잭션 안에 넣으면 커넥션 점유 문제가 생긴다.

## Decision
OrderService.create()에 @Transactional 적용 (재고 차감, 포인트 차감, 주문 저장, 위시 삭제).
OrderService.notify()를 별도 메서드로 분리 (@Transactional 없음).
Controller에서 create() 커밋 후 notify() 호출.

순서:
1. option 조회 → NotFoundException
2. 재고 차감 → InsufficientStockException  ┐
3. member 조회 → NotFoundException          │ @Transactional
4. 포인트 차감 → InsufficientPointException │
5. 주문 저장                                │
6. 위시 삭제                               ┘
7. 카카오 알림 (트랜잭션 밖, NotificationPort)

## Alternatives
- A방법 (Controller 순차 호출): 단순하지만 Controller가 OrderService + NotificationPort 둘 다 알아야 함.
- C방법 (@TransactionalEventListener): 완전 분리지만 소규모에서 과함, notificationSent 처리 복잡.

## Trade-offs
B방법은 OrderService가 notify()도 가지므로 알림 관심사가 완전히 분리되지 않음.
대신 Controller는 OrderService만 알면 되고, 추후 notify()에 @Async 추가로 비동기 전환 가능.
