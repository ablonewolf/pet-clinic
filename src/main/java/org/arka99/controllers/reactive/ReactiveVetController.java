package org.arka99.controllers.reactive;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.service.reactive.ReactiveVetService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/reactive/vets")
@RequiredArgsConstructor
public class ReactiveVetController {

    private final ReactiveVetService vetService;

    @Get
    public Flux<VetResponse> findAllVets() {
        return vetService.findAllVets();
    }

    @Post("/create")
    public Mono<HttpResponse<VetResponse>> createNewVet(@Body VetCreateRequest vetCreateRequest) {
        return vetService.createNewVet(vetCreateRequest)
            .map(HttpResponse::created);
    }

    @Post("/update")
    public Mono<HttpResponse<VetResponse>> updateVet(@Body VetUpdateRequest vetUpdateRequest) {
        return vetService.updateVet(vetUpdateRequest)
            .map(HttpResponse::ok);
    }

    @Delete
    public Mono<HttpResponse<String>> deleteVet(@QueryValue String firstName,
                                                @QueryValue String lastName) {
        return vetService.deleteByName(firstName, lastName)
            .thenReturn(HttpResponse.ok("Vet deleted successfully"));
    }
}
