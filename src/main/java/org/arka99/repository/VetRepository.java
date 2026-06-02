package org.arka99.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.entity.Vet;

import java.util.Optional;

@Repository
@TraceThread("repository")
public interface VetRepository extends JpaRepository<Vet, Long> {

    long deleteByFirstNameAndLastName(String firstName, String lastName);

    @Query(value = """
        SELECT
               vet.id AS id,
               vet.firstName as firstName,
               vet.lastName AS lastName
        FROM
               Vet vet
        WHERE
               vet.firstName = :firstName
           AND vet.lastName = :lastName""")
    Optional<VetResponse> findVetResponseByFirstNameAndLastName(String firstName, String lastName);

    @Query(value = """
        SELECT
               vet.id AS id,
               vet.firstName as firstName,
               vet.lastName AS lastName
        FROM
               Vet vet
        """, countQuery = """
        SELECT COUNT(vet)
        FROM Vet vet
        """)
    Page<VetResponse> findAllVets(Pageable pageable);

    @Query(value = """
        SELECT COALESCE(
            json_agg(
                json_build_object(
                    'id', vet.id,
                    'firstName', vet.first_name,
                    'lastName', vet.last_name,
                    'specialties', COALESCE(specialty_group.specialties, '[]'::json)
                )
                ORDER BY vet.id
            )::text,
            '[]'
        )
        FROM vets vet
        LEFT JOIN (
            SELECT
                vet_specialty.vet_id,
                json_agg(
                    json_build_object(
                        'id', specialty.id,
                        'name', specialty.name
                    )
                    ORDER BY specialty.id
                ) AS specialties
            FROM vet_specialties vet_specialty
            JOIN specialties specialty ON specialty.id = vet_specialty.specialty_id
            GROUP BY vet_specialty.vet_id
        ) specialty_group ON specialty_group.vet_id = vet.id
        """, nativeQuery = true)
    String findAllVetsWithSpecialties();

    Boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
