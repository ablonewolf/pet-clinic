package org.arka99.controllers.reactive;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.service.reactive.ReactiveSpecialtyService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/reactive/specialties")
@RequiredArgsConstructor
public class ReactiveSpecialtyController {

    private final ReactiveSpecialtyService specialtyService;

    @Get
    public Flux<SpecialtyResponse> findAllSpecialties() {
        return specialtyService.findAllSpecialties();
    }

    @Post("/create")
    public Mono<HttpResponse<SpecialtyResponse>> createNewSpecialty(@Body SpecialtyCreateRequest createRequest) {
        return this.specialtyService.createNewSpecialty(createRequest)
            .map(HttpResponse::created);
    }

    @Post("/update")
    public Mono<HttpResponse<SpecialtyResponse>> updateSpecialty(@Body SpecialtyUpdateRequest updateRequest) {
        return this.specialtyService.updateSpecialty(updateRequest)
            .map(HttpResponse::ok);
    }

    @Delete
    public Mono<HttpResponse<String>> deleteSpecialty(@QueryValue String name) {
        return specialtyService.deleteByName(name)
            .thenReturn(HttpResponse.ok("Specialty deleted successfully"));
    }
}
