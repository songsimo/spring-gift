# Exception Design

## 예외 계층 구조

```
BusinessException (abstract)
├── NotFoundException        → HTTP 404
├── DuplicateException       → HTTP 409
├── UnauthorizedException    → HTTP 401
├── ForbiddenException       → HTTP 403
├── InsufficientPointException   → HTTP 400
└── InsufficientStockException   → HTTP 400
```

**패키지**: `gift.exception`

## GlobalExceptionHandler

`@RestControllerAdvice`로 전역 처리. 각 Controller의 `@ExceptionHandler` 제거.

```
BusinessException 하위 → 각 예외 타입에 매핑된 HTTP 상태 반환
MethodArgumentNotValidException → 400 + validation 오류 목록
그 외 RuntimeException → 500 (내부 메시지 미노출)
```

## Error Response 형식

```json
{
  "message": "Product not found",
  "status": 404
}
```

- `message`: 클라이언트에 노출되는 메시지 (영어, 내부 ID 미포함)
- `status`: HTTP 상태 코드

## 예외 사용 예시

```java
// NotFoundException (404)
productRepository.findById(id)
    .orElseThrow(() -> new NotFoundException("Product not found"));

// DuplicateException (409)
if (memberRepository.existsByEmail(email)) {
    throw new DuplicateException("Email already registered");
}

// UnauthorizedException (401)
// AuthenticationResolver에서 JWT 파싱 실패 시
throw new UnauthorizedException("Invalid or missing token");

// ForbiddenException (403)
if (!wish.getMemberId().equals(member.getId())) {
    throw new ForbiddenException("Not your wish");
}

// InsufficientPointException (400)
// Member.deductPoint()에서 포인트 부족 시

// InsufficientStockException (400)
// Option.subtractQuantity()에서 재고 부족 시
```

## 기존 코드와의 차이

| 기존 | 개선 후 |
|------|--------|
| `IllegalArgumentException` → 항상 400 | 예외 타입별 적절한 HTTP 상태 |
| Controller마다 `@ExceptionHandler` 중복 | `GlobalExceptionHandler` 단일 처리 |
| 내부 ID 노출 (`"id=" + id`) | 클라이언트 안전 메시지만 노출 |
| 도메인 예외 메시지 한국어 하드코딩 | 영어 메시지, 예외 타입으로 의미 전달 |
