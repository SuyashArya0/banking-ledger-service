# Banking Ledger Service

A double-entry banking transaction and ledger microservice built with **Java 21**, **Spring Boot 3.2**, **PostgreSQL** and **virtual threads**. It is structured as a **hexagonal (ports and adapters)** application so the business rules stay independent of Spring, JPA and the web layer.

## Features

- **Money transfers** between accounts through a REST API (`POST /api/v1/transfers`)
- **Double-entry bookkeeping** – every transfer produces a balanced `DEBIT` and `CREDIT` journal entry
- **Immutable `Money` value object** – `BigDecimal` with banker's rounding (`HALF_EVEN`) and strict currency matching
- **Idempotency** – each transfer carries a unique `referenceId`; a repeated request returns the original result and does not debit twice
- **Deadlock prevention** – accounts are locked with pessimistic locks (`SELECT ... FOR UPDATE`) in a deterministic order by account ID
- **Virtual threads** – enabled with `spring.threads.virtual.enabled=true`
- **Consistent error responses** – RFC 7807 `ProblemDetail` for validation, not-found, duplicate and insufficient-funds errors
- **Database migrations** with Flyway
- **API docs** – Swagger UI via Springdoc OpenAPI
- **Observability** – Spring Boot Actuator, Micrometer and Prometheus, with custom transfer counters and volume metrics
- **Domain events** – a `TransactionCompletedEvent` is published through an output port (see [Events](#events))

## Architecture

```text
src/main/java/com/bank/ledger
├── domain                      # Pure business logic, no Spring imports
│   ├── model                   # Account, Transaction, JournalEntry, Money, EntryType, TransactionStatus
│   ├── exception               # DomainException and its subclasses
│   └── service                 # TransferDomainService
├── application
│   ├── port
│   │   ├── in                  # TransferMoneyUseCase, GetAccountBalanceQuery, TransferCommand, TransferResult
│   │   └── out                 # LoadAccountPort, SaveAccountPort, SaveTransactionPort, PublishTransactionEventPort
│   └── usecase                 # TransferMoneyService
└── infrastructure
    ├── adapter
    │   ├── in/web              # TransferController, DTOs, GlobalExceptionHandler
    │   └── out
    │       ├── persistence     # JPA entities, mappers, repositories, persistence adapters
    │       ├── messaging       # Transaction event adapter
    │       └── metrics         # LedgerMetricsAdapter
    └── config                  # JacksonConfig, OpenApiConfig
```

Dependencies point inwards: infrastructure depends on application, and application depends on domain. The domain never depends on either.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.2.x (Web, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16, Flyway |
| API docs | Springdoc OpenAPI 2.5.0 |
| Metrics | Micrometer, Prometheus |
| Testing | JUnit 5, Spring Boot Test, Testcontainers |
| Packaging | Maven, Docker, Docker Compose |

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.9+
- Docker and Docker Compose (also required to run the integration tests)

### Run with Docker Compose

```bash
git clone https://github.com/SuyashArya0/banking-ledger-service.git
cd banking-ledger-service

docker-compose up --build -d
docker-compose ps
```

This starts PostgreSQL, Prometheus and the ledger service.

| Service | URL |
| --- | --- |
| Ledger API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Prometheus metrics | http://localhost:8080/actuator/prometheus |
| Prometheus UI | http://localhost:9090 |

View logs:

```bash
docker-compose logs -f ledger-service
```

### Configuration

The service reads its database settings from standard Spring environment variables:

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL, e.g. `jdbc:postgresql://postgres:5432/banking_db` |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_THREADS_VIRTUAL_ENABLED` | Set to `"true"` to run request handling on virtual threads |

The credentials in `docker-compose.yml` are for local development only. Change them before deploying anywhere else.

## API

### Execute a transfer

`POST /api/v1/transfers`

```json
{
  "referenceId": "TX-902184029-A",
  "sourceAccountId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "targetAccountId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
  "amount": 250.00,
  "currency": "USD"
}
```

**201 Created**

```json
{
  "transactionId": "f3b3c3c3-8a8a-4a4a-9a9a-0a0a0a0a0a0a",
  "referenceId": "TX-902184029-A",
  "sourceAccountId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "targetAccountId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
  "amount": 250.00,
  "currency": "USD",
  "status": "COMPLETED",
  "timestamp": "2026-10-05T10:15:30Z"
}
```

Example with cURL:

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "referenceId": "TX-VERIFY-001",
    "sourceAccountId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "targetAccountId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "amount": 150.00,
    "currency": "USD"
  }'
```

The accounts must already exist in the database.

### Errors

Errors are returned as `ProblemDetail` (RFC 7807) responses.

| Status | Cause |
| --- | --- |
| `400 Bad Request` | Validation failure, invalid argument or currency mismatch |
| `404 Not Found` | Source or target account does not exist |
| `409 Conflict` | Duplicate transaction |
| `422 Unprocessable Entity` | Insufficient funds in the source account |

Example (`422`):

```json
{
  "type": "https://bank.com/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Account ACC-001 has insufficient balance (100.00) for debit of 250.00",
  "timestamp": "2026-10-05T10:15:30Z"
}
```

### Idempotency

Send a unique `referenceId` with every transfer. If the same `referenceId` is submitted again, the service returns the original transaction and does not move money a second time. A unique database constraint on `reference_id` backs this up.

## How a transfer works

1. Look up the `referenceId`; if it already exists, return the stored result.
2. Load both accounts with pessimistic locks, always in ascending ID order, so two opposite transfers cannot deadlock.
3. Create the `Transaction` with its balanced debit and credit entries.
4. Debit the source and credit the target, then persist both accounts.
5. Mark the transaction `COMPLETED` and save it. On failure, mark it `FAILED` and rethrow.

## Observability

Actuator exposes `health`, `metrics` and `prometheus`. Custom metrics:

| Metric | Description |
| --- | --- |
| `ledger_transfers_completed_total` | Number of successful transfers |
| `ledger_transfers_failed_total` | Number of failed transfers |
| `ledger_transfers_volume` | Transfer amounts, tagged by currency |

Prometheus scrapes `ledger-service:8080/actuator/prometheus` every 5 seconds using `prometheus.yml` in the project root.

## Events

After a transfer completes, `TransferMoneyService` publishes a `TransactionCompletedEvent` through the `PublishTransactionEventPort`. The current adapter emits it as a Spring application event. Because it sits behind a port, it can be replaced with a Kafka or RabbitMQ publisher without touching the domain or use-case code.

## Testing

```bash
mvn clean verify
```

The integration tests start a real PostgreSQL 16 container with Testcontainers, so Docker must be running. They cover:

- a successful transfer and the resulting balances
- idempotent handling of a repeated `referenceId`
- balance consistency under 50 concurrent transfers on virtual threads

## Contributing

Contributions are welcome. Fork the repo, create a feature branch and open a pull request. Please keep the `domain` package free of Spring and JPA imports, and add tests for any change to transfer logic.

## License

Released under the [MIT License](LICENSE).
