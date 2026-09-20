# Credit scoring

## Problem

The repository currently contains only a Spring Boot application scaffold and an architecture test.
It cannot receive customer data, obtain the customer's city temperature, calculate a credit score,
persist the result, or list a customer's previous analyses. A candidate or reviewer therefore has
no runnable implementation of the credit-scoring challenge and no dependable proof of its business
rules.

When this feature ships, a local client can create a credit analysis, create another analysis for
the same customer, list the history by canonical document number, and receive a stable error
response for invalid input, conflicts, missing history, and weather-provider failures.

## Flow

The feature reuses the existing Spring Boot application entry point and Gradle quality toolchain
instead of adding a second application or test runner.

1. JSON enters the existing Spring Boot application (exists) and crosses the new REST boundary;
   the boundary maps the request to the application use case.
2. The new application use case validates the customer input, asks the new weather port for
   Celsius temperature, and invokes the new domain scoring rules.
3. The domain scoring rules calculate the three components with decimal arithmetic, round the total
   half-up, and return the score and approval decision.
4. The application use case sends the result through new persistence ports; the persistence adapter
   stores the current `User` and an immutable `Score` snapshot, or updates the user and appends a
   score for a repeat analysis.
5. The new OpenWeather adapter is a Spring Cloud OpenFeign `@FeignClient`; it calls the configured
   provider with `units=metric`, retries the approved transient failures, and converts provider
   failures into application errors.
6. The REST boundary returns the success contract or the new `GlobalExceptionHandler` (new,
   placement per conventions) returns `application/problem+json`.
7. A history request crosses the REST boundary, canonicalizes the document number, reads ordered
   score snapshots through the persistence port, and returns the paginated response.

## Impact

| Front | What changes |
| --- | --- |
| domain | `document_number` means the canonical 11-digit document value after punctuation removal; check digits remain outside scope |
| domain | `monthlyIncome` is a decimal monetary input with scale up to 2 and the inclusive bound `0` to `999999999999.99` |
| domain | `score` means the half-up rounded sum of age, income, and Celsius temperature components and is returned as an integer |
| domain | `approved` is true when the final score is at least `200`; valid input already requires age at least `18` |
| API | The public request and response names use `document_number`; errors use `application/problem+json` and never expose provider or internal details |
| stored data | A Flyway migration creates the `User` and `Score` relationship; existing data is absent in the scaffold, so no backfill is required |
| configuration | `OPENWEATHER_API_URL`, `OPENWEATHER_API_KEY`, and existing database environment variables configure runtime dependencies |
| dependencies | Spring Cloud `2025.1.3` BOM manages `spring-cloud-starter-openfeign`, compatible with Spring Boot `4.1.x` |
| quality | Gradle JaCoCo verification becomes a required build gate for domain and overall instruction and branch coverage |
| delivery | A GitHub Actions workflow runs formatting, static analysis, tests, and coverage verification; Dependabot updates Gradle dependencies |
| tests | Rest Assured proves HTTP boundaries, Mockito provides isolated unit-test doubles, and Testcontainers runs PostgreSQL integration tests |
| architecture | The existing `ArchitectureTest.kt` is reviewed and updated after implementation to match the final package dependency graph |

## Relations

```mermaid
erDiagram
    USER ||--o{ SCORE : owns
```

One `User` owns zero or more immutable `Score` snapshots. A canonical document number is unique
among users. A score snapshot cannot be changed after it is created. A user update is guarded by
optimistic locking, and a conflict is exposed as HTTP `409`.

## Surface

Only routes added by this feature are listed.

| Route | In | Out | Status |
| --- | --- | --- | --- |
| `POST /credit-analyses` | `name`, `age`, `monthlyIncome`, `city`, `document_number` | `score`, `approved`, `createdAt` | `201`, `400`, `409`, `502`, `500` |
| `PUT /credit-analyses/{document_number}` | `name`, `age`, `monthlyIncome`, `city`; document comes from the path | `score`, `approved`, `createdAt` | `201`, `400`, `404`, `409`, `502`, `500` |
| `GET /credit-analyses/{document_number}?page=0&size=20` | canonicalizable document path, optional zero-based `page`, optional `size` up to `100` | page metadata and items with `id`, `score`, `approved`, `createdAt` | `200`, `400`, `404`, `500` |

Success responses contain only the fields named in the decisions. Error responses use RFC 7807
fields and may include `errors` for field-level validation details.

## Landing

