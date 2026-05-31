# ADR-001: NotificationPort 추상화 도입

## Status
Proposed

## Context
OrderService가 KakaoMessageClient에 직접 의존한다.
알림 방식(동기→비동기→Outbox)이 바뀔 때마다 OrderService를 수정해야 한다.
소규모 기준으로 동기 best-effort면 충분하지만, 중규모 이상에서 비동기 전환이 필요하다.

## Decision
NotificationPort 인터페이스를 도입하고 KakaoNotificationAdapter가 구현한다.
OrderService는 NotificationPort에만 의존한다.

```
OrderService → NotificationPort (interface)
                    ↑
              KakaoNotificationAdapter  ← 현재 (동기, best-effort)
              AsyncNotificationAdapter  ← 중규모 시 추가
```

## Alternatives
- ApplicationEvent 방식: OrderService가 알림 자체를 모름. 완전한 분리지만
  notificationSent 필드를 응답에 담기 어려움 (비동기 시).
- KakaoMessageClient 직접 호출 유지: 단순하지만 알림 방식 변경 시 OrderService 수정 필요.

## Trade-offs
인터페이스 파일 1개 추가 ↔ OrderService 불변, 알림 구현 교체 자유로움.
중규모 전환 시 KakaoNotificationAdapter에 @Async + @Retryable 추가만으로 비동기 전환 가능.
