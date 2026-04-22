# Smart Campus API

> **Module:** 5COSC022W – Client-Server Architectures | **University of Westminster**
> **Technology Stack:** Java 11 · JAX-RS (Jersey 2.41) · Grizzly Embedded HTTP Server · Maven

A robust RESTful API for managing Rooms and Sensors across a university Smart Campus. Built entirely with JAX-RS — no Spring Boot, no external database.

---

## Table of Contents

- [Overview](#overview)
- [Architecture and Design](#architecture-and-design)
- [Project Structure](#project-structure)
- [How to Build and Run](#how-to-build-and-run)
- [API Endpoints Reference](#api-endpoints-reference)
- [Sample curl Commands](#sample-curl-commands)
- [Error Handling](#error-handling)
- [Extra Features Beyond Specification](#extra-features-beyond-specification)
- [Conceptual Report](#conceptual-report)

---

## Overview

The Smart Campus API provides a seamless interface for facilities managers and automated building systems to interact with room and sensor data. Key features:

- Full **CRUD** for Rooms and Sensors with correct HTTP semantics (`201`, `204`, `404`, etc.)
- **Sub-resource locator pattern** for sensor reading history (`/sensors/{id}/readings`)
- **Consistent `ErrorResponse` JSON model** across every error scenario
- **HATEOAS-style** discovery endpoint with live stats and server uptime
- **Request/Response logging filter** with response-time measurement in milliseconds
- **Thread-safe** in-memory data store using `ConcurrentHashMap`
- **Input validation** on all POST endpoints
- **Bonus dual-filter** on GET /sensors supporting `?type=` and `?status=` simultaneously

---

## Architecture and Design

### Resource Hierarchy

```
/api/v1
├── /                           Discovery — HATEOAS metadata, uptime, live stats
├── /rooms
│   ├── GET                     List all rooms
│   ├── POST                    Create room → 201 + Location header
│   └── /{roomId}
│       ├── GET                 Get room by ID
│       └── DELETE              Delete room → 204, blocked if sensors exist → 409
└── /sensors
    ├── GET ?type= &status=     List/filter sensors (case-insensitive, combinable)
    ├── POST                    Register sensor → 201 + Location header
    ├── PUT /{sensorId}         Update sensor status/type
    └── /{sensorId}
        ├── GET                 Get sensor by ID
        └── /readings
            ├── GET             Get full reading history
            └── POST            Add reading → 201, updates parent currentValue
```

### Thread Safety

All data lives in static `ConcurrentHashMap` fields in the shared `DataStore` class. JAX-RS resource classes are **request-scoped** by default — a new instance is created for each incoming HTTP request. Because all mutable state is centralised in `DataStore` and backed by `ConcurrentHashMap`, concurrent requests read and write safely without race conditions.

### Consistent Error Model

Every error returns the same `ErrorResponse` structure:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot delete room 'LIB-301' — it still has 1 sensor(s) assigned. Remove all sensors first.",
  "timestamp": 1713200000000
}
```

---

## Project Structure

```
src/main/java/com/smartcampus/
├── AppConfig.java                         @ApplicationPath("/api/v1") entry point
├── DataStore.java                         ConcurrentHashMap in-memory store with seed data
├── Main.java                              Grizzly embedded server bootstrap
│
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java                 UUID id + server-side epoch timestamp
│   └── ErrorResponse.java                 ★ Extra: unified error body model
│
├── resource/
│   ├── DiscoveryResource.java             HATEOAS discovery with uptime + live stats
│   ├── RoomResource.java                  Rooms CRUD with Location headers & validation
│   ├── SensorResource.java                Sensors CRUD + PUT update + dual filters
│   └── SensorReadingResource.java         Sub-resource: reading history management
│
├── exception/
│   ├── RoomNotEmptyException.java         Carries roomId + sensor count
│   ├── LinkedResourceNotFoundException.java
│   └── SensorUnavailableException.java
│
├── mapper/
│   ├── RoomNotEmptyExceptionMapper.java           → 409 Conflict
│   ├── LinkedResourceNotFoundExceptionMapper.java → 422 Unprocessable Entity
│   ├── SensorUnavailableExceptionMapper.java      → 403 Forbidden
│   └── GlobalExceptionMapper.java                → 500 (catch-all, logs full stack server-side)
│
└── filter/
    └── LoggingFilter.java                 ★ Extra: logs method, URI, status, duration(ms)
```

---

## How to Build and Run

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 11 or higher |
| Apache Maven | 3.6+ |
| NetBeans IDE | 12+ (recommended) |

### Step 1 — Clone the repository

```bash
git clone https://github.com/Rukaiya-Riyas/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the project

```bash
mvn clean package
```

### Step 3 — Start the server

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

Expected console output:

```
===========================================
 Smart Campus API is running!
 URL: http://localhost:8080/api/v1
===========================================
Press ENTER to stop the server...
```

### Step 4 — Verify

```bash
curl http://localhost:8080/api/v1
```

### Running inside NetBeans

1. **File → Open Project** → select the `smart-campus-api` folder
2. Right-click the project → **Clean and Build**
3. Right-click `Main.java` → **Run File**
4. The Output tab shows the server start message

---

## API Endpoints Reference

**Base URL:** `http://localhost:8080/api/v1`
All request bodies: `Content-Type: application/json`

### Discovery

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/` | HATEOAS discovery — version, contact, uptime, live counts, `_links` | 200 |

### Rooms

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | `/rooms` | List all rooms | 200 |
| POST | `/rooms` | Create room — `id`, `name`, `capacity > 0` required | 201 + Location |
| GET | `/rooms/{roomId}` | Get room by ID | 200 |
| DELETE | `/rooms/{roomId}` | Delete room — blocked if sensors assigned | 204 |

### Sensors

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | `/sensors` | List sensors — optional `?type=` and/or `?status=` (case-insensitive) | 200 |
| POST | `/sensors` | Register sensor — validates `roomId` exists | 201 + Location |
| GET | `/sensors/{sensorId}` | Get sensor by ID | 200 |
| PUT | `/sensors/{sensorId}` | Update sensor status or type | 200 |

### Sensor Readings

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| GET | `/sensors/{sensorId}/readings` | Full reading history | 200 |
| POST | `/sensors/{sensorId}/readings` | Add reading — updates parent `currentValue` | 201 |

---

## Sample curl Commands

### 1 — Discover the API

```bash
curl -X GET http://localhost:8080/api/v1
```

### 2 — Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-202","name":"Computer Science Lab","capacity":35}'
```

### 3 — List all Rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 4 — Register a Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-007","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 5 — Filter Sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=temperature"
```

### 6 — Filter Sensors by status

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?status=ACTIVE"
```

### 7 — Post a Sensor Reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

### 8 — Get Reading History

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 9 — Attempt to delete Room with active Sensors (expect 409)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 10 — Register Sensor with bad roomId (expect 422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-000"}'
```

---

## Error Handling

| Scenario | HTTP Code |
|----------|-----------|
| Room or sensor not found | 404 Not Found |
| Missing or invalid request field | 400 Bad Request |
| Duplicate ID on POST | 409 Conflict |
| DELETE room that still has sensors | 409 Conflict |
| Sensor roomId does not exist | 422 Unprocessable Entity |
| POST reading to MAINTENANCE sensor | 403 Forbidden |
| Any unexpected server error | 500 Internal Server Error |

All errors return a consistent JSON body — **no raw Java stack traces are ever exposed to the client**.

---

## Extra Features Beyond Specification

| Feature | Description |
|---------|-------------|
| **`ErrorResponse` model** | All exception mappers use a shared POJO with `status`, `error`, `message`, `timestamp` |
| **`201 Created` + Location header** | POST /rooms and POST /sensors both return a `Location` header pointing to the new resource |
| **`204 No Content` on DELETE** | Correct REST semantics — no body on successful deletion |
| **Input validation on POST** | Rooms: validates `id`, `name`, `capacity > 0`. Sensors: validates `id`, `type`, `roomId` |
| **Dual filter on GET /sensors** | Supports `?type=` and `?status=` together or separately, both case-insensitive |
| **Response-time logging** | Filter records start time on request and logs elapsed milliseconds on response |
| **HATEOAS `_links` + live stats** | Discovery returns navigable `_links`, live room/sensor counts, and `uptimeMs` |
| **Sensor count in 409 error** | `RoomNotEmptyException` includes the exact number of blocking sensors |
| **Default sensor status** | If `status` is omitted from POST /sensors, defaults gracefully to `"ACTIVE"` |
| **PUT /sensors/{id}** | Allows updating sensor status (e.g. MAINTENANCE → ACTIVE) |

---

## Conceptual Report

### Part 1 — Service Architecture & Setup

#### 1.1 JAX-RS Resource Lifecycle & Data Handling

Every incoming request will generate an instance of the resource with the default configuration of JAX-RS (Per-Request). By default, all requests will be processed independently of one another, meaning that any variables defined in a resource class would not be shared with any other requests. The second lifecycle you can configure is the singleton. With this configuration, there would only be one instance of a given resource handling all requests. This approach may improve performance, but there are added risks associated with multiple threads accessing the same object concurrently. If such cases aren't handled properly, data could become corrupted.

For example, the Smart Campus API uses the default (Per-Request) lifecycle for resources such as rooms, sensors, and readings. All real-world data utilized by these resources is stored in a common DataStore class, which uses ConcurrentHashMap to store resources. Since the data store is both static and thread-safe, the contents of the ConcurrentHashMap will be consistent for all requests, no matter how many resource instances were created.

Unlike a HashMap, a ConcurrentHashMap allows multiple threads to access an object simultaneously without causing problems. A typical issue with two users attempting to add rooms is that the second user may have their add room action denied because of a duplicate entry being created or data being lost. ConcurrentHashMap prevents this by handling operations like putIfAbsent() atomically.

So overall, even though each request gets a new resource instance, the shared data is safely managed in the background.

#### 1.2 HATEOAS in REST APIs

HATEOAS is a concept that means that through its response, an API should provide clients with links so that they can discover what they need to do next without having to look elsewhere for documentation. In this API, the discovery endpoint is the primary starting point, returning links to all of the major resources, as well as other useful information such as the number of rooms, sensors, and server uptime. As such, the discovery endpoint can be thought of as both a navigation tool and a quick check on the health of the system.

There are many benefits to using HATEOAS. First, clients do not need to hard-code URLs; they only need to follow the links that are provided. Secondly, it simplifies the understanding of how the API works because developers can dynamically navigate through the API. Thirdly, since the API can be enhanced independently of clients, when new functionality is added, it can simply be added as a link without breaking existing clients. As a result, using HATEOAS provides the API with greater flexibility and self-explanatory capabilities and is easier for developers to maintain.

In addition to navigation links, the discovery endpoint also includes important metadata such as API version information, administrative or contact details, and a structured list of available resource endpoints. This makes the API more self-descriptive and aligns with best practices in RESTful design.

---

### Part 2 — Room Management

#### 2.1 Returning Full Objects vs IDs

The GET /rooms API has two ways to return data — by returning just the room id or full room details. If only returning ids, it reduces the response size and is better for performance; however, it requires the client to make additional requests in order to retrieve information about each room (N+1 problem — the original request creates many requests), increasing the overall response time. If returning the full object (data), it eliminates the N+1 issue due to the fact that all needed values will be returned to the client immediately, saving time even though the response would be larger than if just the id were returned.

In this API, full room objects are returned. This is because:
- The data is small (only a few fields per room)
- The number of rooms is limited
- Most use cases (like dashboards) need all the information anyway

In larger systems, a mixed approach is often used, such as pagination or allowing clients to request specific fields. But for this case, returning full objects is the most practical choice.

#### 2.2 Idempotency of the DELETE Operation

A method or function is considered idempotent when applying it several times will not change the result of applying it only once. The DELETE API request for deleting a room follows that definition as well. If you make a successful request to DELETE /rooms/{id}, the response will be HTTP 204 No Content; however, if you were to make that request again, the room would not exist and the response will be HTTP 404 Not Found.

Even though the two responses are different (one indicating success and the other failure), the final result for both is the same. The room was deleted. Therefore, the DELETE /rooms/{id} function continues to be considered idempotent.

Furthermore, when attempting to delete a room that has sensors, the DELETE /rooms/{id} API request returns an HTTP 409 Conflict error. If you were to continue to make the same DELETE API request until successful, the response will continue to be the same with an HTTP 409 Conflict error response; therefore, the DELETE /rooms/{id} API request continues to yield a consistent and predictable behaviour regardless of the outcome of the initial DELETE request.

---

### Part 3 — Sensor Operations

#### 3.1 Technical Consequences of Content-Type Mismatches with @Consumes

When you decorate a POST endpoint with the `@Consumes(MediaType.APPLICATION_JSON)` annotation, you are telling JAX-RS that only `application/json` formatted request bodies are acceptable. If an HTTP request is sent with an invalid Content-Type (such as `text/plain` or `application/xml`), JAX-RS automatically returns HTTP 415 Unsupported Media Type before the request body reaches the resource method or the business logic contained within it.

JAX-RS handles all of this at the framework level through the MessageBodyReader selection process. When a request arrives, JAX-RS checks the Content-Type header and iterates over each of the available MessageBodyReader implementations to determine if any of them support deserialising that Content-Type into the appropriate Java type (in our case, a Sensor object using the Jackson JSON provider). If no valid MessageBodyReader exists to support that format, JAX-RS immediately responds with 415 before the resource method is ever invoked.

Two key advantages of this approach are: first, the framework intercepts and rejects invalid input data formats before they reach your service logic, freeing you from writing defensive code within your resource method; and second, the HTTP 415 status code clearly tells the client that a media-type mismatch caused the error, allowing them to quickly identify and fix the issue.

#### 3.2 Query vs Path Parameters for Filtering

Filtering sensors can be done in two ways:
- Query parameter: `/sensors?type=CO2`
- Path parameter: `/sensors/type/CO2`

Query parameters are the better choice here. They clearly indicate that we are filtering a collection, not accessing a separate resource. They are also optional, so `/sensors` can return all sensors, while `/sensors?type=CO2` returns only filtered results. They also make it easier to combine filters:

```
/sensors?type=CO2&status=ACTIVE
```

Using path parameters for this would be messy and harder to manage. Because of this, query parameters are the standard approach used in most APIs.

---

### Part 4 — Sub-Resources

#### 4.1 Sub-Resource Locator Pattern

The Sub-Resource Locator pattern is a way to organise API code such that nested paths are routed to separate, dedicated classes rather than having everything routed through one large controller. In this API, there is a class called SensorResource that delegates the path `/{sensorId}/readings` to a second class called SensorReadingResource via a locator method annotated with `@Path` but no HTTP method annotation.

The primary advantage of using this pattern is the separation of concerns within the codebase. The responsibilities of the classes are:
- **SensorResource:** Responsible for any type of operation related to sensors (creating, retrieving and filtering sensors)
- **SensorReadingResource:** Responsible solely for the management of the history of readings from a specific sensor

The result of this design leads to a codebase that is far more understandable, testable, and maintainable than if everything were implemented through one large controller class. If all the content were in one class, it would become extremely unmanageable with potentially hundreds of methods. This violates the Single Responsibility Principle which makes it hard to debug, review and onboard new developers.

Another benefit is extensibility. Adding nested resources such as `/sensors/{id}/alerts` would involve creating a new resource class and adding a new locator method — there is no need to change existing code, which reduces the risk of introducing bugs. The pattern also supports contextual initialisation — SensorReadingResource receives its sensorId via its constructor from the locator method, ensuring it always works under the context of a specific sensor.

Finally, this design closely resembles the real-world domain. Sensor readings belong to a specific sensor and do not exist independently of it. Organising code this way allows more intuitive navigation through the codebase for developers who understand how the system works.

#### 4.2 Managing Sensor Readings

The SensorReadingResource supports two main operations:
- **GET** → to retrieve the full history of readings for a sensor
- **POST** → to add a new reading

When a new reading is added using POST, the server automatically generates a UUID for the reading ID and records the timestamp in milliseconds on the server side. The client only needs to send the reading value. This design ensures that all reading IDs are unique and timestamps are consistent and reliable since they come from the server, not the client.

An important side effect of adding a new reading is that the parent Sensor object is also updated. After storing the new SensorReading, the API retrieves the corresponding sensor from the DataStore and updates its `currentValue` field with the new reading's value. This is done to maintain data consistency — when a client calls `GET /sensors/{sensorId}`, they immediately see the latest reading in the `currentValue` field, without needing to go through the entire history of readings.

---

### Part 5 — Error Handling & Security

#### 5.1 Why HTTP 422 is More Semantically Accurate than 404

When a client POSTs a new sensor with a `roomId` that is not found in the system, returning a 404 Not Found response is semantically incorrect. A 404 response conventionally indicates that the resource referenced by the request URI — in this case `/api/v1/sensors` — cannot be found. Since the `/sensors` endpoint exists and can process requests, there is nothing wrong with the URI.

The problem lies entirely within the request payload. The JSON body is semantically valid, the Content-Type header is correct, and the body matches the Sensor model structure. However, the `roomId` field is referencing a resource that does not exist in the system. This is a semantic validation failure, not a format error (which would produce a 400 Bad Request) and not a missing endpoint (which would result in a 404).

HTTP 422 Unprocessable Entity refers specifically to this type of situation — the server received the request in an acceptable format and was able to parse it, but cannot comply because the data is logically invalid. To a client, 422 is unambiguous: the structure is correct but the data inside needs to be fixed. This is a more helpful signal than a 404, which might cause a client to try a different endpoint, whereas 422 correctly signals that the client must first create the referenced room before registering a sensor.

#### 5.2 Risks of Exposing Stack Traces

Java stack traces help with debugging in development, but showing them in API responses can create serious security risks. A stack trace might reveal sensitive internal details like:
- Class and package names (e.g., `com.smartcampus.resource.SensorResource`)
- Framework and library versions (like Jersey)
- File paths and exact line numbers for errors
- Specific exception types (e.g., `NullPointerException`)

This information can be used by attackers to understand the system's internal structure and identify potential weaknesses. For instance, they could match library versions against known security issues in CVE databases or craft specific inputs to exploit flaws in the code.

To prevent this, the API uses a global exception handler with `ExceptionMapper<Throwable>`. This catches all unhandled exceptions including runtime errors. Instead of providing detailed error information, the API returns a generic HTTP 500 Internal Server Error message that keeps internal details hidden. The complete exception data, including the stack trace, is logged internally using `java.util.logging.Logger` at the SEVERE level. This follows the key security principle: log everything internally, but never expose anything sensitive to the client.

#### 5.3 Benefits of JAX-RS Filters for Cross-Cutting Concerns

Cross-cutting concerns are capabilities needed throughout most or all parts of an application — logging, authentication, rate limiting, CORS header injection, and request validation are common examples. As they cross the boundaries of individual modules, they cannot be easily encapsulated in a single class.

Implementing cross-cutting concerns directly in resource methods creates two significant problems. First, it violates the DRY (Don't Repeat Yourself) principle — if logging needs to be added to every resource method, a change to the logging format requires updating dozens of methods, and a developer creating a new endpoint must remember to manually add the logging code, making the system fragile and inconsistent. Second, it violates the Single Responsibility Principle — resource methods whose responsibility is business logic should not also be responsible for operational observability.

JAX-RS filters solve this by applying behaviour at the framework level before and after resource method invocation, without modifying the resource methods at all. The `LoggingFilter` in this API implements both `ContainerRequestFilter` and `ContainerResponseFilter` in a single `@Provider`-annotated class. Since it is registered as a `@Provider`, JAX-RS automatically applies it to every incoming request and every outgoing response — logging the HTTP method, full request URI, response status code, and response time in milliseconds — without requiring any changes to the resource classes.

Filters also support advanced composability. They can be chained and prioritised using `@Priority` to control execution order, and selectively applied to specific endpoints using `@NameBinding`. For example, an authentication filter can be bound to secured endpoints without modifying their implementation. Filters are therefore the ideal architectural tool for promoting consistent behaviour across all API endpoints while keeping resource classes focused purely on business logic.