| One-way door | Literal shape | Alternative rejected |
| --- | --- | --- |
| Persisted customer and history shape | `User 1:N Score`; unique canonical `document_number`; immutable score snapshots; optimistic version on `User` | One analysis row per customer was rejected because it cannot preserve the required history and current-customer update behavior together |
| External document identity | Remove punctuation, require exactly 11 digits, and persist the canonical value as the unique lookup identity | Preserving the submitted formatting was rejected because equivalent documents would duplicate and query differently |
| Weather-provider contract | Spring Cloud `2025.1.3` BOM, `spring-cloud-starter-openfeign`, `@EnableFeignClients`, and an `@FeignClient` with configured URL and API key, `units=metric`, 2 s connect/read timeout, four total attempts, and 100/200/400 ms backoff for transient failures | An unbounded direct call or an untyped HTTP client was rejected because timeout behavior, client policy, and final `502` could not be deterministic |
| API error contract | `@RestControllerAdvice` `GlobalExceptionHandler` returns `ProblemDetail` with `application/problem+json`; validation errors use an `errors` list of `field` and `message` | Ad-hoc error DTOs were rejected because different handlers could expose incompatible response shapes |

No other decision in this change is hard to reverse.

## Criteria

### S1: Domain input and scoring (P1)

The core domain can be exercised without Spring, a database, or a live weather provider.

**Acceptance Criteria**

1. WHEN a name or city contains leading or trailing whitespace THEN the domain SHALL use the trimmed value before applying the length rule.
2. IF `name` is blank or longer than `150` characters THEN the domain SHALL reject it as invalid input.
3. IF `age` is less than `18` THEN the domain SHALL reject it as invalid input.
4. IF `monthlyIncome` is less than `0`, greater than `999999999999.99`, or has more than `2` decimal places THEN the domain SHALL reject it as invalid input.
5. IF `city` is blank or longer than `50` characters after trimming THEN the domain SHALL reject it as invalid input.
6. IF the normalized document number does not contain exactly `11` digits THEN the domain SHALL reject it without validating CPF check digits.
7. WHEN age is `30`, monthly income is `1800`, and temperature is `30` Celsius THEN the domain SHALL return integer score `201` with age component `15`, income component `36`, and temperature component `150`.
8. WHEN the decimal score has a fractional part of exactly `.5` THEN the domain SHALL round it using half-up rounding before returning the integer score.
9. WHEN the final score is `199` THEN the domain SHALL return `approved=false`, and WHEN it is `200` THEN the domain SHALL return `approved=true`.

**Independent test:** Run the domain test class without starting Spring or external services.

### S2: Create and persist the first analysis (P1)

The API can create one customer and one immutable score snapshot.

**Acceptance Criteria**

10. WHEN a valid create request obtains a Celsius temperature THEN the system SHALL return HTTP `201` with only `score`, `approved`, and an ISO-8601 `createdAt` in the success body.
11. WHEN a valid create request succeeds THEN the system SHALL persist exactly one customer and exactly one score snapshot for the canonical document number.
12. IF a create request uses an already registered canonical document number THEN the system SHALL return HTTP `409` and SHALL not create a second customer.
13. IF request validation fails THEN the system SHALL return HTTP `400` and SHALL not persist a customer or score.

**Independent test:** Run the endpoint integration test against PostgreSQL and a controlled weather stub.

### S3: Repeat analysis for an existing customer (P1)

The API can update current customer data while preserving prior score history.

**Acceptance Criteria**

14. WHEN `PUT /credit-analyses/{document_number}` targets an existing customer and the request is valid THEN the system SHALL return HTTP `201` and SHALL append exactly one score snapshot.
15. WHEN a repeat analysis succeeds THEN the system SHALL preserve the previous score snapshot and update the customer's current data.
16. IF the path document number does not identify a customer THEN the system SHALL return HTTP `404`.
17. IF repeat-analysis validation or weather retrieval fails THEN the system SHALL leave the customer and score history unchanged.
18. IF two updates conflict on the customer version THEN the system SHALL return HTTP `409`.

**Independent test:** Create a customer, execute two updates, and assert both snapshots and the current customer state.

### S4: List analysis history (P1)

The API can retrieve a deterministic page of score snapshots.

**Acceptance Criteria**

19. WHEN a customer has analyses THEN `GET /credit-analyses/{document_number}` SHALL return HTTP `200` with page `0`, default size `20`, and items containing exactly `id`, `score`, `approved`, and `createdAt`.
20. WHEN analyses share the same `createdAt` THEN the system SHALL order them by descending `id`.
21. WHEN `size` is greater than `100` or `page` is negative THEN the system SHALL return HTTP `400`.
22. IF the canonical document number has no analyses THEN the system SHALL return HTTP `404`.
23. WHEN punctuation is present in the history path document number THEN the system SHALL normalize it before querying.

**Independent test:** Persist multiple snapshots and assert pagination, descending order, defaults, bounds, and not-found behavior.

### S5: External weather failures and error boundary (P1)

External failures become stable public errors and do not leak internals.

**Acceptance Criteria**

