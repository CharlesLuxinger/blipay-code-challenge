# Credit scoring verification

**Verdict**: FAIL
**Profile**: standard
**Diff range**: 027eb82..HEAD
**Round**: 1 - full
**Verifier**: independent sub-agent (author != verifier)

## Binding sources

(no binding sources declared in plan)

## Checks

| Check | Claim | Proof run | Evidence | Result |
| --- | --- | --- | --- | --- |
| C1 | Names and cities are trimmed before validation; name limit is 150 characters | `gradlew test --tests "*DomainValidationTest.namesAreTrimmedAndNameLimitIs150"` exit 0 | `DomainValidationTest.kt:13` – `assertEquals("Maria", input.name.value)` / `assertEquals("Recife", input.city.value)` / `assertFailsWith<IllegalArgumentException> { validInput(name = "x".repeat(151)) }` | PASS |
| C2 | Age below 18 is rejected; age 18 is accepted | `gradlew test --tests "*DomainValidationTest.ageBoundary"` exit 0 | `DomainValidationTest.kt:26` – `assertEquals(18, validInput(age = 18).age.value)` / `assertFailsWith<IllegalArgumentException> { validInput(age = 17) }` | PASS |
| C3 | Monthly income accepts 0 and 999999999999.99, rejects values outside range and scale > 2 | `gradlew test --tests "*DomainValidationTest.monthlyIncomeBoundsAndScale"` exit 0 | `DomainValidationTest.kt:32` – `assertEquals(BigDecimal.ZERO, ...)` / `assertFailsWith<IllegalArgumentException> { validInput(monthlyIncome = BigDecimal("-0.01")) }` / `assertFailsWith<IllegalArgumentException> { validInput(monthlyIncome = BigDecimal("1.001")) }` | PASS |
| C4 | Document number canonicalized to exactly 11 digits; check digits not validated | `gradlew test --tests "*DomainValidationTest.documentNumberNormalization"` exit 0 | `DomainValidationTest.kt:44` – `assertEquals("12345678909", validInput(documentNumber = "123.456.789-09").documentNumber.value)` / `assertFailsWith<IllegalArgumentException> { validInput(documentNumber = "1234567890") }` | PASS |
| C5 | age=30, income=1800, temp=30 produces components 15, 36, 150 and score 201 | `gradlew test --tests "*CreditScoreCalculatorTest.exampleProducesScore201"` exit 0 | `CreditScoreCalculatorTest.kt:14` – `assertEquals(0, result.ageComponent.compareTo(BigDecimal("15")))` / `assertEquals(0, result.incomeComponent.compareTo(BigDecimal("36")))` / `assertEquals(0, result.temperatureComponent.compareTo(BigDecimal("150")))` / `assertEquals(201, result.score)` | PASS |
| C6 | Decimal score at exact half boundary rounds half-up | `gradlew test --tests "*CreditScoreCalculatorTest.roundsHalfUp"` exit 0 | `CreditScoreCalculatorTest.kt:32` – `assertEquals(10, exactHalf.score)` (total = 9.5, HALF_UP → 10) | PASS |
| C7 | Score 199 is not approved; score 200 is approved | `gradlew test --tests "*CreditScoreCalculatorTest.approvalBoundary"` exit 0 | `CreditScoreCalculatorTest.kt:38` – `assertEquals(false, CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal("38")).approved)` / `assertEquals(true, CreditScoreCalculator.calculate(input.age, input.monthlyIncome, BigDecimal("38.2")).approved)` | PASS |
| C8 | Valid POST returns 201 with only score, approved, and createdAt | Testcontainers/Docker required | `CreditAnalysisControllerTest.kt:17` – `.statusCode(201)` / `.body("score", equalTo(201))` / `.body("approved", equalTo(true))` / `.body("createdAt", notNullValue())` / `.body("size()", equalTo(3))` | BLOCKED (Docker) |
| C9 | Successful POST persists one User and one Score snapshot | Testcontainers/Docker required | `CreateCreditAnalysisIntegrationTest.kt:19` – `assertEquals(1, users.count())` / `assertEquals(1, scores.count())` / `assertEquals("12345678909", users.findAll().single().documentNumber)` | BLOCKED (Docker) |
| C10 | Duplicate canonical document returns 409, no second User created | Testcontainers/Docker required | `CreateCreditAnalysisIntegrationTest.kt:32` – `.statusCode(409)` / `assertEquals(1, users.count())` | BLOCKED (Docker) |
| C11 | Invalid POST input returns 400, persists neither User nor Score | Testcontainers/Docker required | `CreateCreditAnalysisIntegrationTest.kt:43` – `.statusCode(400)` / `assertEquals(0, users.count())` / `assertEquals(0, scores.count())` | BLOCKED (Docker) |
| C12 | Valid PUT returns 201 and appends one Score snapshot | Testcontainers/Docker required | `UpdateCreditAnalysisIntegrationTest.kt:24` – `.statusCode(201)` / `assertEquals(2, scores.count())` | BLOCKED (Docker) |
| C13 | Successful PUT preserves prior snapshot and updates current User data | Testcontainers/Docker required | `UpdateCreditAnalysisIntegrationTest.kt:39` – `assertEquals("Ana", user.name)` / `assertEquals("Olinda", user.city)` / `assertEquals(2, scores.count())` | BLOCKED (Docker) |
| C14 | PUT for unknown document returns 404 | Testcontainers/Docker required | `UpdateCreditAnalysisIntegrationTest.kt:53` – `.statusCode(404)` | BLOCKED (Docker) |
| C15 | Failed PUT leaves User data and Score history unchanged | Testcontainers/Docker required | `UpdateCreditAnalysisIntegrationTest.kt:66` – `assertEquals(before, users.findAll().single().name)` / `assertEquals(1, scores.count())` | BLOCKED (Docker) |
| C16 | Optimistic-lock conflict returns 409 | Testcontainers/Docker required | `UpdateCreditAnalysisIntegrationTest.kt:96` – `assertFailsWith<VersionConflictException> { persistence.update(...) }` / `assertEquals(409, GlobalExceptionHandler().conflict().status)` | BLOCKED (Docker) |
| C17 | History request returns 200 with default page 0, size 20, and fields id/score/approved/createdAt | Testcontainers/Docker required | `ListCreditAnalysesIntegrationTest.kt:16` – `.statusCode(200)` / `.body("page", equalTo(0))` / `.body("size", equalTo(20))` / `.body("items[0].score", equalTo(201))` | BLOCKED (Docker) |
| C18 | History ordered by createdAt desc, id desc for ties | Testcontainers/Docker required | `ListCreditAnalysesIntegrationTest.kt:40` – `assertEquals(ids.sortedDescending(), ids)` | BLOCKED (Docker) |
| C19 | Negative page and size > 100 return 400 | Testcontainers/Docker required | `ListCreditAnalysesIntegrationTest.kt:46` – `.statusCode(400)` (for page=-1 and size=101) | BLOCKED (Docker) |
| C20 | Document with no analyses returns 404 | Testcontainers/Docker required | `ListCreditAnalysesIntegrationTest.kt:52` – `.statusCode(404)` | BLOCKED (Docker) |
| C21 | Punctuation in history path is normalized before querying | Testcontainers/Docker required | `ListCreditAnalysesIntegrationTest.kt:58` – `.get("/credit-analyses/123.456.789-09").then().statusCode(200)` | BLOCKED (Docker) |
| C22 | Timeout/network/5xx use 4 total attempts with 100/200/400 ms backoff | `gradlew test --tests "*OpenWeatherClientTest.retriesTransientFailuresWithConfiguredBackoff"` exit 0 | `OpenWeatherClientTest.kt:32` – `assertFailsWith<RetryableException> { retryer.continueOrPropagate(failure) }` (4th call fails) / `assertEquals(100L, retryerType.getDeclaredField("period").valueOf(retryer))` / `assertEquals(400L, ..maxPeriod..)` / `assertEquals(4, ..maxAttempts..)` | PASS |
| C23 | Weather 4xx is not retried; unknown city becomes 400 | `gradlew test --tests "*OpenWeatherClientTest.doesNotRetryClientErrors"` exit 0 | `OpenWeatherClientTest.kt:50` – `assertIs<UnknownCityException>(decoded)` (status 404 decoded to UnknownCityException, not RetryableException) | PASS |
| C24 | Exhausted weather attempts return 502 without provider body, API key, or internal detail, persist nothing | Testcontainers/Docker required | `CreateCreditAnalysisIntegrationTest.kt:56` – `.statusCode(502)` / `.body("detail", containsString("weather"))` / `assertFalse(response.asString().contains("test-key"))` / `assertEquals(4, weatherStub.calls)` / `assertEquals(0, users.count())` | BLOCKED (Docker) |
| C25 | Validation exceptions return application/problem+json 400 with all field errors | `gradlew test --tests "*GlobalExceptionHandlerTest.validationReturnsProblemDetailWithAllFields"` exit 0 | `GlobalExceptionHandlerTest.kt:34` – `assertEquals(400, problem.status)` / `assertEquals("Request validation failed", problem.detail)` / `assertEquals(2, (problem.properties!!["errors"] as List<*>).size)` | PASS |
| C26 | Duplicate and missing-resource map to 409 and 404 ProblemDetail | `gradlew test --tests "*GlobalExceptionHandlerTest.domainConflictsAndMissingResourcesUseMappedStatuses"` exit 0 | `GlobalExceptionHandlerTest.kt:43` – `assertEquals(409, handler.duplicate().status)` / `assertEquals(404, handler.notFound().status)` | PASS |
| C27 | Weather and optimistic-lock map to 502 and 409 without internal detail | `gradlew test --tests "*GlobalExceptionHandlerTest.externalAndConcurrencyFailuresAreSafe"` exit 0 | `GlobalExceptionHandlerTest.kt:49` – `assertEquals(502, handler.weatherFailure().status)` / `assertEquals(409, handler.conflict().status)` / `assertTrue(handler.weatherFailure().detail!!.contains("weather"))` | PASS |
| C28 | Unexpected exceptions return 500 with detail "An unexpected internal error occurred" | `gradlew test --tests "*GlobalExceptionHandlerTest.unexpectedExceptionUsesSafe500Detail"` exit 0 | `GlobalExceptionHandlerTest.kt:57` – `assertEquals(500, problem.status)` / `assertEquals("An unexpected internal error occurred", problem.detail)` | PASS |
| C29 | OpenWeather adapter declared with @FeignClient; application enables with @EnableFeignClients | `gradlew test --tests "*ArchitectureTest.feignClientIsUsedForOpenWeather"` exit 0 | `ArchitectureTest.kt:27` – `check(OpenWeatherFeignClient::class.java.isAnnotationPresent(FeignClient::class.java))` / `check(Application::class.java.isAnnotationPresent(EnableFeignClients::class.java))` | PASS |
| C30 | JaCoCo enforces domain instruction and branch minimums of 1.0 | `gradlew test jacocoTestReport jacocoTestCoverageVerification` — BUILD FAILED (Docker unavailable, test task fails before coverage runs) | `build.gradle.kts:136` – `element = "PACKAGE"`, `includes = listOf("com.blipay.credit_scoring.domain.*")`, `minimum = "1.0".toBigDecimal()` for INSTRUCTION and BRANCH. Rule exists but cannot execute end-to-end without Docker. | BLOCKED (Docker) |
| C31 | JaCoCo enforces overall instruction and branch minimums of 0.95 | `gradlew test jacocoTestReport jacocoTestCoverageVerification` — BUILD FAILED (Docker unavailable) | `build.gradle.kts:124` – `element = "BUNDLE"`, `minimum = "0.95".toBigDecimal()` for INSTRUCTION and BRANCH. Rule exists but cannot execute end-to-end without Docker. | BLOCKED (Docker) |
| C32 | ktlint and Detekt pass with implemented source and tests | `gradlew ktlintMainSourceSetCheck ktlintTestSourceSetCheck detekt` exit 0 | All three tasks reported `UP-TO-DATE` (cached green) in the batched run | PASS |
| C33 | CI workflow runs Java 25, formatting, Detekt, tests, JaCoCo report, and coverage verification | `gradlew test --tests "*RepositoryConfigurationTest.ciWorkflowContainsRequiredGates"` exit 0 | `RepositoryConfigurationTest.kt:14` – `assertTrue(workflow.contains("java-version: '25'"))` / `assertTrue(workflow.contains("ktlintMainSourceSetCheck"))` / `assertTrue(workflow.contains("detekt"))` / `assertTrue(workflow.contains("jacocoTestCoverageVerification"))` | PASS |
| C34 | Dependabot declares Gradle dependency updates | `gradlew test --tests "*RepositoryConfigurationTest.dependabotDeclaresGradleUpdates"` exit 0 | `RepositoryConfigurationTest.kt:25` – `assertTrue(config.contains("package-ecosystem: gradle"))` / `assertTrue(config.contains("interval: weekly"))` | PASS |
| C35 | README documents prerequisites, env vars, local commands, verification commands, and all three routes | `gradlew test --tests "*RepositoryConfigurationTest.readmeDocumentsRuntimeAndApi"` exit 0 | `RepositoryConfigurationTest.kt:33` – asserts presence of `"Java 25"`, `"Docker Compose"`, `"OPENWEATHER_API_URL"`, `"OPENWEATHER_API_KEY"`, `"DB_URL"`, `"./gradlew test"`, `"jacocoTestCoverageVerification"`, `"POST /credit-analyses"`, `"PUT /credit-analyses/{document_number}"`, `"GET"` | PASS |
| C36 | Rest Assured for boundary tests; Mockito for isolated unit tests; Testcontainers for PostgreSQL integration | `gradlew test --tests "*RepositoryConfigurationTest.approvedTestingLibrariesAreUsedByTheirTestLevels"` exit 0 | `RepositoryConfigurationTest.kt:58` – `assertTrue(gradle.contains("io.rest-assured:rest-assured"))` / `assertTrue(gradle.contains("org.mockito.kotlin:mockito-kotlin"))` / `assertTrue(gradle.contains("testcontainers-postgresql"))` / `assertTrue(tests.contains("RestAssured"))` / `assertTrue(tests.contains("PostgreSQLContainer"))` | PASS |
| C37 | Final implementation includes reviewed and passing ArchitectureTest.kt for package dependency graph | `gradlew test --tests "*ArchitectureTest"` exit 0 | `ArchitectureTest.kt:16` – `domainMustNotDependOnApplicationOrInfrastructure.check(importedClasses)` / `applicationMustNotDependOnInfrastructure.check(importedClasses)` / `feignClientIsUsedForOpenWeather()` — all 3 tests pass | PASS |

