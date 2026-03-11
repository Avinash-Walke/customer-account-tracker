# Customer Account Tracker

A production-ready Spring Boot microservice for managing customers and bank accounts. This document is an operational and integration contract for developers and operators.

Table of contents
- Overview
- Quick Start (Dev)
- API Contract (inputs / outputs / error model)
- API Endpoints
  - Account Endpoints
    - Create Account
    - List Accounts
    - Transfer Funds
    - Get Account Balance / Details
    - Deactivate Account
  - Customer Endpoints
    - Get all Customers 
    - Get Customer by ID
    - Update exiting customer details
- Error model & exception mappings
- Tests
- Observability & Security
- Contribution

---

Overview

Customer Account Tracker provides APIs to create and manage customers and bank accounts, perform transfers, query balances, and deactivate accounts. The project is layered (controller → service → repository) and uses DTOs and MapStruct for mapping. By default it runs with an embedded H2 database and includes seed data for local development.

---

Quick Start (Dev)

Prerequisites
- Java 17+
- Maven 3.6+
- Docker (optional, for running the container)

Clone and build:

```powershell
# git clone project-repo-url to your local machine
git clone https://github.com/Avinash-Walke/customer-account-tracker.git
```

```powershell
# from project root
mvn clean package
```

Run locally (JAR):

```powershell
java -jar target/customer-account-tracker.jar
```

Default port and overrides
- Default HTTP port: 8080 (Spring Boot default; `application.yml` does not override it)
- To change the port: `java -jar target/customer-account-tracker.jar --server.port=8182` or `-Dserver.port=8182`.

Run with Maven (dev)

```powershell
mvn spring-boot:run
```

H2 Console (dev)
- The project enables H2 console for the embedded H2 DB. When running locally, open:
  - http://localhost:8080/h2-console
- JDBC URL (embedded H2): `jdbc:h2:mem:bankdb`
- Username: `sa`, Password: (empty)

- Add `Dockerfile` to repo (example below). Build and run image in CI/CD pipelines.

Example Dockerfile
```dockerfile
FROM eclipse-temurin:17-jdk
ARG JAR_FILE=target/customer-account-tracker.jar
COPY ${JAR_FILE} /app/customer-account-tracker.jar
ENTRYPOINT ["java","-jar","/app/customer-account-tracker.jar"]
```
Run with Docker The repository includes a Dockerfile which expects the built JAR at **target/customer-account-tracker-1.0-SNAPSHOT.jar**

1) Build the Docker image (from project root):

```powershell
docker build -t customer-account-tracker:latest .
```

2) Run the container and map port 8182 to host:8182

```powershell
docker run -p 8080:8080 customer-account-tracker
```

Notes
- The provided `import.sql` seeds H2 only for the embedded profile, when running with an external DB, use Flyway or Liquibase migrations and remove `import.sql` for non-dev profiles.

---

API Contract (inputs / outputs / error model)

Base path: `/v1/api`
- Host: `localhost:8080` (dev)
- Content-Type: `application/json`
- Primary input DTOs: `CreateAccountRequest`, `TransferRequest`, `DeactivateAccountRequest`, `CustomerUpdateRequest` (see `src/main/java/.../dto`).
- Primary output DTOs: `CreateAccountResponse`, `TransferResponse`, `AccountsResponse`, `AccountDto`, `DeactivateAccountResponse`, `CustomerResponse`.
- Error payload: `ErrorDetails` JSON with fields: `traceId`, `status`, `message`, `timestamp`.

Validation
- Inputs validated using Jakarta Validation (annotations on DTOs). Validation failures return 400 with a descriptive message.

API Docs (Swagger / OpenAPI)

The project already includes `springdoc-openapi-starter-webmvc-ui` in `pom.xml`. When the application runs (dev), the OpenAPI and Swagger UI are available at the following endpoints:

- OpenAPI JSON (raw): http://localhost:8080/v3/api-docs
- Swagger UI (interactive): http://localhost:8080/swagger-ui.html or http://localhost:8080/swagger-ui/index.html

---

API Endpoints

All requests/responses are JSON unless the request is an empty GET. Each endpoint includes a representative Request block and the main success Response block (with HTTP status). Error cases are summarized after each endpoint.

A. **Account Endpoints**

1) Create Account
- POST /v1/api/account

Request
```http
POST /v1/api/account HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "firstName": "Alice",
  "lastName": "Johnson",
  "email": "alice.johnson@example.com",
  "mobileNumber": "5551234567",
  "address": "123 Main St",
  "accountType": "SAVING_INDIVIDUAL",
  "openingBalance": 1000.00,
  "currency": "USD",
  "dob": "1990-07-15",
  "bankName": "HDFC"
}
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "NEW ACCOUNT HAS BEEN CREATED ",
  "accountNumber": 1000001
}
```

Errors
- 400 Bad Request — validation failure
- 409 Conflict — `AccountAlreadyExistsException` when active account in same bank exists

---

2) List Accounts
- GET /v1/api/accounts
- Success: 200 OK (AccountsResponse)

Request
```http
GET /v1/api/accounts HTTP/1.1
Host: localhost:8080
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "accounts": [
      {
        "accountNumber": 1,
        "accountType": "SAVING_INDIVIDUAL",
        "balance": 300000.00,
        "currency": "INR",
        "customer": {
            "id": 1,
            "firstName": "Tony",
            "lastName": "Stark",
            "email": "tony@example.com",
            "mobileNumber": "9000000002",
            "address": "Malibu",
            "dob": "1994-05-29"
        },
        "accountOpeningDate": "18 Jan 2021, 10:32 am",
        "accountActive": true,
        "bankName": "HDFC",
        "mobileNumber": "9000000002"
      }
   ]
}
```

