# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
./gradlew build          # Build the project
./gradlew test           # Run all tests
./gradlew test --tests "ClassName.methodName"  # Run a single test
./gradlew bootRun        # Run the application
./gradlew ktlintCheck    # Check code style
./gradlew ktlintFormat   # Auto-fix code style
```

## Architecture Overview

### Data Layer

- **Flyway** manages schema (`V1__` = DDL, `V2__` = seed data in `src/main/resources/db/migration/`)
- **H2** in test scope; **MySQL** in production
- Entities use a protected no-arg constructor for JPA; updates go through an `update()` method on the entity
- Some relations use entity references (e.g. `Option` → `Product`), others use primitive FK (`Order.memberId`) — be consistent with the existing pattern when adding new entities

### Authentication Flow

1. `POST /api/members/register` or `POST /api/members/login` → returns a JWT
2. Kakao OAuth: `GET /api/auth/kakao/callback` → issues a JWT wrapping the Kakao access token
3. Protected endpoints read the `Authorization: Bearer <token>` header; `AuthenticationResolver` resolves it to a `Member`

### External Integrations

- **`KakaoLoginClient`** — OAuth2 token exchange with Kakao
- **`KakaoMessageClient`** — Sends a Kakao Talk message after an order completes (failure is logged but does not fail the order)
- Kakao credentials and JWT secret are read from `application.properties` and can be overridden via environment variables

### DTO Conventions

- Request/response types are Kotlin **data classes** or Java **records**
- Request DTOs carry Jakarta Validation annotations (`@NotBlank`, `@Positive`, etc.)
- Custom validators are static methods in `ProductNameValidator` and `OptionNameValidator`

## Core Mission Rules (Strict Compliance)

### Development Rhythm & Workflow

- Plan First: Never modify code before defining the "Next Single Task" in the README.md checklist.
- Atomic Changes: Modify only one small piece (one class or method) at a time.
- TDD Loop: Follow the Red → Green → Refactor cycle. All tests must pass after every single change.

### README Development Log

After every fix or feature task, append an entry to the **"AI-Assisted Development Log"** section in `README.md` using this format:

```
### [YYYY-MM-DD] 작업 제목

**문제 정의**
(what the problem was)

**AI와의 상호작용**
(what was proposed, what decision the user made)

**결과**
(what changed)
```

### Architecture Decision Record (ADR)
Create an ADR entry in README.md if:

- Multiple options with trade-offs exist.
- Recurring rules or boundaries are defined.
- Testing or verification strategy is central to the decision.

## Security Guardrails
Confidentiality: Never include Admin Keys, Access Tokens, or Client Secrets in the GitHub repository or client-side code.