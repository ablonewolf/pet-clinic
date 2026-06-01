package org.arka99.repository.reactive;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import org.arka99.model.dto.response.ReactiveVetSpecialtyRow;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.r2dbc.ReactiveVetEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@R2dbcRepository(dataSource = "reactive", dialect = Dialect.POSTGRES)
public interface ReactiveVetRepository extends ReactorCrudRepository<ReactiveVetEntity, Long> {

    @Query(value = """
        SELECT id AS id,
               first_name AS first_name,
               last_name AS last_name
        FROM vets
        """, nativeQuery = true)
    Flux<VetResponse> findAllVetResponses();

    @Query(value = """
        SELECT vet.id AS vet_id,
               vet.first_name AS first_name,
               vet.last_name AS last_name,
               specialty.id AS specialty_id,
               specialty.name AS specialty_name
        FROM vets vet
        LEFT JOIN vet_specialties vet_specialty ON vet_specialty.vet_id = vet.id
        LEFT JOIN specialties specialty ON specialty.id = vet_specialty.specialty_id
        ORDER BY vet.id, specialty.id
        """, nativeQuery = true)
    Flux<ReactiveVetSpecialtyRow> findAllVetSpecialtyRows();

    @Query(value = """
        INSERT INTO vet_specialties (vet_id, specialty_id)
        VALUES (:vetId, :specialtyId)
        """, nativeQuery = true)
    Mono<Long> addSpecialty(Long vetId, Long specialtyId);

    @Query(value = """
        DELETE FROM vet_specialties
        WHERE vet_id = :vetId
        """, nativeQuery = true)
    Mono<Long> deleteSpecialties(Long vetId);

    Mono<Boolean> existsByFirstNameAndLastName(String firstName, String lastName);

    Mono<ReactiveVetEntity> findByFirstNameAndLastName(String firstName, String lastName);

    Mono<Long> deleteByFirstNameAndLastName(String firstName, String lastName);
}
