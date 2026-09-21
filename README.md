# Blipay Credit Scoring

Spring Boot and Kotlin implementation of the credit-scoring challenge.

## Stack

- Java 25
- Kotlin 2.4.20
- Spring Boot 4.1.1
- PostgreSQL with Flyway
- Docker and Docker Compose
- Hexagonal Architecture
- JUnit 5, Mockito, Rest Assured, Testcontainers, and ArchUnit

## Decisions

- Credit analyses are persisted in PostgreSQL.
- OpenWeather is queried with metric units and returns Celsius.
- Fractional scores are rounded half up to an integer.
- The API supports create, repeat analysis, and history routes.

## Run locally

Prerequisites: Java 25, Docker, and Docker Compose V2.

```bash
cat > .env <<'EOF'
OPENWEATHER_API_URL=https://api.openweathermap.org/data/2.5
OPENWEATHER_API_KEY=your-key
DB_URL=jdbc:postgresql://localhost:5432/credit_scoring
DB_USERNAME=postgres
DB_PASSWORD=postgres
EOF
```

Docker Compose loads `.env` automatically. Start the stack with:

```bash
docker compose up --build
```

The `.env` file is ignored by Git.

The PostgreSQL database is available at `localhost:5432`.

## Verify

```bash
./gradlew ktlintMainSourceSetFormat ktlintTestSourceSetFormat
./gradlew ktlintMainSourceSetCheck ktlintTestSourceSetCheck
./gradlew detekt
./gradlew test
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

## API

`POST /credit-analyses` creates the first analysis:

```bash
curl -X POST http://localhost:8080/credit-analyses \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria","age":30,"monthlyIncome":1800,"city":"Recife","document_number":"123.456.789-09"}'
```

`PUT /credit-analyses/{document_number}` creates another analysis for the same customer:

```bash
curl -X PUT http://localhost:8080/credit-analyses/12345678909 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria","age":31,"monthlyIncome":2000,"city":"Olinda"}'
```

`GET /credit-analyses/{document_number}` lists history with zero-based pagination:

```bash
curl 'http://localhost:8080/credit-analyses/123.456.789-09?page=0&size=20'
```

All errors use `application/problem+json`. OpenWeather timeouts, network errors, and server errors
are retried up to four total attempts before returning `502`.

The feature packages follow this dependency direction:

```text
infra -> application -> domain
```

- Controllers and external adapters belong under `infra`.
- Use cases belong under `application`.
- Business rules and ports belong under `domain`.
