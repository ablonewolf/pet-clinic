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

## Logging Setup
- Custom application logging/tracing classes were removed.
- Framework/default logging remains via Logback and Micronaut.
- Do not reintroduce custom request/thread/Netty logging unless explicitly requested.

## Controllers And Routes
- `PetClinicController` base path: `/pet-clinic`
- `SpecialtyController` base path: `/specialties`
- `VetController` base path: `/vets`
- Reactive controller base paths:
  - `/reactive/pet-clinic`
  - `/reactive/specialties`
  - `/reactive/vets`
- Delete operations currently use query parameters, not path variables.

## Reactive Flow
- The existing imperative JPA/Hibernate flow remains in place.
- A parallel reactive flow uses Micronaut Data R2DBC and Reactor.
- The reactive flow uses the same PostgreSQL database and same tables as the imperative flow.
- The R2DBC datasource bean is named `reactive` to avoid colliding with the JPA/Hibernate `default` datasource transaction beans.
- R2DBC models live separately from JPA entities under `src/main/java/org/arka99/model/r2dbc/`.
- Reactive repositories live under `src/main/java/org/arka99/repository/reactive/`.
- Reactive services live under `src/main/java/org/arka99/service/reactive/`.
- R2DBC does not use Hibernate relationship mapping; many-to-many data is handled with explicit queries against `vet_specialties`.

## Postman Collection
- Current collection file:
  - `postman/pet-clinic.postman_collection.json`
- Reactive collection file:
  - `postman/pet-clinic-reactive.postman_collection.json`
- Base URL in collection is `http://localhost:8001`.
- Collection was updated to match query-parameter delete endpoints.

## Known Git/Workspace Context
- There may be unrelated staged changes in resource config files.
- Do not blindly revert existing workspace changes.
- Custom thread tracing code has been removed from the active branch.

## Suggested Working Style For Next Agent
- Prefer minimal changes.
- Re-scan controllers/config before editing endpoint docs or Postman files because routes changed multiple times during development.
- When diagnosing Micronaut Data issues, distinguish IDE false positives from real compile/runtime failures.
- Do not run build/test verification commands unless the user explicitly asks; the user prefers to run verification locally.