Errors
- 500 Internal Server Error — unexpected runtime error

---

3) Transfer Funds
- POST /v1/api/accounts/transfer
- Success: 200 OK (TransferResponse)

Request
```http
POST /v1/api/accounts/transfer HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "from": 1000001,
  "to": 1000002,
  "amount": 250.00
}
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "SUCCESS",
  "message": "Your transfer of INR 250.00 to John Doe has been successfully completed on ..."
}
```

Errors
- 400 Bad Request — `InsufficientBalanceException`, `IllegalArgumentException`, `AccountNotFoundException` (mapped to 400 in this project)

---

4) Get Account Balance / Details
- GET /v1/api/accounts/{accountNumber}/balance
- Success: 200 OK (AccountDto)

Request
```http
GET /v1/api/accounts/1000001/balance HTTP/1.1
Host: localhost:8080
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{ /* AccountDto JSON */ }
```

Errors
- 400 Bad Request — `AccountNotFoundException`

---

5) Deactivate Account
- POST /v1/api/accounts/{accountNumber}/deactivate
- Success: 200 OK (DeactivateAccountResponse)

Request
```http
POST /v1/api/accounts/12/deactivate HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "message": "Customer requested closure",
  "requestedBy": "support@example.com"
}
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "DEACTIVATED",
  "accountNumber": 12,
  "message": "Customer requested closure",
  "deactivatedAt": "2026-03-11T15:23:01.123"
}
```

Errors
- 400 Bad Request — `AccountAlreadyDeactivatedException`, `AccountNotFoundException`

---

B. **Customer Endpoints**

1) Get all Customers 
- GET /v1/api/customers 
- Success: 200 OK (CustomerResponse)

Request
```http
GET /v1/api/customers HTTP/1.1
Host: localhost:8080
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{ 
  "customers": [ 
     {
         "id": 1,
         "firstName": "Tony",
         "lastName": "Stark",
         "email": "tony@example.com",
         "mobileNumber": "9000000002",
         "address": "Malibu",
         "dob": "1994-05-29"
     }
  ] 
}
```

Errors
- 500 Internal Server Error — runtime

2) Get Customer by ID
- GET /v1/api/customers/{id} 
- Success: 200 OK (CustomerResponse)

Request
```http
GET /v1/api/customers/1 HTTP/1.1
Host: localhost:8080
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{ 
  "customers": [ 
     {
         "id": 1,
         "firstName": "Tony",
         "lastName": "Stark",
         "email": "tony@example.com",
         "mobileNumber": "9000000002",
         "address": "Malibu",
         "dob": "1994-05-29"
     }
  ] 
}
```

Errors
- 404 Not Found — `CustomerNotFoundException`

3) Update exiting customer details
- PUT /v1/api/customers/{id}
- Success: 200 OK (CustomerResponse)

Request
```http
PUT /v1/api/customers/1 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "firstName": "Alice M.",
  "address": "500 New St",
  "email": "alice.updated@example.com"
}
```

Response (200 OK)
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
    "customers": [
        {
            "id": 1,
            "firstName": "Alice M.",
            "lastName": "Doe",
            "email": "alice.doe@example.com",
            "mobileNumber": "9000000002",
            "address": "500 New St",
            "dob": "1994-05-29"
        }
    ]
}
```

Errors
- 400 Bad Request — validation
- 404 Not Found — `CustomerNotFoundException`

---

Error model & exception mappings

All errors use `ErrorDetails`:
```json
{
  "traceId": "<uuid>",
  "status": "BAD_REQUEST|CONFLICT|NOT_FOUND|INTERNAL_SERVER_ERROR",
  "message": "Descriptive message",
  "timestamp": "2026-03-11T12:34:56.789"
}
```

Exception -> HTTP mapping (implemented in `BankExceptionHandler`):
- MethodArgumentNotValidException -> 400
- IllegalArgumentException, AccountNotFoundException, InsufficientBalanceException -> 400
- AccountAlreadyExistsException -> 409
- CustomerNotFoundException -> 404
- AccountAlreadyDeactivatedException -> 400
- HttpMessageNotReadableException -> 400
- RuntimeException -> 500

---

Tests

Run tests
```powershell
mvn test
```

Notes
- Parameterized tests use external test data in `src/test/resources/json/`.
- Recommended CI practice: run unit tests, static analysis, and Testcontainers-based integration tests (Postgres) to validate migrations and transactional behavior.

---

Observability & Security

Observability
- Add `spring-boot-starter-actuator` and selectively expose `/actuator/health` and `/actuator/metrics`.
- Use structured logs and a central logging solution (ELK/EFK, Cloud logging).
- Instrument business metrics: total transfers, failed transfers, active account counts.

Security
- JWT utilities and a commented `AuthController` are present in `util/` but not enabled by default.
- Production checklist if enabling auth:
  - Store signing keys in a secure store (Vault)
  - Use HTTPS/TLS
  - Apply rate limiting and monitor suspicious requests
  - Limit returned error details to avoid information leakage

---

Contribution
- Fork → feature branch → implement → tests → PR
- Keep PRs small, include tests, and ensure `mvn test` passes locally

---

