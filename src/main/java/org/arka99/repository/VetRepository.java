package org.arka99.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.entity.Vet;

import java.util.List;
import java.util.Optional;

@Repository
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
               Vet vet""")
    List<VetResponse> findAllVets();

    Boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