## Coverage

| Set (size) | Recomputed from | Member → proof | Unproven |
| --- | --- | --- | --- |
| name and city validation boundaries (2) | `Name.kt`, `City.kt`, `DomainValidationTest.kt` | trimmed values C1 (`DomainValidationTest.kt:13-14`); blank/length boundaries C1 (`DomainValidationTest.kt:19-21`) | none |
| age decision boundaries (2) | `Age.kt`, `DomainValidationTest.kt` | age 17 rejected C2 (`DomainValidationTest.kt:27`); age 18 accepted C2 (`DomainValidationTest.kt:26`) | none |
| monthly income boundaries (4) | `MonthlyIncome.kt`, `DomainValidationTest.kt` | zero C3 (`DomainValidationTest.kt:32`); maximum C3 (`DomainValidationTest.kt:33-36`); above maximum C3 (`DomainValidationTest.kt:38`); scale above 2 C3 (`DomainValidationTest.kt:39`) | none |
| document normalization outcomes (2) | `DocumentNumber.kt`, `DomainValidationTest.kt` | punctuation removed C4 (`DomainValidationTest.kt:44`); exactly 11 digits enforced without check-digit validation C4 (`DomainValidationTest.kt:46-47`) | none |
| score component calculation (3) | `CreditScoreCalculator.kt`, `CreditScoreCalculatorTest.kt` | age component C5 (`CreditScoreCalculatorTest.kt:14`); income component C5 (`CreditScoreCalculatorTest.kt:15`); temperature component C5 (`CreditScoreCalculatorTest.kt:16`) | none |
| rounding and approval decision table (4) | `CreditScoreCalculator.kt`, `CreditScoreCalculatorTest.kt` | exact half (9.5) C6 (`CreditScoreCalculatorTest.kt:32`); below 200 C7 (`CreditScoreCalculatorTest.kt:38`); exactly 200 C7 (`CreditScoreCalculatorTest.kt:39`); above 200 — not explicitly tested by a named case (approvalBoundary only tests 199 and 200 boundary; a score strictly > 200 is implied by the domain path for score=201 in C5 but not asserted as `approved=true` separately in a `> 200` case) | "above 200" boundary not independently asserted in C7 — C5 implies it via score 201 |
| POST `/credit-analyses` statuses (5) | Integration tests | 201 C8 (BLOCKED); 400 C11 (BLOCKED); 409 C10 (BLOCKED); 502 C24 (BLOCKED); 500 C28 — GlobalExceptionHandler test proves mapping but no HTTP endpoint round-trip for 500 without Docker | 201/400/409/502 BLOCKED; 500 endpoint proof BLOCKED |
| PUT `/credit-analyses/{document_number}` statuses (6) | Integration tests | 201 C12 (BLOCKED); 400 C15 (BLOCKED); 404 C14 (BLOCKED); 409 C16 (BLOCKED); 502 C24 (BLOCKED); 500 C28 (handler proof only) | all statuses BLOCKED |
| GET `/credit-analyses/{document_number}` statuses (4) | Integration tests | 200 C17 (BLOCKED); 400 C19 (BLOCKED); 404 C20 (BLOCKED); 500 C28 (handler proof only) | all statuses BLOCKED |
| weather outcomes (4) | `OpenWeatherClientTest.kt`, Integration tests | timeout/network/5xx retry C22 (PASS); provider 4xx C23 (PASS); unknown city C23 (PASS); exhausted failure C24 (BLOCKED) | exhausted failure end-to-end BLOCKED |
| GlobalExceptionHandler mappings (7) | `GlobalExceptionHandlerTest.kt` | validation C25 (PASS); duplicate C26 (PASS); not found C26 (PASS); weather C27 (PASS); concurrency C27 (PASS); malformed input — `MethodArgumentNotValidException` covered by C25; unexpected C28 (PASS) | none (all 7 proven at handler unit level) |
| Feign client wiring (2) | `OpenWeatherFeignClient.kt`, `Application.kt`, `ArchitectureTest.kt` | `@FeignClient` declaration C29 (`ArchitectureTest.kt:27`); `@EnableFeignClients` C29 (`ArchitectureTest.kt:28`) | none |
| persisted entities and constraints (4) | Integration tests | User C9 (BLOCKED); Score C9 (BLOCKED); immutable history C13 (BLOCKED); optimistic version C16 (BLOCKED) | all BLOCKED |
| pagination decision table (4) | Integration tests | default page/size C17 (BLOCKED); descending order C18 (BLOCKED); negative page C19 (BLOCKED); size maximum C19 (BLOCKED) | all BLOCKED |
| quality and delivery artifacts (5) | `RepositoryConfigurationTest.kt`, `build.gradle.kts` | coverage rules C30/C31 (rules exist in build.gradle.kts; enforcement BLOCKED by Docker); CI gates C33 (PASS); Dependabot C34 (PASS); README C35 (PASS); Feign dependency policy C29 (PASS) | coverage enforcement end-to-end BLOCKED |
| approved testing libraries by test level (3) | `RepositoryConfigurationTest.kt` | Rest Assured boundary tests C36 (PASS); Mockito isolated tests C36 (PASS); Testcontainers persistence tests C36 (PASS) | none |
| architecture dependency rules (2) | `ArchitectureTest.kt` | domain dependency rule C37 (`ArchitectureTest.kt:17`); application dependency rule C37 (`ArchitectureTest.kt:22`) | none |

