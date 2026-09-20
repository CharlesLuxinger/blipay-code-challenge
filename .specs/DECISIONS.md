# Credit Scoring Decisions

## Scope

- The application receives customer data, retrieves the city's temperature, and calculates a credit score.
- The goal is to keep the implementation concise, testable, and runnable locally.

## Stack and architecture

- Kotlin with Spring Boot.
- REST API.
- Hexagonal architecture:
  - `domain`: business rules and ports.
  - `application`: use cases.
  - `infra`: controllers, persistence, and OpenWeather.
- PostgreSQL with Flyway.

## Input data

The POST request receives JSON with:

- `name`: required, trimmed, and up to 150 characters.
- `age`: required integer greater than or equal to 18.
- `monthlyIncome`: decimal JSON number between `0` and `999999999999.99`, with up to two decimal places.
- `city`: required, trimmed, and up to 50 characters.
- `document_number`: required; punctuation is removed and the canonical value must contain 11 digits. Check digits are not validated.

## Score calculation

```text
age component = age * 0.5
income component = (monthlyIncome / 100) * 2
temperature component = temperature * 5
decimal score = sum of the components
score = decimal score rounded using half-up
```

- The final score is an integer.
- Credit is approved when the score is greater than or equal to 200.
- Calculations use `BigDecimal` to avoid floating-point errors.
- The temperature is requested from OpenWeather with `units=metric` and used in Celsius.

## OpenWeather

- The endpoint is configured through the `.env` variable`OPENWEATHER_API_URL` and API key is read from `OPENWEATHER_API_KEY`.
- The application makes up to four total attempts for timeouts, network errors, and HTTP 5xx responses.
- The backoff delays are 100 ms, 200 ms, and 400 ms.
- Each attempt uses a 2-second connect timeout and a 2-second read timeout.
- 4xx errors are not retried.
- An unknown city is treated as an input error (`400`).
- After all attempts fail, the API returns `502` without internal details.
- The application does not persist an analysis when it cannot obtain the temperature.

## API

### Create an analysis

```text
POST /credit-analyses
```

- POST creates the customer record and its first analysis.
- The document number must be unique.
- Attempting to create a customer with an existing document number returns
  `409`.
- A successful execution returns `201`.
- Validation or OpenWeather failures do not create a customer or analysis.
- The success body contains only:

```json
{
  "score": 201,
  "approved": true,
  "createdAt": "2024-06-01T12:34:56Z"
}
```

### Create a new analysis for an existing customer

```text
PUT /credit-analyses/{document_number}
```

- The document number in the URL identifies an existing customer.
- The body receives `name`, `age`, `monthlyIncome`, and `city`.
- The endpoint updates the customer's current data and creates a new `Score` snapshot.
- The document number cannot be changed in the request body.
- An unknown document number returns `404`.
- Each successful execution creates a new analysis and returns `201` with the same success body as POST.
- Validation or OpenWeather failures do not change the customer or create a score.

### List analyses

```text
GET /credit-analyses/{document_number}?page=0&size=20
```

- `page` is zero-based and defaults to `0`.
- `size` defaults to `20` and has a maximum of `100`.
- Results are sorted by `createdAt desc`, with `id desc` as the tie-breaker.
- The response is paginated, and each item contains `id`, `score`, `approved`, and `createdAt`.
- A document number with no analyses returns `404`.
- The document number in the URL is normalized before the query.

### Errors

- Errors use `application/problem+json`.
- `400`: input validation or unknown city.
- `404`: document number with no analyses.
- `409`: document number already registered on POST or concurrency conflict.
- `502`: final OpenWeather failure.
- `500`: unexpected application failure.

## Persistence

- The relationship is `User` 1:N `Score`.
- `User` has a UUID, a unique canonical document number, the customer's current data, `createdAt`, `updatedAt`, and a version for optimistic locking.
- `Score` has a Long ID, references `User`, and stores an immutable snapshot of all data used in the calculation:
  - Document number, name, age, income, and city.
  - Temperature and score components.
  - Final score, approval status, and `createdAt`.
- POST creates the `User` and its first `Score`.
- PUT for an existing document number updates the current `User` data and creates a new `Score`.
- Optimistic locking applies to `User`. The request does not require a version; concurrency conflicts are returned as `409`.

## Tests

- Unit tests are required.
- The domain tests cover validation, calculation, rounding, and approval.
- Integration tests cover endpoints, persistence, migrations, and errors.
- OpenWeather integration is replaced with a controlled stub server.
- Tests do not depend on a real external API or a real API key.

## Documentation

- The README explains the commands to run the application, execute tests, and use the endpoints.
- This file records the product and implementation decisions that guide development.
