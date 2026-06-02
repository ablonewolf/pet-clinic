package org.arka99.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.validation.annotation.ValidatedElement;
import lombok.RequiredArgsConstructor;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.service.SpecialtyService;

@Controller("/specialties")
@TraceThread("controller")
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @Post
    public HttpResponse<PageResponse<SpecialtyResponse>> findAllSpecialties(@ValidatedElement @Body PageRequest pageRequest) {
        PageResponse<SpecialtyResponse> response = specialtyService.findAllSpecialties(pageRequest);
        return HttpResponse.ok(response);
    }

    @Post("/create")
    public HttpResponse<SpecialtyResponse> createNewSpecialty(
        @ValidatedElement @Body SpecialtyCreateRequest createRequest) {
        SpecialtyResponse createdSpecialty = this.specialtyService.createNewSpecialty(createRequest);
        return HttpResponse.created(createdSpecialty);
    }

    @Post("/update")
    public HttpResponse<SpecialtyResponse> updateSpecialty(
        @ValidatedElement @Body SpecialtyUpdateRequest updateRequest) {
        SpecialtyResponse updatedSpecialty = this.specialtyService.updateSpecialty(updateRequest);
        return HttpResponse.ok(updatedSpecialty);
    }

    @Delete
    public HttpResponse<String> deleteSpecialty(@QueryValue String name) {
        specialtyService.deleteByName(name);
        return HttpResponse.ok("Specialty deleted successfully");
    }
}
