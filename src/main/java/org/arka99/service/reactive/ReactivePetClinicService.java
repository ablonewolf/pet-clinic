package org.arka99.service.reactive;

import org.arka99.model.dto.response.PetClinicDetails;
import reactor.core.publisher.Mono;

public interface ReactivePetClinicService {

    Mono<PetClinicDetails> getPetClinicDetails();
}
