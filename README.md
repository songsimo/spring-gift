# spring-gift

Spring Boot 기반 선물 쇼핑 API.

## 기술 스택

| 항목 | 내용 |
|------|------|
| Framework | Spring Boot 3.5.9 |
| Language | Java 21 |
| DB | MySQL (운영) / H2 (로컬) |
| Migration | Flyway |
| Auth | JWT, Kakao OAuth2 |

## 실행 방법

```bash
./gradlew bootRun
```

환경 변수 필요:
- `JWT_SECRET` — JWT 서명 키 (256비트 이상)
- `KAKAO_CLIENT_ID` — 카카오 앱 키
- `KAKAO_CLIENT_SECRET` — 카카오 시크릿
- `KAKAO_REDIRECT_URI` — 카카오 콜백 URI

## 기능 목록

### 회원
- [x] 이메일/비밀번호 회원가입
- [x] 이메일/비밀번호 로그인 (JWT 발급)
- [x] 카카오 OAuth2 로그인

### 상품
- [x] 상품 목록 조회 (페이징)
- [x] 상품 단건 조회
- [x] 상품 등록
- [x] 상품 수정
- [x] 상품 삭제

### 카테고리
- [x] 카테고리 목록 조회
- [x] 카테고리 등록
- [x] 카테고리 수정
- [x] 카테고리 삭제

### 옵션
- [x] 상품별 옵션 목록 조회
- [x] 옵션 등록
- [x] 옵션 삭제 (상품당 최소 1개 유지)

### 위시리스트
- [x] 위시리스트 조회 (페이징)
- [x] 위시 추가
- [x] 위시 삭제

### 주문
- [x] 주문 목록 조회 (페이징)
- [x] 주문 생성 (재고 차감, 포인트 결제, 카카오 알림)

---

## AI 활용 방식

이 프로젝트는 Claude Code(Anthropic Claude Sonnet)를 페어 프로그래머로 활용해 개발했다.

### 역할 분담

| 역할 | 담당 |
|------|------|
| 방향 결정, 트레이드오프 판단 | 사람 |
| 코드 구현, 테스트 작성, 리팩토링 | Claude |
| 설계 문제 발견, 대안 제시 | Claude |
| 최종 채택 결정 | 사람 |

### 협업 워크플로

```
Plan Mode (설계 분석·대안 비교)
    ↓ 사람이 방향 결정
ADR 작성 (docs/adr/)
    ↓
TDD — Red → Green → Refactor
    ↓
단일 커밋 (구조 변경 / 동작 변경 분리)
```

1. **Plan Mode 진입**: 구현 전 설계 분석과 대안을 먼저 검토한다.
2. **ADR 기록**: 설계 결정과 트레이드오프를 `docs/adr/`에 남긴다.
3. **TDD 구현**: 실패 테스트 → 통과 → 리팩토링 순서로 진행한다.
4. **커밋 규칙**: 구조 변경(`refactor`)과 동작 변경(`feat`/`fix`)을 반드시 분리한다.

### 구체적 활용 사례

#### 1. 서비스 레이어 추출 (리팩토링)
Controller에 집중된 비즈니스 로직을 Service로 이전했다. 기능별로 한 번에 하나씩, 각각 독립된 커밋으로 분리해 진행했다(`delegate getProducts`, `delegate createProduct` 등 순차 커밋).

#### 2. 예외 계층 설계
Controller마다 중복된 `@ExceptionHandler`를 `GlobalExceptionHandler`로 통합하고, `NotFoundException` / `BadRequestException` / `DuplicateException` / `UnauthorizedException` / `ConflictException` 계층을 도입했다. 각 예외가 HTTP 상태 코드에 1:1 대응되도록 설계 결정은 Plan Mode에서 수행했다.

#### 3. 포인트 Race Condition 발견 및 수정 (ADR-007)
비관적 락을 구현한 뒤 JUnit 테스트가 통과했음에도, k6 부하 테스트에서 예산의 2.7배(538건) 주문이 커밋되는 버그가 발견됐다. Claude가 OSIV + Hibernate L1 캐시 오염을 근본 원인으로 분석하고, `SELECT FOR UPDATE` 대신 원자적 `UPDATE ... WHERE point >= amount` 쿼리로 교체했다. 수정 후 성공 주문 수가 이론치(200건)와 정확히 일치했으며 p95 응답 시간도 118ms → 42ms로 개선됐다.

#### 4. OSIV 비활성화 (ADR-008)
Race Condition 수정 후 OSIV 자체 문제를 별도 작업으로 분리해 처리했다. 전체 코드베이스에서 OSIV에 의존하는 LAZY 로딩 지점 9곳을 분석한 뒤, `@Transactional` 경계 명시와 fetch join 도입으로 `open-in-view=false`를 안전하게 적용했다.

#### 5. @AuthMember 어노테이션 도입 (ADR-004)
Controller마다 `@RequestHeader("Authorization")`와 null 체크가 중복되는 문제를 `HandlerMethodArgumentResolver` + `@AuthMember` 패턴으로 해결했다. Spring의 `@AuthenticationPrincipal`과 동일한 관례를 따른다.

#### 6. 카카오 알림 NotificationPort 추상화 (ADR-001)
`OrderService`가 `KakaoMessageClient`에 직접 의존하는 구조를 `NotificationPort` 인터페이스로 분리했다. 동기 → 비동기 → Outbox 패턴 전환 시 `OrderService` 수정 없이 어댑터만 교체할 수 있다.

### 얻은 것

- **설계 근거 보존**: 어떤 대안을 검토했고 왜 현재 선택을 했는지 ADR에 남아 있어, 나중에 코드만 봐서는 알 수 없는 맥락을 유지한다.
- **안전한 리팩토링**: 구조 변경과 동작 변경을 커밋 단위로 분리해, 어떤 변경이 테스트 실패를 유발했는지 즉시 추적 가능하다.
- **실제 부하 테스트**: JUnit 통과만으로는 발견하지 못했던 Race Condition을 k6 부하 테스트로 재현하고 수정했다.
