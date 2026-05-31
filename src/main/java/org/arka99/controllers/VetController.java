package org.arka99.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import lombok.RequiredArgsConstructor;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.service.VetService;

import java.util.List;

@Controller("/vets")
@TraceThread("controller")
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;

    @Get
    public HttpResponse<List<VetResponse>> findAllVets() {
        List<VetResponse> vets = vetService.findAllVets();
        return HttpResponse.ok(vets);
    }

    @Post("/create")
    public HttpResponse<VetResponse> createNewVet(@Body VetCreateRequest vetCreateRequest) {
        VetResponse createdVet = vetService.createNewVet(vetCreateRequest);
        return HttpResponse.created(createdVet);
    }

    @Post("/update")
    public HttpResponse<VetResponse> updateVet(@Body VetUpdateRequest vetUpdateRequest) {
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
