package org.arka99.service.reactive;

import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.VetResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveVetService {

    Flux<VetResponse> findAllVets();

    Mono<VetResponse> createNewVet(VetCreateRequest vetCreateRequest);

    Mono<Void> deleteByName(String firstName, String lastName);

    Mono<VetResponse> updateVet(VetUpdateRequest vetUpdateRequest);
}
