# Device Manager API

A REST API built with Spring Boot to persist and manage devices.

## Overview

This API allows clients to create, get, update, and delete devices. Each device has a name, brand, and a state (`AVAILABLE`, `IN_USE`, `INACTIVE`).

Rules:
- Creation time cannot be updated
- Name and brand properties cannot be updated if the device is in `IN_USE` state
- Devices with a duplicate name + brand combination cannot be created
- Devices in `IN_USE` state cannot be deleted
- State transitions are validated — not all transitions are allowed

### State Transitions

Devices follow a defined state machine:

```
AVAILABLE → IN_USE
AVAILABLE → INACTIVE
IN_USE    → AVAILABLE
IN_USE    → INACTIVE
INACTIVE  → Terminal state
```

Attempting an invalid transition returns a `409 Conflict`.

---

## Requirements

- Java 21
- Docker

---

## Running the Application (with docker and Postgres DB)

### With Docker and PostgresDB

```bash
docker compose up
```

The API will be available at `http://localhost:8080`.

## Locally (with in-memory H2 DB)

If you don't want to run Docker, the application includes an H2 in-memory database profile for local development.

### 1. Execute spring-boot:run

```bash
./mvnw spring-boot:run
```

---

## API Documentation

Once the application is running, the Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI spec can be found at:

```
http://localhost:8080/v3/api-docs
```

## API Endpoints

| Method   | Endpoint              | Description                        |
|----------|-----------------------|------------------------------------|
| `POST`   | `/api/devices`        | Create a new device                |
| `GET`    | `/api/devices`        | List devices (filter by brand, state) |
| `GET`    | `/api/devices/{id}`   | Get a device by ID                 |
| `PATCH`  | `/api/devices/{id}`   | Partially update a device          |
| `DELETE` | `/api/devices/{id}`   | Delete a device                    |

---

## Request & Response Examples

### Create a device
```json
POST /api/devices
{
  "name": "iPhone 15",
  "brand": "Apple",
  "state": "AVAILABLE"
}
```

### Update a device
```json
PATCH /api/devices/{id}
{
  "name": "iPhone 15 Pro",
  "state": "IN_USE"
}
```

All fields are optional in a PATCH request.

---

## Error Handling

The API returns consistent error responses across all endpoints:

```json
{
  "message": "Validation failed",
  "errors": ["Device name is required"],
  "timestamp": "2026-03-04T18:00:00.000Z"
}
```

| Status | Scenario                                      |
|--------|-----------------------------------------------|
| `400`  | Validation errors, invalid parameter types   |
| `404`  | Device not found                              |
| `409`  | Duplicate device or invalid state transition  |
| `500`  | Unexpected server error                       |

---

## Next Steps / Improvements

- Currently the GET endpoints return all devices at once. With too much volume, this would not scale. To address this, we could add pagination using Spring Data's `Pageable`.
- The Schema is built by Hibernate due to `ddl-auto`. For better production-rediness we could replace it with a schema migration management system, such as Flyway
- A bottom-up approach was followed where the code generates the API contract. An alternative would be to follow a contract-first model, where we would write the contract first and leverage tools such as the `OpenAPI generator plugin` to generate the controller interfaces from it.
- Better auditing on the Device entity. On top of the already existing `creationDate`, we could also track who modified what and when.
- Read endpoints could benefit from a caching mechanism (e.g., Redis) if volume and query frequency justifies it.
- No AuthN/AuthZ, which could and should be added for product-ready software.
- No resilience patterns. We could leverage `resilience4j` and implement retries, rate limiting, ...
- We could have contract testing to verify dependant services are not affected by changes in this API.
