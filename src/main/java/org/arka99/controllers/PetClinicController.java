package org.arka99.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.service.PetClinicService;

@Controller("/pet-clinic")
@RequiredArgsConstructor
public class PetClinicController {

    private final PetClinicService petClinicService;

    @Get("/details")
    public HttpResponse<PetClinicDetails> getPetClinicDetails() {
        PetClinicDetails petClinicDetails = petClinicService.getPetClinicDetails();
        return HttpResponse.ok(petClinicDetails);
    }
}
