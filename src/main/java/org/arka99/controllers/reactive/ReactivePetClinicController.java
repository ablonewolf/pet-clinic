package org.arka99.controllers.reactive;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.service.reactive.ReactivePetClinicService;
import reactor.core.publisher.Mono;

@Controller("/reactive/pet-clinic")
@RequiredArgsConstructor
public class ReactivePetClinicController {

    private final ReactivePetClinicService petClinicService;

    @Get("/details")
    public Mono<HttpResponse<PetClinicDetails>> getPetClinicDetails() {
        return petClinicService.getPetClinicDetails()
            .map(HttpResponse::ok);
    }
}
