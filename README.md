# Pet Clinic

Micronaut 5 pet clinic application built with Netty, Micronaut Data JPA, PostgreSQL, and Micronaut Serialization.

## Stack
- Java 25
- Micronaut 5
- Netty server
- Micronaut Data JPA + Hibernate
- PostgreSQL
- Micronaut Serialization
- Gradle

## Run

Prerequisites:
- Java 25
- PostgreSQL running locally
- database `pet_clinic`

Default application settings are in `src/main/resources/application.yml`:

```yml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/pet_clinic
    username: postgres
    password: postgres

micronaut:
  server:
    port: 8001
    thread-selection: AUTO
```

Start the app:

```bash
./gradlew run
```

## Build

Compile:

```bash
./gradlew compileJava
```

Build:

```bash
./gradlew build
```

## Tests

Integration tests use Micronaut Test with Testcontainers PostgreSQL:

```bash
./gradlew test
```

The integration test base is:

```text
src/test/java/org/arka99/support/PostgresIntegrationTest.java
```

Service unit tests use JUnit Jupiter 6 and Mockito.

## API

Base URL:

```text
http://localhost:8001
```

Endpoints:

### Pet Clinic
- `GET /pet-clinic/details`

### Specialties
- `POST /specialties`
- `POST /specialties/create`
- `POST /specialties/update`
- `DELETE /specialties?name={name}`

### Vets
- `POST /vets`
- `POST /vets/create`
- `POST /vets/update`
- `DELETE /vets?firstName={firstName}&lastName={lastName}`

Paged list requests currently use this body shape:

```json
{
  "page": 1,
  "size": 10
}
```

## Postman

Postman collection file:

```text
postman/pet-clinic.postman_collection.json
```

It is configured for:

```text
http://localhost:8001
```

## DTO Notes

This project uses Micronaut compile-time metadata heavily.

### Request DTOs

Request DTOs should generally use:
- `@Serdeable`
- `@Introspected`
- controller request-body parameters that should be validated are currently annotated with `@ValidatedElement`

Why:
- `@Serdeable` is used by Micronaut Serialization to deserialize request JSON.
- `@Introspected` gives Micronaut compile-time bean metadata for request binding and validation.

This matters more for request DTOs because Micronaut has to:
- bind incoming data
- inspect constructor/record components
- evaluate validation annotations such as `@NotBlank` and `@NotNull`

Current validation trigger in this project:
- `SpecialtyController` uses `@ValidatedElement @Body` on list/create/update request DTO parameters
- `VetController` uses `@ValidatedElement @Body` on list/create/update request DTO parameters

`VetUpdateRequest.id` is currently annotated with `@Nonnull`.

### Response DTOs

Response DTOs generally only need:
- `@Serdeable`

Why:
- response DTOs are usually only serialized back to JSON
- they typically do not need the same binding/validation introspection path as request DTOs

## Validation Notes

Bean validation support requires:

```gradle
implementation("io.micronaut.validation:micronaut-validation")
```

Without it, validation annotations like `@NotBlank` and `@NotNull` will not resolve correctly on the compile classpath.

Validation failures are handled through:
- `src/main/java/org/arka99/exception/ConstraintViolationExceptionHandler.java`

Current behavior:
- request-body validation failures return `400 Bad Request`
- the response body uses `ErrorResponse`
- multiple constraint violations are combined into one readable message string

## Error Handling

Standardized error responses use:

```text
src/main/java/org/arka99/model/dto/response/ErrorResponse.java
```

Current exception handlers:
- `ResourceNotFoundExceptionHandler` for `NoSuchElementException`
- `IllegalArgumentExceptionHandler` for `IllegalArgumentException`
- `ConstraintViolationExceptionHandler` for validation failures
- `GenericExceptionHandler` for uncaught `RuntimeException`

These handlers return `ErrorResponse(code, message)` so error payloads stay consistent across the API.

## Projection Notes

For Micronaut Data DTO/record projections, JPQL select items should be explicitly aliased to match DTO component names.

Correct pattern:

```java
@Query("""
    SELECT specialty.id AS id,
           specialty.name AS name
    FROM Specialty specialty
""")
List<SpecialtyResponse> findAllSpecialties();
```

Why:
- missing aliases can cause tuple/projection mapping failures at runtime

## Pagination Notes

Both list APIs now use request-body pagination and return a custom `PageResponse<T>`.

Current pattern:
- controller accepts `PageRequest`
- service converts it to Micronaut `Pageable`
- repository returns `Page<T>`
- service maps that to `PageResponse<T>`

For custom paged `@Query` methods in Micronaut Data, an explicit `countQuery` is required.

Why:
- `Pageable` only describes which slice to fetch
- `Page<T>` also needs total-count metadata such as total elements and total pages
- Spring Data JPA often derives the count query automatically
- Micronaut Data is stricter here and requires the `countQuery` to be declared for custom paged projection queries

Example:

```java
@Query(value = """
    SELECT specialty.id AS id,
           specialty.name AS name
    FROM Specialty specialty
    """, countQuery = """
    SELECT COUNT(specialty)
    FROM Specialty specialty
    """)
Page<SpecialtyResponse> findAllSpecialties(Pageable pageable);
```

## Pet Clinic Details Design

`PetClinicDetails` is built from repository-provided JSON instead of entity graph mapping.

Current flow:
- `VetRepository.findAllVetsWithSpecialties()` runs a native PostgreSQL query
- the query aggregates vets and specialties into JSON
- `PetClinicServiceImpl` deserializes that JSON into `List<VetResponseForPetClinic>`
- the list is wrapped in `PetClinicDetails`

This was chosen intentionally instead of:
- entity mapping + manual nested conversion
- flat projection grouping

## Thread Tracing

The project includes tracing to observe how requests move across Netty and Micronaut execution layers.

### Current tracing components
- `src/main/java/org/arka99/config/ThreadSelectionLogger.java`
- `src/main/java/org/arka99/config/ThreadTraceServerFilter.java`
- `src/main/java/org/arka99/config/TraceThread.java`
- `src/main/java/org/arka99/config/TraceThreadInterceptor.java`
- `src/main/java/org/arka99/config/NettyTransportLoggerRegistrar.java`

Tracing classes now include:
- detailed maintenance-oriented comments explaining why each hook exists
- `@NullMarked` annotations for null-safety

### What is logged
- configured Micronaut thread-selection mode at startup
- request entry/exit at Micronaut filter level
- controller/service/repository method entry and exit with thread names
- low-level Netty inbound transport activity
- low-level Netty outbound writes with message type and thread name

### Thread selection

`micronaut.server.thread-selection` is set to `AUTO`.

This is intentional so you can observe thread handoff from:
- Netty event loop threads
- to Micronaut-managed execution threads

### Netty tracing caution

The Netty customizer intentionally skips `ChannelRole.LISTENER`.

Reason:
- listener channels are not the right place for per-request tracing
- attaching handlers there can cause duplicate handler issues and noisy runtime errors

If transport logs appear inconsistent after code changes, do a full restart instead of relying on hot reload.

## Useful Files
- `src/main/resources/application.yml`
- `src/test/resources/application-test.yml`
- `postman/pet-clinic.postman_collection.json`
- `src/main/java/org/arka99/controllers/`
- `src/main/java/org/arka99/repository/`
- `src/main/java/org/arka99/service/impl/`
- `src/main/java/org/arka99/config/`