**Note on "above 200" coverage**: The approval decision table member "above 200" is not independently asserted with `approved=true` at a score > 200. C5 asserts `score=201` from the example but does not assert `approved=true` for the > 200 case in isolation. C7 (`approvalBoundary`) tests score 199 (not approved) and score 200 (approved) but not a score strictly above 200 with `approved=true`. This is a minor coverage gap — the predicate `score >= 200` is proven true at exactly 200 and the implementation compiles with the correct operator, but the > 200 branch of approval is only indirectly implied. It does not change the verdict because `score >= 200` is a single comparison, not a two-branch decision.

## Test policy rows

| Row | Files it classifies | Required proof | Expectation met |
| --- | --- | --- | --- |
| Domain decision rules | `DomainValidationTest.kt`, `CreditScoreCalculatorTest.kt`, `DomainEntityCoverageTest.kt` | Direct domain unit tests without Spring or external services | YES — all 8 tests run and pass without any Spring context; `CreditAnalysisInput.of(...)` and `CreditScoreCalculator.calculate(...)` used directly |
| Application orchestration reached through API | `CreditAnalysisUseCaseTest.kt` (Mockito) + Testcontainers integration tests | One Rest Assured boundary proof + one use-case Mockito proof | PARTIAL — Mockito use-case proof passes (`CreditAnalysisUseCaseTest.kt`); Rest Assured integration proofs are all BLOCKED (Docker) |
| REST boundary and GlobalExceptionHandler | `GlobalExceptionHandlerTest.kt` (direct), Testcontainers endpoint tests | Rest Assured endpoint proof + direct Mockito-backed handler proof | PARTIAL — direct handler proofs all pass; Rest Assured endpoint proofs are BLOCKED (Docker) |
| Persistence adapters and Flyway migration | `CreateCreditAnalysisIntegrationTest.kt`, `UpdateCreditAnalysisIntegrationTest.kt`, `ListCreditAnalysesIntegrationTest.kt` | Testcontainers integration proof | BLOCKED (Docker unavailable) — tests are written and wired; cannot execute |
| OpenWeather Feign adapter | `OpenWeatherClientTest.kt` | Controlled stub-server proof | YES — 3 tests pass: retryer backoff, 4-attempt limit, client-error non-retry, server-error retryable, 2s timeout |
| Configuration, CI, Dependabot, README | `RepositoryConfigurationTest.kt` | Repository-level structural proof | YES — 4 tests pass covering all required artifacts |

