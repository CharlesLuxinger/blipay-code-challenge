# Project state

## Decisions

| ID | Decision | Rationale | Status | Date |
| --- | --- | --- | --- | --- |
| AD-001 | `DECISIONS.md` is the binding product and implementation decision source | It expands the challenge minimum into the approved API, persistence, weather, and testing contract | active | 2026-09-20 |
| AD-002 | Verification profile is `standard` with a `150k` budget | Coverage requirements need recomputed coverage and fault injection | active | 2026-09-20 |
| AD-003 | Domain instruction and branch coverage are 100%; overall instruction and branch coverage are at least 95% | The domain contains the credit decision and must be fully discriminated; the rest of the application has a high quality floor | active | 2026-09-20 |
| AD-004 | OpenWeather uses four total attempts with 100 ms, 200 ms, and 400 ms backoff delays; each attempt has 2 s connect and read timeouts | This resolves the retry-count ambiguity and makes timeout behavior bounded and testable | active | 2026-09-20 |
| AD-005 | API failures are translated by an infra `GlobalExceptionHandler` into RFC 7807 `ProblemDetail` responses | All routes need one stable `application/problem+json` error boundary without leaking internal details | active | 2026-09-20 |
| AD-006 | Human approval is required before every commit | The author must not turn an unreviewed implementation slice into repository history | active | 2026-09-20 |
| AD-007 | Kotlin files stay small and responsibility-focused | Splitting domain, application, API, persistence, clients, and error handling keeps changes reviewable and prevents large aggregate files | active | 2026-09-20 |
| AD-008 | External API clients use Spring Cloud OpenFeign | The project standardizes declarative clients and keeps provider calls behind a typed adapter | active | 2026-09-20 |
| AD-009 | Rest Assured proves HTTP boundaries, Mockito provides isolated test doubles, and Testcontainers proves PostgreSQL integration | Each test tool is assigned to the boundary it can observe without replacing the wrong level of proof | active | 2026-09-20 |
| AD-010 | `ArchitectureTest.kt` is reviewed and updated after the implementation is complete | New packages and adapters can make the initial dependency rules incomplete | active | 2026-09-20 |

## Handoff

**Feature**: credit-scoring;

**Where**: Planning artifacts are complete and validated; implementation and verification are not started

**In progress**: none - `.specs/features/credit-scoring/plan.md` and `checks.md` are frozen for review

**Next step**: Human review of the frozen obligations, then write tests from `checks.md` and implement the feature; request approval before every commit

**Blockers**: none

**Uncommitted**: `AGENTS.md`, `.specs/STATE.md`, `.specs/features/credit-scoring/plan.md`, `.specs/features/credit-scoring/checks.md`

**Branch**: main