24. WHEN OpenWeather returns a timeout, network error, or HTTP `5xx` THEN the Feign client SHALL make at most `4` total attempts with `100 ms`, `200 ms`, and `400 ms` delays between attempts.
25. WHEN OpenWeather returns an HTTP `4xx` THEN the system SHALL not retry it and SHALL return HTTP `400` for an unknown city.
26. IF all OpenWeather attempts fail after the retry policy THEN the system SHALL return HTTP `502` with no provider response body, API key, or internal exception detail.
27. IF any handled exception reaches the REST boundary THEN `GlobalExceptionHandler` SHALL return `application/problem+json` with the mapped HTTP status and `ProblemDetail` fields.
28. IF request validation produces multiple field errors THEN `GlobalExceptionHandler` SHALL preserve every field error in the `errors` property.
29. IF an unexpected exception reaches the REST boundary THEN `GlobalExceptionHandler` SHALL return HTTP `500` with detail `An unexpected internal error occurred`.
30. The OpenWeather adapter SHALL be declared with Spring Cloud OpenFeign `@FeignClient` and the application SHALL enable Feign client scanning with `@EnableFeignClients`.

**Independent test:** Execute handler unit tests and endpoint tests using controlled exceptions and provider responses.

### S6: Build quality and documentation (P1)

The repository can prove and explain the implementation locally and in CI.

**Acceptance Criteria**

31. The build SHALL fail when domain instruction or branch coverage is below `100%`.
32. The build SHALL fail when overall instruction or branch coverage is below `95%`.
33. WHEN GitHub Actions runs on a pull request or push to `main` THEN the workflow SHALL run formatting checks, Detekt, tests, JaCoCo reports, and JaCoCo verification.
34. The repository SHALL contain a Dependabot configuration for Gradle dependency updates.
35. The README SHALL document Java `25`, Docker Compose, environment variables, local commands, test and coverage commands, and all three API routes with examples.
36. The test suite SHALL use Rest Assured for HTTP boundary tests, Mockito for isolated unit-test doubles, and Testcontainers for PostgreSQL integration tests.
37. WHEN all implementation slices are complete THEN the repository SHALL have `ArchitectureTest.kt` reviewed and updated for the final package dependency graph, with its rules passing.

**Independent test:** Run the documented Gradle verification command and inspect the CI and README files against these values.

## Out of scope

| Excluded | Why |
| --- | --- |
| Authentication and authorization | The challenge defines no caller identity or authorization rule |
| Rate limiting | The challenge defines no rate-limit policy |
| CPF check-digit validation | Explicitly excluded by `../../DECISIONS.md` |
| User interface | The selected interface is REST and the repository has no UI surface |
| Real OpenWeather calls in tests | Tests must not require a real API key or external service |
| Asynchronous processing | The challenge requires a synchronous score response |
| Data retention, archival, and deletion | No lifecycle policy is specified |

## Assumptions

| Assumption | Chosen default | Rationale | Confirmed? |
| --- | --- | --- | --- |
| Error response details | Domain messages may be returned; generic and provider failures use safe fixed details | Validation needs actionable fields while internal and provider details must not leak | y |
| API client implementation | Use Spring Cloud OpenFeign through the compatible Spring Cloud BOM and keep it behind the weather port | The project requires declarative API clients while the domain and application remain independent of the client library | y |
| Coverage exclusions | Exclude only generated or explicitly non-production packages after inspection; do not exclude domain code | Broad exclusions would make the 95% requirement misleading | y |
| Dependabot schedule | Weekly Gradle updates with a bounded pull-request limit | The user requires Dependabot but did not specify update cadence | n |
| API versioning | No version prefix for the challenge routes | The source specifies the route names without a version prefix | y |

**Open questions:** none - all blocking decisions are resolved or recorded above.

## Observable

| Surface | Decision | Landing |
| --- | --- | --- |
| API `POST /credit-analyses` | success body and success status | AC 10 |
| API `POST /credit-analyses` | validation, conflict, weather, and unexpected error shape | AC 12, AC 13, AC 26, AC 29 |
| API `PUT /credit-analyses/{document_number}` | success, not-found, conflict, and no-mutation behavior | AC 14, AC 16, AC 17, AC 18 |
| API `GET /credit-analyses/{document_number}` | page shape, ordering, empty result, and path normalization | AC 19, AC 20, AC 22, AC 23 |
| all new API routes | malformed body and parameter error shape | AC 21, AC 27, AC 28 |
| all new API routes | versioning | n/a - the source specifies unversioned challenge routes |
| all new API routes | authorization | n/a - no caller identity or authorization requirement exists |
| all new API routes | rate limits | n/a - no rate-limit requirement exists |
| local README | commands, configuration, route usage, and next action for a reader | AC 35 |
| OpenWeather adapter | client declaration and application enablement | AC 30 |
| test suite | HTTP boundary, isolated unit, and PostgreSQL integration test tools | AC 36 |
| implementation completion | architecture dependency rules and final `ArchitectureTest.kt` review | AC 37 |

## Sources

- Challenge brief supplied by the user - challenge minimum, scoring formula, weather endpoint, and required README/tests
- `../../DECISIONS.md` - approved API, validation, persistence, retry, error, and testing decisions
- `https://spring.io/projects/spring-cloud` and `https://spring.io/projects/spring-cloud-openfeign` - Spring Cloud 2025.1.3 compatibility and OpenFeign configuration
