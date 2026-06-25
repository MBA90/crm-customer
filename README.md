# CRM Customer Service

A Spring Boot microservice for managing CRM customer data. It exposes a REST API
for CRUD operations over customers, backed by an Oracle database with schema
managed by Liquibase.

## Tech Stack

- Java 21
- Spring Boot 3.5.15 (Web, Data JPA, Validation)
- Oracle Database (`ojdbc11`)
- Liquibase (schema migrations)
- MapStruct 1.5.5 (entity/DTO mapping)
- Lombok
- Maven

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included Maven Wrapper)
- A reachable Oracle database

### Configuration

Connection and server settings live in `src/main/resources/application.yaml`.
Defaults:

| Setting        | Default                                     |
|----------------|---------------------------------------------|
| Server port    | `8201`                                      |
| Datasource URL | `jdbc:oracle:thin:@localhost:1521/ORCLPDB1` |
| Username       | `crm_customer`                              |
| Schema         | `crm_customer`                              |

Liquibase runs on startup using `classpath:db/changelog/master.xml` to create the
`CUSTOMERS` table, sequence, and indexes. JPA `ddl-auto` is `none` — the schema is
owned entirely by Liquibase.

### Build & Run

```bash
# Run the app
./mvnw spring-boot:run

# Build a jar
./mvnw clean package

# Run tests
./mvnw test
```

The service starts on http://localhost:8201.

## API

Base path: `/api/customers`

| Method   | Path                      | Description                | Success |
|----------|---------------------------|----------------------------|---------|
| `GET`    | `/api/customers`          | List all customers         | 200     |
| `GET`    | `/api/customers/{refNo}`  | Get a customer by ref. no. | 200     |
| `POST`   | `/api/customers`          | Create a customer          | 201     |
| `PUT`    | `/api/customers/{refNo}`  | Update a customer          | 200     |
| `DELETE` | `/api/customers/{refNo}`  | Delete a customer          | 204     |

## Docker

```bash
# Build the jar first
./mvnw clean package -DskipTests

# Build and run the image
docker build -t crm-customer .
docker run -p 8201:8201 crm-customer
```

## CI/CD

A `Jenkinsfile` defines a pipeline that builds the jar, runs tests (publishing
JUnit reports), then builds and pushes a Docker image tagged with the build
number to Docker Hub.
