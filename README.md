# Blipay Credit Scoring

Spring Boot and Kotlin scaffold for the credit-scoring challenge.

## Stack

- Java 25
- Kotlin 2.4.20
- Spring Boot 4.1.1
- PostgreSQL with Flyway
- Docker and Docker Compose
- Hexagonal architecture
- JUnit 5, Mockito, Rest Assured, Testcontainers, and ArchUnit

## Decisions

- Credit analyses are persisted in PostgreSQL.
- Temperature is converted from OpenWeather Kelvin to Celsius.
- Fractional scores are rounded half up to an integer.
- The planned API is `POST /credit-analyses` and `GET /credit-analyses/{cpf}`.
- `OPENWEATHER_API_KEY` is required for the weather adapter.

## Run locally

Prerequisites: Java 25, Docker, and Docker Compose V2.

```bash
cat > .env <<'EOF'
OPENWEATHER_API_KEY=your-key
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
```

The feature packages follow this dependency direction:

```text
infra -> application -> domain
```

Controllers and external adapters belong under `infra`. Use cases belong under
`application`. Business rules and ports belong under `domain`.
