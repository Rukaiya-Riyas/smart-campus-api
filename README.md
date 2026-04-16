# Smart Campus API

## Overview
A RESTful API built using JAX-RS (Jersey) and the Grizzly HTTP Server for managing university campus rooms and sensors.

The system enables:

Room management (create, retrieve, delete)
Sensor management and filtering
Sensor reading history tracking
Structured error handling
Request/response logging

## Tech Stack
- Java 11
- JAX-RS (Jersey 2.41)
- Grizzly HTTP Server
- Jackson (JSON)
- Maven

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Steps

1. Clone the repository: https://github.com/Rukaiya-Riyas/smart-campus-api.git

2. Navigate into the project: cd smart-campus-api

3. Build the project: 
mvn clean package

4. Run the server:
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar

5. The API will be available at:(Press ENTER to stop) 
http://localhost:8080/api/v1

## API Endpoints

Resource	Endpoint
Discovery	 GET /api/v1
Rooms	         /api/v1/rooms
Sensors	         /api/v1/sensors
Sensor Readings	 /api/v1/sensors/{sensorId}/readings

## Sample curl Commands

### - Get API info
curl -X GET http://localhost:8080/api/v1

### - Get all rooms
curl -X GET http://localhost:8080/api/v1/rooms

### - Create a room
curl -X POST http://localhost:8080/api/v1/rooms 
-H "Content-Type: application/json" 
-d '{"id":"CS-205","name":"Computer Science Lab","capacity":40}'

### - Filter sensors by type
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"

### - Post a sensor reading
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings 
-H "Content-Type: application/json" 
-d '{"value":23.5}'

## Additional Features
The following enhancements were implemented:

### Input Validation
Rooms validated for non-empty name and capacity > 0
Sensors validated for valid type and roomId

### Structured Error Handling
Introduced a dedicated ErrorResponse model
All errors return consistent JSON format:
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input",
  "timestamp": 1713200000000
}
### RESTful Best Practices
POST returns 201 Created + Location header
DELETE returns 204 No Content

### Advanced Filtering (Bonus Feature)
Supports:
GET /sensors?type=CO2
GET /sensors?status=ACTIVE
GET /sensors?type=CO2&status=ACTIVE
Case-insensitive filtering implemented

### Logging Filter
Logs:
HTTP method
Request URI
Response status
Response time (ms)

### HATEOAS Discovery Endpoint
Root endpoint provides:
API links
Metadata
System uptime

### Thread-Safe Data Storage

Uses ConcurrentHashMap for safe concurrent operations

---

# Report


## **Part 1 - Service Architecture & Setup**

### **1.1 JAX-RS Resource Lifecycle & Data Handling**
Every incoming request generates a new instance of a resource class under the default JAX-RS lifecycle (Per-Request). This means each request is handled independently, and any variables defined within a resource class are not shared across requests.

An alternative lifecycle is the Singleton configuration, where only one instance of a resource handles all requests. While this can improve performance, it introduces risks when multiple threads access shared data concurrently. If not managed carefully, this can lead to data corruption.

In the Smart Campus API, the default Per-Request lifecycle is used for resources such as rooms, sensors, and readings. All actual data is stored in a shared `DataStore` class using `ConcurrentHashMap`. Because this data store is static and thread-safe, it ensures consistent data across all requests regardless of how many resource instances are created.

Unlike a regular `HashMap`, a `ConcurrentHashMap` allows multiple threads to safely access and modify data at the same time. For example, if two users try to add rooms simultaneously, a normal `HashMap` could lead to duplicate or lost data. However, `ConcurrentHashMap` prevents this by using atomic operations such as `putIfAbsent()`.

Overall, even though each request uses a separate resource instance, shared data remains safe and consistent.

---

### **1.2 HATEOAS in REST APIs**

HATEOAS (Hypermedia as the Engine of Application State) is a REST principle where API responses include links that help clients discover available actions without relying on external documentation.

In this API, the discovery endpoint acts as the starting point. It provides links to all major resources along with useful metadata such as the number of rooms, sensors, and server uptime. This makes it both a navigation hub and a health check for the system.

There are several benefits to using HATEOAS. Clients do not need to hard-code URLs and can instead follow links dynamically. It also makes the API easier to understand, as developers can explore it without needing separate documentation. Additionally, the API becomes more flexible, as new features can be added simply by including new links without breaking existing clients.

The discovery endpoint also includes metadata such as API version information, contact details, and available endpoints, making the API self-descriptive and aligned with REST best practices.

---

## **Part 2 - Room Management**

### **2.1 Returning Full Objects vs IDs**

The `GET /rooms` endpoint can return either only room IDs or full room objects.

Returning only IDs reduces response size and improves bandwidth efficiency. However, it forces the client to make additional requests to retrieve full details for each room, leading to the N+1 problem, where one request results in many follow-up requests.

Returning full objects increases response size but eliminates the need for additional requests. This improves overall performance by reducing latency.

