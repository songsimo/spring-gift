/**
 * k6 부하 테스트 — 동시 주문 재고/포인트 차감 정확성
 *
 * 사전 준비:
 *   1. ./gradlew bootRun 으로 서버 기동 (MySQL 연결 또는 H2 로컬)
 *   2. JWT 토큰 발급 후 TOKEN 변수에 설정
 *   3. 테스트할 optionId 확인 후 설정 (재고가 VUS 수 이상인 옵션)
 *
 * 실행:
 *   k6 run load-test/order-concurrency.js
 *   k6 run --vus 50 --duration 30s load-test/order-concurrency.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';

// ── 설정값 ─────────────────────────────────────────────────────────────────
const BASE_URL = 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || 'YOUR_JWT_TOKEN_HERE';
const OPTION_ID = __ENV.OPTION_ID || '3'; // 재고가 충분한 옵션 ID

// ── 커스텀 메트릭 ───────────────────────────────────────────────────────────
const orderSuccess = new Counter('order_success');
const orderFail = new Counter('order_fail');
const stockError = new Rate('stock_error_rate');

// ── 부하 프로파일 ───────────────────────────────────────────────────────────
export const options = {
    scenarios: {
        // 시나리오 1: 순차 워밍업 (10명, 10초)
        warmup: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10 },
                { duration: '5s', target: 10 },
            ],
            gracefulRampDown: '5s',
            tags: { scenario: 'warmup' },
        },
        // 시나리오 2: 동시 주문 스파이크 (50명이 동시에 주문)
        spike: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 50,
            maxVUs: 100,
            stages: [
                { duration: '10s', target: 50 },
                { duration: '20s', target: 50 },
                { duration: '5s', target: 0 },
            ],
            startTime: '25s', // warmup 이후 시작
            tags: { scenario: 'spike' },
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<500'],   // 95%ile 응답시간 500ms 이하
        http_req_failed: ['rate<0.1'],       // 실패율 10% 이하
        order_success: ['count>0'],          // 최소 1건 이상 성공
    },
};

const HEADERS = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
};

// ── 주문 생성 ───────────────────────────────────────────────────────────────
export default function () {
    const payload = JSON.stringify({
        optionId: parseInt(OPTION_ID),
        quantity: 1,
        message: `k6 load test - VU ${__VU}`,
    });

    const res = http.post(`${BASE_URL}/api/orders`, payload, { headers: HEADERS });

    const isCreated = check(res, {
        '201 Created': (r) => r.status === 201,
    });

    if (isCreated) {
        orderSuccess.add(1);
    } else {
        orderFail.add(1);
        // 재고 부족(400)은 기대된 실패 — 비관적 락이 동작한다는 증거
        const isStockError = res.status === 400 &&
            (res.body || '').includes('재고');
        stockError.add(isStockError ? 1 : 0);
    }

    sleep(0.1);
}

// ── 테스트 완료 후 요약 ─────────────────────────────────────────────────────
export function handleSummary(data) {
    const summary = {
        'order_success': data.metrics.order_success?.values?.count ?? 0,
        'order_fail': data.metrics.order_fail?.values?.count ?? 0,
        'p95_response_ms': data.metrics.http_req_duration?.values?.['p(95)'] ?? '-',
        'error_rate': data.metrics.http_req_failed?.values?.rate ?? '-',
    };
    console.log('=== 부하 테스트 결과 ===');
    console.log(JSON.stringify(summary, null, 2));
    return {};
}
