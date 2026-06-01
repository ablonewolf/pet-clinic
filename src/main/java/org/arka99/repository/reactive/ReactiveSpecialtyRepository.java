package org.arka99.repository.reactive;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.r2dbc.annotation.R2dbcRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.r2dbc.ReactiveSpecialtyEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@R2dbcRepository(dataSource = "reactive", dialect = Dialect.POSTGRES)
public interface ReactiveSpecialtyRepository extends ReactorCrudRepository<ReactiveSpecialtyEntity, Long> {

    @Query(value = """
        SELECT id AS id,
               name AS name
        FROM specialties
        """, nativeQuery = true)
    Flux<SpecialtyResponse> findAllSpecialtyResponses();

    Mono<ReactiveSpecialtyEntity> findByName(String name);

    Mono<Boolean> existsByName(String name);

    Mono<Boolean> existsByNameAndIdNot(String name, Long id);

    Mono<Long> deleteByName(String name);
}
