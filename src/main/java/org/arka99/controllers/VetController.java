package org.arka99.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.service.VetService;

@Controller("/vets")
@TraceThread("controller")
@Validated
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;

    @Post
    public HttpResponse<PageResponse<VetResponse>> findAllVets(@Valid @Body PageRequest pageRequest) {
        PageResponse<VetResponse> response = vetService.findAllVets(pageRequest);
        return HttpResponse.ok(response);
    }

    @Post("/create")
    public HttpResponse<VetResponse> createNewVet(@Valid @Body VetCreateRequest vetCreateRequest) {
        VetResponse createdVet = vetService.createNewVet(vetCreateRequest);
        return HttpResponse.created(createdVet);
    }

    @Post("/update")
    public HttpResponse<VetResponse> updateVet(@Valid @Body VetUpdateRequest vetUpdateRequest) {
        VetResponse updatedVet = vetService.updateVet(vetUpdateRequest);
        return HttpResponse.ok(updatedVet);
    }

    @Delete
    public HttpResponse<String> deleteVet(@QueryValue String firstName,
                                          @QueryValue String lastName) {
        vetService.deleteByName(firstName, lastName);
        return HttpResponse.ok("Vet deleted successfully");
    }
}