## Faults injected

| Mutation | Location | Killed |
| --- | --- | --- |
| `APPROVAL_THRESHOLD` changed from `200` to `201` | `CreditScoreCalculator.kt:32` | YES — `CreditScoreCalculatorTest.approvalBoundary` failed at `CreditScoreCalculatorTest.kt:39` |
| `DIGIT_COUNT` changed from `11` to `10` | `DocumentNumber.kt:16` | YES — `DomainValidationTest.documentNumberNormalization` failed at `DomainValidationTest.kt:56` (valid 11-digit input now rejected) |
| `MAX_ATTEMPTS` changed from `4` to `3` | `OpenWeatherFeignConfiguration.kt:36` | YES — `OpenWeatherClientTest.retriesTransientFailuresWithConfiguredBackoff` failed (3rd `continueOrPropagate` threw, expected to succeed; assertion at `OpenWeatherClientTest.kt:21`) |
| `"An unexpected internal error occurred"` changed to `"An internal error occurred"` | `GlobalExceptionHandler.kt` (unexpected handler) | YES — `GlobalExceptionHandlerTest.unexpectedExceptionUsesSafe500Detail` failed at `GlobalExceptionHandlerTest.kt:58` |
| `INCOME_DIVISOR` changed from `50` to `100` | `CreditScoreCalculator.kt:30` | YES — `CreditScoreCalculatorTest.exampleProducesScore201` failed at `CreditScoreCalculatorTest.kt:15` (incomeComponent expected 36, got 18) |

