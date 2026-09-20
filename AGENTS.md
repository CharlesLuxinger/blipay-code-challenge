# Agent Guidance

## tlc-spec-lean

profile: standard
budget: 150k

The project uses the Plan -> Checks -> Build -> Verify flow. The full binding product decisions  are recorded in `.specs/DECISIONS.md`. Do not weaken approved checks, delete tests, or skip proofs to make a suite pass.

## Coverage policy

- JaCoCo instruction/code coverage for `..domain..` must be 100%.
- JaCoCo branch coverage for `..domain..` must be 100%.
- Overall JaCoCo instruction/code coverage must be at least 95%.
- Overall JaCoCo branch coverage must be at least 95%.

## Commit policy

- Each implementation slice must be committed separately using a semantic Conventional Commit message in the form `<type>(<scope>): <description>`.
- Commit messages must use imperative mood, lowercase descriptions, and no trailing period.
- Before every commit, request explicit human validation of the staged slice and wait for approval.

## Kotlin file policy

- Keep Kotlin files small and focused on one responsibility.
- Do not aggregate unrelated domain, application, API, persistence, client, or error-handling responsibilities in one file.

## API client policy

- API clients must use Spring Cloud OpenFeign `@FeignClient` rather than a different HTTP client library.
- Manage OpenFeign through the Spring Cloud BOM compatible with the pinned Spring Boot version.

## Test policy

- Use Rest Assured for HTTP boundary and endpoint tests.
- Use Mockito for isolated unit-test doubles where a dependency must be replaced.
- Use Testcontainers for PostgreSQL integration tests, migrations, and persistence behavior.
- Review `ArchitectureTest.kt` after the implementation is complete and update its rules for the final package dependency graph.

The independent verifier is mandatory after the last feature commit. Remote operations, deploys,
production data changes, and pushes require explicit approval.
