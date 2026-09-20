# Credit scoring checks

Profile: standard
Plan: `.specs/features/credit-scoring/plan.md`

## Checks

### S1 - Domain input and scoring

**C1** - Names and cities are trimmed before validation and the name limit is 150 characters
Proof: `./gradlew test --tests "*DomainValidationTest.namesAreTrimmedAndNameLimitIs150"`

**C2** - Age below 18 is rejected and age 18 is accepted
Proof: `./gradlew test --tests "*DomainValidationTest.ageBoundary"`

**C3** - Monthly income accepts 0 and 999999999999.99, rejects values outside that inclusive range, and rejects scale above 2
Proof: `./gradlew test --tests "*DomainValidationTest.monthlyIncomeBoundsAndScale"`

**C4** - A document number is canonicalized to exactly 11 digits after punctuation removal and check digits are not validated
Proof: `./gradlew test --tests "*DomainValidationTest.documentNumberNormalization"`

**C5** - Inputs age 30, income 1800, and temperature 30 produce components 15, 36, and 150 and final score 201
Proof: `./gradlew test --tests "*CreditScoreCalculatorTest.exampleProducesScore201"`

**C6** - A decimal score at the exact half boundary is rounded half-up to the next integer
Proof: `./gradlew test --tests "*CreditScoreCalculatorTest.roundsHalfUp"`

**C7** - Score 199 is not approved and score 200 is approved
Proof: `./gradlew test --tests "*CreditScoreCalculatorTest.approvalBoundary"`

### S2 - Create and persist the first analysis

**C8** - A valid POST returns 201 and only score, approved, and createdAt in the success body
Proof: `./gradlew test --tests "*CreditAnalysisControllerTest.createReturns201SuccessBody"`

**C9** - A successful POST persists one User and one Score snapshot for the canonical document
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.createsUserAndFirstScore"`

**C10** - A duplicate canonical document returns 409 and does not create a second User
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.duplicateDocumentReturns409"`

**C11** - Invalid POST input returns 400 and persists neither User nor Score
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.invalidCreateDoesNotPersist"`

### S3 - Repeat analysis for an existing customer

**C12** - A valid PUT returns 201 and appends one Score snapshot
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.validPutAppendsScore"`

**C13** - A successful PUT preserves the prior snapshot and updates current User data
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.putPreservesHistoryAndUpdatesUser"`

**C14** - A PUT for an unknown document returns 404
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.unknownDocumentReturns404"`

**C15** - A failed PUT leaves User data and Score history unchanged
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.failedPutDoesNotMutate"`

**C16** - An optimistic-lock conflict returns 409
Proof: `./gradlew test --tests "*UpdateCreditAnalysisIntegrationTest.optimisticLockConflictReturns409"`

### S4 - List analysis history

**C17** - A history request returns 200 with default page 0, default size 20, and item fields id, score, approved, and createdAt
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.returnsDefaultPage"`

**C18** - History is ordered by createdAt descending and id descending for ties
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.ordersByCreatedAtAndIdDescending"`

**C19** - Negative page and size above 100 return 400
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.rejectsPageAndSizeBounds"`

**C20** - A document with no analyses returns 404
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.noAnalysesReturns404"`

**C21** - Punctuation in the history path is normalized before querying
Proof: `./gradlew test --tests "*ListCreditAnalysesIntegrationTest.normalizesDocumentPath"`

### S5 - External weather failures and GlobalExceptionHandler

**C22** - Timeout, network, and 5xx weather failures use four total attempts with 100, 200, and 400 ms backoff delays through the Feign client
Proof: `./gradlew test --tests "*OpenWeatherClientTest.retriesTransientFailuresWithConfiguredBackoff"`

**C23** - A weather 4xx is not retried and an unknown city becomes a 400 response
Proof: `./gradlew test --tests "*OpenWeatherClientTest.doesNotRetryClientErrors"`

**C24** - Exhausted weather attempts return 502 without provider body, API key, or internal detail and persist nothing
Proof: `./gradlew test --tests "*CreateCreditAnalysisIntegrationTest.exhaustedWeatherFailureReturns502"`

