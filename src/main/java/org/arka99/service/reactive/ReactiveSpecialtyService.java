package org.arka99.service.reactive;

import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.r2dbc.ReactiveSpecialtyEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveSpecialtyService {

    Flux<SpecialtyResponse> findAllSpecialties();

    Mono<SpecialtyResponse> createNewSpecialty(SpecialtyCreateRequest createRequest);

    Mono<SpecialtyResponse> updateSpecialty(SpecialtyUpdateRequest updateRequest);

    Mono<Void> deleteByName(String name);

    Mono<ReactiveSpecialtyEntity> findSpecialtyByName(String name);
}
