# AGENTS.md

## Project
- Micronaut 5 application using Netty, Micronaut Data JPA, PostgreSQL, and Micronaut Serialization.
- Java target/runtime expectation is Java 25.
- Main package: `org.arka99`.

## Build And Runtime Notes
- Gradle build expects Java 25.
- Main server port is `8001`.
- Main config is YAML-based:
  - `src/main/resources/application.yml`
  - `src/test/resources/application-test.yml`
- YAML support requires `org.yaml:snakeyaml` and is already added.
- Validation support requires `io.micronaut.validation:micronaut-validation` and is already added.

## Configuration Choices
- `micronaut.server.thread-selection` is set to `AUTO` in `application.yml`.
- This is intentional so thread handoff from Netty to Micronaut-managed execution is visible in logs for blocking controller flows.

## DTO Conventions
- Request DTOs should generally be annotated with:
  - `@Serdeable`
  - `@Introspected`
- Response DTOs generally only need:
  - `@Serdeable`
- This project uses Micronaut compile-time introspection/serialization rather than reflection-heavy runtime behavior.

## Repository Projection Notes
- For Micronaut Data record/DTO projections, JPQL select items should be explicitly aliased to DTO component names.
- Example pattern:
  - `select specialty.id as id, specialty.name as name from Specialty specialty`
- Missing aliases can cause runtime tuple/projection mapping failures.

## Pet Clinic Details Fetching
- `PetClinicServiceImpl` builds `PetClinicDetails` from repository-provided JSON.
- `VetRepository.findAllVetsWithSpecialties()` uses a native PostgreSQL JSON aggregation query.
- Service deserializes that JSON into `List<VetResponseForPetClinic>` with Micronaut `ObjectMapper` and wraps it in `PetClinicDetails`.
- This was chosen intentionally instead of entity mapping or flat projection grouping.

## Thread Tracing Setup
- Startup thread-selection logger:
  - `src/main/java/org/arka99/config/ThreadSelectionLogger.java`
- Application-level request tracing:
  - `src/main/java/org/arka99/config/ThreadTraceServerFilter.java`
- Method-level tracing AOP:
  - `src/main/java/org/arka99/config/TraceThread.java`
  - `src/main/java/org/arka99/config/TraceThreadInterceptor.java`
- Low-level Netty transport tracing:
  - `src/main/java/org/arka99/config/NettyTransportLoggerRegistrar.java`

### Current tracing intent
- Log when request enters Micronaut HTTP filter layer.
- Log controller/service/repository method entry and exit with thread names.
- Log when Netty receives inbound traffic and when it forwards traffic toward Micronaut.
- Log outbound Netty message writes with message type and thread name.

### Important Netty tracing constraints
- Do not attach custom Netty handlers to `LISTENER` channels for per-request tracing.
- The current customizer skips `ChannelRole.LISTENER` intentionally.
- Outbound response logging was broadened because Netty does not always write a plain `HttpResponse`; it may write `HttpContent`, `LastHttpContent`, or other outbound message types.
- If transport logs appear missing, verify with a full application restart; hot reload may not reflect pipeline customizer changes reliably.

## Controllers And Routes
- `PetClinicController` base path: `/pet-clinic`
- `SpecialtyController` base path: `/specialties`
- `VetController` base path: `/vets`
- Delete operations currently use query parameters, not path variables.

## Postman Collection
- Current collection file:
  - `postman/pet-clinic.postman_collection.json`
- Base URL in collection is `http://localhost:8001`.
- Collection was updated to match query-parameter delete endpoints.

## Known Git/Workspace Context
- There may be unrelated staged changes in resource config files.
- Do not blindly revert existing workspace changes.
- A recent atomic commit added tracing across Netty and application layers:
  - `ec03d31` `Add thread tracing across Netty and application layers`

## Suggested Working Style For Next Agent
- Prefer minimal changes.
- Re-scan controllers/config before editing endpoint docs or Postman files because routes changed multiple times during development.
- When diagnosing Micronaut Data issues, distinguish IDE false positives from real compile/runtime failures.
- When diagnosing Micronaut/Netty thread behavior, separate:
  - Netty transport thread events
  - Micronaut HTTP/filter execution
  - controller/service/repository execution
