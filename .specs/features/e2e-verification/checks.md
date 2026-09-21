# E2E Verification checks

Profile: standard
Plan: `.specs/features/credit-scoring/plan.md`

## Intent

Verify the credit scoring API matches BACKEND_INSTRUCTIONS.MD requirements and that README instructions work for a human tester. This validates all three endpoints, score calculation, document normalization, error handling, and end-to-end flows.

5 checks in 1 slice · 2 one-way doors · 0 open

## Checks

### S1 - E2E Verification · 1 slice · ~15k

**C1** - POST creates analysis with correct score from challenge example (age=30, income=1800, temp=30 -> score=201, approved=true)
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.createsUserAndFirstScore"`

**C2** - Document normalization removes punctuation (123.456.789-09 -> 12345678909) and prevents duplicates
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.duplicateDocumentReturns409"`

**C3** - PUT appends new score while preserving history
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.putPreservesHistoryAndUpdatesUser"`

**C4** - GET returns paginated history with correct defaults and bounds
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.returnsDefaultPage" "*ListCreditAnalysesIntegrationTest.rejectsPageAndSizeBounds"`

**C5** - Error responses use application/problem+json with correct status codes
Proof: `./gradlew test --tests "*GlobalExceptionHandlerTest" "*CreateCreditAnalysisIntegrationTest.invalidCreateDoesNotPersist"`

## Coverage

| Set (size) | Member -> proof | Unproven |
| --- | --- | --- |
| API routes (3) | POST C1 · PUT C3 · GET C4 | - |
| HTTP statuses (5) | 201 C1,C3 · 200 C4 · 400 C4,C5 · 404 C3 · 409 C2 | - |
| Document normalization (2) | with punctuation C2 · without punctuation C2 | - |
| Score calculation (3) | age component C1 · income component C1 · temperature component C1 | - |

## Swept

- validation: C4, C5
- failure modes: C5
- idempotency: n/a - API design is not idempotent
- authorization: n/a - no auth in challenge
- concurrency: n/a - tested in unit tests
- data lifecycle: n/a - no retention policy
- dependency failure: n/a - weather stub used in integration tests
- state transitions: C1, C3
- observability: n/a - no logging requirement

## Handoff

Size: ~15k tokens (5 checks × ~3k each)
Under the budget (150k) — one builder, no ask.
