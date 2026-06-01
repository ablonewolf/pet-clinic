# Pet Clinic

Micronaut 5 pet clinic application built with Netty, Micronaut Data JPA, PostgreSQL, and Micronaut Serialization.

## Stack
- Java 25
- Micronaut 5
- Netty server
- Micronaut Data JPA + Hibernate
- Micronaut Data R2DBC
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

## API

Base URL:

```text
http://localhost:8001
```

Endpoints:

### Pet Clinic
- `GET /pet-clinic/details`

### Specialties
- `GET /specialties`
- `POST /specialties/create`
- `POST /specialties/update`
- `DELETE /specialties?name={name}`

### Vets
- `GET /vets`
- `POST /vets/create`
- `POST /vets/update`
- `DELETE /vets?firstName={firstName}&lastName={lastName}`

### Reactive Pet Clinic
- `GET /reactive/pet-clinic/details`

### Reactive Specialties
- `GET /reactive/specialties`
- `POST /reactive/specialties/create`
- `POST /reactive/specialties/update`
- `DELETE /reactive/specialties?name={name}`

### Reactive Vets
- `GET /reactive/vets`
- `POST /reactive/vets/create`
- `POST /reactive/vets/update`
- `DELETE /reactive/vets?firstName={firstName}&lastName={lastName}`

## Postman

Postman collection file:

```text
postman/pet-clinic.postman_collection.json
```

Reactive Postman collection file:

```text
postman/pet-clinic-reactive.postman_collection.json
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

Why:
- `@Serdeable` is used by Micronaut Serialization to deserialize request JSON.
- `@Introspected` gives Micronaut compile-time bean metadata for request binding and validation.

This matters more for request DTOs because Micronaut has to:
- bind incoming data
- inspect constructor/record components
- evaluate validation annotations such as `@NotBlank` and `@NotNull`

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

## Reactive Flow

The project keeps the original imperative JPA/Hibernate flow and adds a parallel R2DBC flow.

The reactive routes use the same PostgreSQL database and the same tables:
- `vets`
- `specialties`
- `vet_specialties`

The R2DBC datasource bean is named `reactive` so its transaction infrastructure does not collide with the JPA/Hibernate `default` datasource.

R2DBC persistence models are separate from JPA entities because Hibernate relationships such as `@ManyToMany` do not apply to R2DBC. The reactive flow composes the many-to-many data with explicit queries.

## Benchmark Helper

To compare the pet clinic details endpoint with minimal typing, start the app and run:

```bash
./scripts/bench-petclinic-details.sh imperative
./scripts/bench-petclinic-details.sh reactive
```

Optional request count and concurrency:

```bash
./scripts/bench-petclinic-details.sh imperative 5000 50
./scripts/bench-petclinic-details.sh reactive 5000 50
```

The script writes ApacheBench output and CPU samples under `build/benchmarks/`.

## Logging

The project does not include custom application logging/tracing classes. Framework/default logging is still provided by Logback and Micronaut.

## Useful Files
- `src/main/resources/application.yml`
- `src/test/resources/application-test.yml`
- `postman/pet-clinic.postman_collection.json`
- `src/main/java/org/arka99/controllers/`
- `src/main/java/org/arka99/controllers/reactive/`
- `src/main/java/org/arka99/repository/`
- `src/main/java/org/arka99/repository/reactive/`
- `src/main/java/org/arka99/service/impl/`
- `src/main/java/org/arka99/service/reactive/`
