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
- [ ] 이메일/비밀번호 회원가입
- [ ] 이메일/비밀번호 로그인 (JWT 발급)
- [ ] 카카오 OAuth2 로그인

### 상품
- [ ] 상품 목록 조회 (페이징)
- [ ] 상품 단건 조회
- [ ] 상품 등록
- [ ] 상품 수정
- [ ] 상품 삭제

### 카테고리
- [ ] 카테고리 목록 조회
- [ ] 카테고리 등록
- [ ] 카테고리 수정
- [ ] 카테고리 삭제

### 옵션
- [ ] 상품별 옵션 목록 조회
- [ ] 옵션 등록
- [ ] 옵션 삭제 (상품당 최소 1개 유지)

### 위시리스트
- [ ] 위시리스트 조회 (페이징)
- [ ] 위시 추가
- [ ] 위시 삭제

### 주문
- [ ] 주문 목록 조회 (페이징)
- [ ] 주문 생성 (재고 차감, 포인트 결제, 카카오 알림)
