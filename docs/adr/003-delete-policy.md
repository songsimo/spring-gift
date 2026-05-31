# ADR-003: 상품/카테고리 삭제 정책

## Status
Proposed

## Context
현재 deleteById()를 바로 호출해 연관 데이터 존재 시 FK 제약 위반이 발생한다.
- 상품 삭제: 위시(wish.product_id), 주문(order → option → product) FK 위반 가능
- 카테고리 삭제: 상품(product.category_id) FK 위반 가능

## Decision
엔티티별 정책 분리:
- 주문이 있는 상품: 삭제 거부 (409) — 주문은 재무 기록, 삭제 불가
- 위시만 있는 상품: 위시 cascade 삭제 후 상품 삭제
- 상품이 있는 카테고리: 삭제 거부 (409)

추후 "판매 중단" 개념 필요 시 ProductStatus Enum(ACTIVE/DISCONTINUED)으로 확장.

## Alternatives
- Soft Delete (@SQLRestriction): 데이터 보존되나 모든 쿼리에 숨겨진 필터, 가독성 저하.
  Unique constraint 재설계 필요. 현 규모에서 비용 대비 효과 낮음.
- Hard Delete with cascade: 주문 데이터 유실 위험으로 채택 불가.

## Trade-offs
거부 정책은 단순하고 데이터 안전하지만, 운영 중 상품 삭제가 어려워질 수 있음.
이 경우 Status Enum으로 전환하는 것이 자연스러운 확장 경로.