In this API, full room objects are returned because:

* Each room contains only a small amount of data
* The number of rooms is limited
* Most use cases require full room details

In larger systems, a hybrid approach such as pagination or field filtering is often used. However, for this system, returning full objects is the most practical solution.

---

### **2.2 Idempotency of the DELETE Operation**

An operation is idempotent if performing it multiple times results in the same final state as performing it once.

The `DELETE /rooms/{id}` endpoint follows this principle. The first successful request returns HTTP 204 (No Content). If the same request is repeated, the room no longer exists, and the API returns HTTP 404 (Not Found).

Although the responses differ, the final state remains the same — the room is deleted. Therefore, the operation is idempotent.

If a room still contains sensors, the API returns HTTP 409 (Conflict). Repeating the request will continue to return the same response, maintaining consistent and predictable behaviour.

---

## **Part 3 - Sensor Operations**

### **3.1 Content-Type Handling**

Using `@Consumes(application/json)` ensures that the API only accepts JSON input.

If a client sends data in an unsupported format (such as XML or plain text), the API automatically returns HTTP 415 (Unsupported Media Type) before the request reaches the method.

JAX-RS handles this internally by attempting to find a suitable parser (e.g., Jackson for JSON). If no parser is available, the request is rejected immediately.

This prevents invalid data from reaching the business logic layer and provides clear feedback to the client.

---

### **3.2 Query vs Path Parameters for Filtering**

Filtering sensors can be implemented using:

* Query parameters: `/sensors?type=CO2`
* Path parameters: `/sensors/type/CO2`

Query parameters are preferred because they clearly represent filtering of a collection rather than accessing a separate resource. They are also optional, allowing `/sensors` to return all results and `/sensors?type=CO2` to return filtered results.

They also support combining multiple filters, such as:

```
/sensors?type=CO2&status=ACTIVE
```

Using path parameters for filtering would be less flexible and harder to manage. Therefore, query parameters are the standard approach.

---

## **Part 4 - Sub-Resources**

### **4.1 Sub-Resource Locator Pattern**

The Sub-Resource Locator pattern allows nested paths to be handled by separate classes instead of a single large controller.

In this API, `SensorResource` delegates the path `/{sensorId}/readings` to `SensorReadingResource` using a locator method annotated with `@Path`.

This approach improves separation of concerns:

* `SensorResource` handles sensor-related operations
* `SensorReadingResource` manages sensor readings

This keeps the codebase clean, easier to maintain, and easier to test. Without this pattern, a single class could become very large and difficult to manage, violating the Single Responsibility Principle.

It also improves extensibility. New nested resources (e.g., `/sensors/{id}/alerts`) can be added without modifying existing code.

Additionally, `SensorReadingResource` receives the `sensorId` through its constructor, ensuring it always operates within the correct context. This avoids repeated validation across methods.

Finally, this structure reflects the real-world relationship where readings belong to a specific sensor, making the system intuitive to understand.

---

### **4.2 Managing Sensor Readings**

The `SensorReadingResource` supports:

* **GET** → retrieve all readings for a sensor
* **POST** → add a new reading

When a reading is added:

* A UUID is generated for the reading ID
* A timestamp is recorded on the server

The client only provides the value, ensuring consistency and reliability.

After storing the reading, the parent sensor’s `currentValue` field is updated. This ensures that clients retrieving sensor data always see the latest value without needing to check the full history.

---

## **Part 5 - Error Handling & Security**

### **5.1 Why 422 Instead of 404**

Returning HTTP 404 would be incorrect when a sensor is created with an invalid `roomId`, as the `/sensors` endpoint exists.

Instead, HTTP 422 (Unprocessable Entity) is used because the request is valid in structure but contains invalid data.

This clearly indicates a semantic validation error and helps developers understand that the issue lies in the request data, not the endpoint.

---

### **5.2 Risks of Exposing Stack Traces**

Exposing stack traces in API responses is a security risk. They can reveal:

* Internal class and package names
* Framework and library versions
* File paths and line numbers
* Exception types

Attackers can use this information to identify vulnerabilities.

To prevent this, a global exception handler (`ExceptionMapper<Throwable>`) returns a generic HTTP 500 response while logging full details internally using `java.util.logging.Logger`.

This follows the principle: **log everything internally, expose nothing externally.**

---

### **5.3 JAX-RS Filters for Cross-Cutting Concerns**

Cross-cutting concerns such as logging, authentication, and rate limiting affect multiple parts of the application.

Implementing them inside resource methods leads to code duplication and poor maintainability.

Instead, this API uses JAX-RS filters. A `LoggingFilter` implements both:

* `ContainerRequestFilter`
* `ContainerResponseFilter`

It automatically logs:

* HTTP method
* Request URI
* Response status
* Processing time

This keeps business logic clean and ensures consistent logging.

Filters can also be combined, prioritised, and selectively applied using `@Priority` and `@NameBinding`, making them highly flexible.

---
