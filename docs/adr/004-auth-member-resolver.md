# ADR-004: HandlerMethodArgumentResolver 기반 인증

## Status
Proposed

## Context
현재 AuthenticationResolver.extractMember()가 모든 예외를 null로 삼키고,
인증 필요 컨트롤러마다 null 체크와 @RequestHeader 파라미터가 중복된다.
예외 계층 도입 후 null 반환 패턴은 금지 규칙과 충돌한다.

## Decision
@AuthMember 어노테이션 + AuthMemberResolver(HandlerMethodArgumentResolver) 도입.
JWT 파싱 실패 시 UnauthorizedException 던짐 → GlobalExceptionHandler가 401 반환.
Controller에서 @RequestHeader("Authorization") 파라미터 제거.

## Alternatives
- Option A (예외 던지기만): AuthenticationResolver에서 null 대신 예외 던짐.
  null 체크는 제거되지만 @RequestHeader 파라미터와 extractMember() 호출은 남음.

## Trade-offs
간접성 증가 (@AuthMember가 어떻게 주입되는지 바로 안 보임).
단, Spring MVC 관례(@AuthenticationPrincipal 동일 패턴)를 따르므로
Spring에 익숙한 개발자에게는 오히려 자연스러운 패턴.
인증 방식 변경 시 AuthMemberResolver 하나만 수정.
