package org.arka99.service;

import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;

import java.util.List;

public interface SpecialtyService {

    List<SpecialtyResponse> findAllSpecialties();

    SpecialtyResponse createNewSpecialty(SpecialtyCreateRequest createRequest);

    SpecialtyResponse updateSpecialty(SpecialtyUpdateRequest updateRequest);

    void deleteByName(String name);

    Specialty findSpecialtyByName(String name);
}