**C25** - Validation exceptions return application/problem+json with 400 and every field error in the errors property
Proof: `./gradlew test --tests "*GlobalExceptionHandlerTest.validationReturnsProblemDetailWithAllFields"`

**C26** - Duplicate and missing-resource exceptions map to 409 and 404 ProblemDetail responses
Proof: `./gradlew test --tests "*GlobalExceptionHandlerTest.domainConflictsAndMissingResourcesUseMappedStatuses"`

**C27** - Weather and optimistic-lock exceptions map to 502 and 409 without internal details
Proof: `./gradlew test --tests "*GlobalExceptionHandlerTest.externalAndConcurrencyFailuresAreSafe"`

**C28** - Unexpected exceptions return 500 with detail `An unexpected internal error occurred`
Proof: `./gradlew test --tests "*GlobalExceptionHandlerTest.unexpectedExceptionUsesSafe500Detail"`

**C29** - The OpenWeather adapter is declared with `@FeignClient` and the application enables scanning with `@EnableFeignClients`
Proof: `./gradlew test --tests "*ArchitectureTest.feignClientIsUsedForOpenWeather"`

### S6 - Build quality and documentation

**C30** - The JaCoCo verification task enforces domain instruction and branch minimums of 1.0
Proof: `./gradlew test jacocoTestReport jacocoTestCoverageVerification`

**C31** - The JaCoCo verification task enforces overall instruction and branch minimums of 0.95
Proof: `./gradlew test jacocoTestReport jacocoTestCoverageVerification`

**C32** - Formatting and Detekt checks pass with the implemented source and tests
Proof: `./gradlew ktlintMainSourceSetCheck ktlintTestSourceSetCheck detekt`

**C33** - The CI workflow runs Java 25, formatting, Detekt, tests, JaCoCo report, and coverage verification
Proof: `./gradlew test --tests "*RepositoryConfigurationTest.ciWorkflowContainsRequiredGates"`

**C34** - Dependabot declares Gradle dependency updates
Proof: `./gradlew test --tests "*RepositoryConfigurationTest.dependabotDeclaresGradleUpdates"`

**C35** - README documents prerequisites, environment variables, local run commands, verification commands, and all three routes
Proof: `./gradlew test --tests "*RepositoryConfigurationTest.readmeDocumentsRuntimeAndApi"`

**C36** - HTTP boundary tests use Rest Assured, isolated unit tests use Mockito, and PostgreSQL integration tests use Testcontainers
Proof: `./gradlew test --tests "*RepositoryConfigurationTest.approvedTestingLibrariesAreUsedByTheirTestLevels"`

**C37** - The final implementation includes a reviewed and passing ArchitectureTest.kt for the package dependency graph
Proof: `./gradlew test --tests "*ArchitectureTest"`

## Coverage

| Set (size) | Member -> proof | Unproven |
| --- | --- | --- |
| name and city validation boundaries (2) | trimmed values C1; blank/length boundaries C1 | - |
| age decision boundaries (2) | age 17 C2; age 18 C2 | - |
| monthly income boundaries (4) | zero C3; maximum C3; above maximum C3; scale above 2 C3 | - |
| document normalization outcomes (2) | punctuation removed C4; exactly 11 digits without check validation C4 | - |
| score component calculation (3) | age component C5; income component C5; temperature component C5 | - |
| rounding and approval decision table (4) | exact half C6; below 200 C7; exactly 200 C7; above 200 C7 | - |
| POST `/credit-analyses` statuses (5) | 201 C8; 400 C11; 409 C10; 502 C24; 500 C28 | - |
| PUT `/credit-analyses/{document_number}` statuses (6) | 201 C12; 400 C15; 404 C14; 409 C16; 502 C24; 500 C28 | - |
| GET `/credit-analyses/{document_number}` statuses (4) | 200 C17; 400 C19; 404 C20; 500 C28 | - |
| weather outcomes (4) | timeout/network/5xx retry C22; provider 4xx C23; unknown city C23; exhausted failure C24 | - |
| GlobalExceptionHandler mappings (7) | validation C25; duplicate C26; not found C26; weather C27; concurrency C27; malformed input C25; unexpected C28 | - |
| Feign client wiring (2) | `@FeignClient` declaration C29; `@EnableFeignClients` C29 | - |
| persisted entities and constraints (4) | User C9; Score C9; immutable history C13; optimistic version C16 | - |
| pagination decision table (4) | default page/size C17; descending order C18; negative page C19; size maximum C19 | - |
| quality and delivery artifacts (5) | coverage rules C30/C31; CI gates C33; Dependabot C34; README C35; Feign dependency policy C29 | - |
| approved testing libraries by test level (3) | Rest Assured boundary tests C36; Mockito isolated tests C36; Testcontainers persistence tests C36 | - |
| architecture dependency rules (2) | domain dependency rule C37; application dependency rule C37 | - |