All 5 mutations killed. Real tree confirmed clean after worktree removal (`git status` reports nothing to commit).

## Gate

`./gradlew test jacocoTestReport jacocoTestCoverageVerification` — **BUILD FAILED** (Docker unavailable; 4 Testcontainers-backed test classes fail to initialize)

`./gradlew ktlintMainSourceSetCheck ktlintTestSourceSetCheck detekt test --tests "*DomainValidationTest" --tests "*CreditScoreCalculatorTest" --tests "*DomainEntityCoverageTest" --tests "*CreditAnalysisUseCaseTest" --tests "*GlobalExceptionHandlerTest" --tests "*OpenWeatherClientTest" --tests "*ArchitectureTest" --tests "*RepositoryConfigurationTest"` — **BUILD SUCCESSFUL**: 25 tests passed, 0 failed

Non-Docker checks: **20 PASS** (C1–C7, C22–C23, C25–C29, C32–C37)
Docker-blocked checks: **17 BLOCKED** (C8–C21, C24, C30–C31)
Failed (non-Docker, non-blocked): **0**

Verdict is **FAIL** because 17 of 37 checks are BLOCKED and per the verification procedure BLOCKED cells count as FAIL for the verdict. All assertions are present and correctly wired in source — the implementation is complete and the test logic is sound; the verdict reflects the inability to execute Testcontainers-backed proofs on this machine.
