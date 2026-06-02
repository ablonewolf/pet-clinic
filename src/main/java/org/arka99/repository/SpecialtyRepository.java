package org.arka99.repository;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;

import java.util.Optional;

@Repository
@TraceThread("repository")
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    @Query(value = """
        SELECT specialty.id AS id,
               specialty.name AS name
        FROM
               Specialty specialty
        """, countQuery = """
        SELECT COUNT(specialty)
        FROM Specialty specialty
        """)
    Page<SpecialtyResponse> findAllSpecialties(Pageable pageable);

    void deleteByName(String name);

    Optional<Specialty> findByName(String name);

    Boolean existsByName(String name);

    Boolean existsByNameAndIdNot(String name, Long id);
}
