# spring-gift

Spring Boot 3.5.9, Java 21 선물 API.
카카오 OAuth2 로그인, JWT 인증, 위시리스트, 포인트 결제, 카카오톡 알림.
DB: MySQL(운영) / H2(로컬)

## Commands
- `./gradlew build`       — 전체 빌드 + 테스트
- `./gradlew test`        — 테스트만
- `./gradlew compileJava` — 컴파일 검증 (빠름)
- `./gradlew bootRun`     — 로컬 서버 기동

## 패키지
`gift.{member, product, category, option, order, wish, auth, exception}`
계층: Controller → Service → Repository

## 절대 금지
- `orElse(null)` → `orElseThrow(NotFoundException::new)` 사용
- `ResponseEntity<?>` → 반환 타입 명시
- `@Autowired` → SB3 생성자 주입 자동

## 작업 방식
- Plan Mode에서 계획 수립 → 사용자 승인 → 구현. 방향 결정은 사용자, 구현은 Claude.
- 구현 전 README 기능 목록 작성 → 기능 단위로 구현 → 자동 커밋 → README 체크.
- 구조 변경과 동작 변경은 반드시 분리한다. 같은 커밋에 섞지 않는다.
  - 구조 변경: 외부 동작 유지, 기존 테스트 통과로 검증 (`refactor` 커밋)
  - 동작 변경: TDD로 실패 테스트 먼저 작성 후 구현 (`feat`/`fix` 커밋)
- 커밋 메시지: AngularJS Git Commit Message Conventions — `type(scope): subject`
  - type: `feat` | `fix` | `refactor` | `test` | `docs` | `style` | `chore`
  - scope: `member` | `product` | `category` | `option` | `order` | `wish` | `auth` | `exception`
- 설계 결정과 트레이드오프는 Plan Mode에서 `docs/adr/`에 ADR로 기록.

## 문서
- 아키텍처: `docs/architecture.md`
- 코드 컨벤션: `docs/conventions.md`
- 예외 설계: `docs/exception-design.md`
- 설계 결정 내역: `docs/adr/`
