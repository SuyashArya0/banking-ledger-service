# High-Concurrency Banking Ledger Service

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.x](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20%2F%20DDD-blue.svg)](#architecture)
[![License](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](LICENSE)

A production-grade, double-entry financial ledger service built with **Java 21**, **Spring Boot 3.x**, and **PostgreSQL 16**. The system enforces strict double-entry accounting invariants, prevents database deadlocks under high concurrency using deterministic lock ordering, and offers real-time observability via Micrometer and Prometheus.

---

## Architecture Overview

The system adheres strictly to **Hexagonal Architecture (Ports and Adapters)** and **Domain-Driven Design (DDD)** to decouple core business logic from infrastructure, storage, and frameworks.

```text
               +---------------------------------------------------+
               |                    PRIMARY                        |
               |  HTTP Controllers / OpenAPI  (Web Inbound Adapter)|
               +-------------------------+-------------------------+
                                         |
                                         v
               +---------------------------------------------------+
               |               APPLICATION LAYER                   |
               |           TransferMoneyUseCase (In Port)         |
               |          TransferMoneyService (Use Case)          |
               +------------+------------------------+-------------+
                            |                        |
                            v                        v
+------------------------------------+      +----------------------------------+
|           DOMAIN LAYER             |      |         SECONDARY PORTS          |
|  - Account & Transaction Aggregates|      |  - LoadAccountPort / SaveAccountPort|
|  - Money Value Object (Invariants) |      |  - SaveTransactionPort           |
|  - Domain Exception Hierarchy      |      |  - PublishTransactionEventPort   |
+------------------------------------+      |  - RecordMetricsPort             |
                                            +----------------+-----------------+
                                                             |
                                                             v
               +---------------------------------------------------+
               |                    SECONDARY                      |
               |  - JPA/Postgres Persistence (Outbound Adapter)    |
               |  - Kafka / Spring Application Event Publisher     |
               |  - Micrometer Prometheus Metrics Adapter          |
               +---------------------------------------------------+
```

---

## Key Features & Implementation Phases

### Phase 1: Core Domain Modeling & Double-Entry Invariants

- Immutable Value Objects: Strict `Money` value objects handling precision calculations and currency alignment.
- Domain Invariants: Accounts enforce balance checks, anti-negative debits, and self-transfer prohibitions at the domain layer.
- Double-Entry Operations: Debits from source accounts and credits to target accounts are managed atomically within domain boundaries.

### Phase 2: High-Concurrency & Deadlock Prevention

- Deterministic Lock Ordering: Prevents database deadlocks when concurrent threads perform cross-transfers between identical account pairs by sorting account UUID locks deterministically:

```java
UUID firstLockId = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
UUID secondLockId = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;
```

- Pessimistic Locking: Executes `SELECT ... FOR UPDATE` via `LoadAccountPort.loadAccountForUpdate()` to isolate concurrent state modifications.
- Idempotency Verification: Validates unique reference keys(`referenceId`) before processing to prevent duplicate financial execution.

### Phase 3: Observability & Messaging Adapters

- Micrometer & Prometheus: Custom `LedgerMetricsAdapter` captures completion counters(`ledger.transfers.completed`), failure metrics, and monetary trabsfer distribution summaries by currency.
- Event-Driven Decoupling: Emits `TransactionCompletedEvent` records via `PublishTransactionEventPort` for event listeners or streaming message brokers(Kafka/RabbitMQ).

### Phase 4: Containerization & DevOps

- Multi-Stage Docker Build: Utilizes Spring Boot layer extraction(`layertools`) inside Docker to maximise layer caching and minimise runtime container footprints.
- Non-Root Runtime Security: Executes container processes under a dedicated `ledgeruser` non-root Linux user account.
- Docker Compose Stack: Integrated PostgreSQL 16 database instance and pre-configured Prometheus monitoring daemon.

---

## Getting Started

### Prerequisites

- Java 21 SDK or higher
- Maven 3.9+
- Docker & Docker Compose

### Running the Infrastructure

Start PostgreSQL and Prometheus containers locally:

```bash
docker-compose up -d postgres prometheus
```

### Building & Running the Application

Build the project and execute all unit and integration test suites:

```bash
mvn clean verify
```

Run the application locally:

```bash
mvn spring-boot:run
```

---

## Endpoints & Verification

- Swagger UI/OpenAPI Documentation:<br>`http://localhost:8080/swagger-ui.html`
- Prometheus Metrics Scrape Target:<br>`http://localhost:8080/actuator/prometheus`
- Health Endpoint:<br>`http://localhost:8080/actuator/health`

### Sample Transfer Request

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "referenceId": "REF-TX-9901",
    "sourceAccountId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "targetAccountId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "amount": 250.00,
    "currency": "USD"
  }'
```

---

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0). You are free to share, adapt, and build upon this material for non-commercial purposes, provided appropriate attribution is given. Commercial use or monetization is strictly prohibited.