The coverage join is written at authoring time and the `standard` verifier must recompute it from
the plan, source, and tests. Claims naming a status, route, or response shape are proven at the
HTTP boundary; domain decision tables are also proven at the domain layer.

## Test policy

| Code shape | Required proofs | Coverage expectation |
| --- | --- | --- |
| Domain decision rules | Direct domain unit tests | Every validation boundary, score component, rounding outcome, and approval outcome is asserted |
| Application orchestration reached through an API | One Rest Assured boundary integration proof and one use-case proof | Persistence, no-mutation, retry, and conflict outcomes are asserted at the relevant layer |
| REST boundary and GlobalExceptionHandler | Rest Assured endpoint proof plus direct Mockito-backed handler proof for mappings | Every documented route status and every mapped exception class is asserted |
| Persistence adapters and Flyway migration | Testcontainers integration proof | User/Score relation, uniqueness, ordering, and optimistic locking are asserted |
| OpenWeather Feign adapter | Controlled stub-server proof | Feign declaration, retryable statuses, non-retryable statuses, four-attempt limit, delay sequence, timeout, and response mapping are asserted |
| Configuration, CI, Dependabot, and README | Repository-level structural proof | Every required file and command is present and named explicitly |

Evidence:

- The repository currently has no policy that defines proof level and decision-table depth for these layers.
- `ArchitectureTest.kt` is the closest existing test precedent for repository-level structural rules.
- The implementation must use direct handler tests plus endpoint and integration tests for `ProblemDetail` mappings.
- The final implementation must include a post-build review of `ArchitectureTest.kt` and a passing targeted run.

## Swept

- validation: C1, C2, C3, C4, C19, C25
- failure modes: C11, C15, C23, C24, C27, C28
- idempotency: n/a - the source defines retry behavior but no client idempotency key or deduplication contract
- authorization: n/a - the source defines no authentication or authorization policy
- concurrency: C16
- data lifecycle: n/a - the source defines no retention, archival, deletion, or backfill behavior
- dependency failure: C22, C23, C24
- state transitions: C12, C13, C15, C16
- observability: n/a - the source defines no logging, metrics, or tracing contract

## Handoff

The estimate is based on expected implementation and test files before code exists. It uses the
skill arithmetic of file bytes divided by four and stays below the declared `150k` budget.

- S1 domain = 12 files x 4 KB / 4 = 12k tokens
- S2 create and application flow = 18 files x 5 KB / 4 = 22.5k tokens
- S3 repeat analysis and persistence = 14 files x 6 KB / 4 = 21k tokens
- S4 history = 8 files x 5 KB / 4 = 10k tokens
- S5 weather, Feign client, and GlobalExceptionHandler = 14 files x 5 KB / 4 = 17.5k tokens
- S6 quality, CI, Dependabot, and README = 8 files x 4 KB / 4 = 8k tokens
- S7 test-tool and architecture review = 4 files x 4 KB / 4 = 4k tokens
- Total estimate = 95k tokens, below 150k; mechanism: one builder

No implementation code should start until `validate_checks.py` passes. Before every commit, the
builder must present the staged slice and request explicit human validation. The builder updates
the checks with completion evidence only after approval and stops after the last green commit.
The final implementation slice must review `ArchitectureTest.kt` before its commit. The orchestrator
then dispatches a fresh verifier over the full feature diff.
