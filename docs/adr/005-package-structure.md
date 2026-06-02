# ADR-005: 패키지 구조 — Package by Feature + 선택적 서브패키지

## Status
Accepted

## Context

현재 프로젝트는 도메인별 flat 패키지 구조(Package by Feature)를 사용한다.
~60개 파일, 8개 도메인 패키지, 소규모 팀.

아키텍처 논의 과정에서 세 가지 옵션을 검토했다.

- **Option A: Package by Layer** (`controller/`, `service/`, `repository/` 최상위 분리)
- **Option B: 현재 flat domain 유지** (아무 변경 없음)
- **Option C: Package by Feature + 선택적 서브패키지** (외부 어댑터가 있는 도메인만 분리)

## Decision

**Option C 채택**: 도메인별 패키지를 유지하되, 외부 시스템 어댑터가 혼재하는
`gift.order`와 `gift.auth` 두 패키지에만 서브패키지를 도입한다.

```
gift.order/
  └── infrastructure/
      ├── KakaoNotificationAdapter.java
      └── KakaoMessageClient.java

gift.auth/
  └── web/
      ├── KakaoAuthController.java
      └── WebMvcConfig.java
```

나머지 도메인(category, wish, member, option, product, exception)은 flat 유지.

## Alternatives

**Option A (Package by Layer) 불채택 이유:**
- 하나의 기능 수정 시 5개 디렉터리를 횡단해야 함
- 도메인 간 경계가 패키지 구조에 드러나지 않음
- 향후 모듈 분리가 어려워짐

**Option B (완전 flat 유지) 불채택 이유:**
- `gift.order`에서 `Order.java`(도메인 엔티티)와 `KakaoMessageClient.java`(HTTP 클라이언트)가
  같은 레벨에 놓여 역할 혼동을 유발함
- `gift.auth`에서 웹 진입점(Controller)과 MVC 설정(WebMvcConfig)이
  JwtProvider, AuthMemberResolver 등과 뒤섞임

## Trade-offs

- **장점**: 외부 어댑터 교체 시 `infrastructure/` 내부만 수정하면 됨.
  도메인 코드(`Order`, `NotificationPort`)는 변경 불필요.
  나중에 `gift.order`를 독립 서비스로 분리할 때 경계가 명확함.
- **단점**: 도메인마다 서브패키지 기준이 다름(일관성이 완전하지 않음).
  파일 수가 적은 도메인에서도 같은 기준 적용 시 혼용 위험.

## 서브패키지 도입 기준 (향후 참고)

- `infrastructure/`: 외부 HTTP 클라이언트, 메시지 어댑터 등 외부 시스템 연동 파일이 2개 이상
- `web/`: 컨트롤러가 2개 이상이거나 WebMvcConfig 등 MVC 설정이 함께 있을 때
- 파일 수가 7개 이하이고 외부 어댑터가 없는 도메인: flat 유지

## 관련 자료

- Martin Fowler, "Presentation Domain Data Layering" (martinfowler.com)
- Alistair Cockburn, "Hexagonal Architecture" (alistair.cockburn.us)
- Tom Hombergs, *Get Your Hands Dirty on Clean Architecture* (Packt, 2019)
- GitHub: `spring-projects/spring-petclinic` — 유사 규모의 flat domain 구조 레퍼런스
