package org.arka99.service;

import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;

import java.util.List;

public interface SpecialtyService {

    List<SpecialtyResponse> findAllSpecialties();

    SpecialtyResponse createNewSpecialty(String name);

    SpecialtyResponse updateSpecialty(SpecialtyUpdateRequest updateRequest);

    void deleteByName(String name);

    Specialty findSpecialtyByName(String name);
}
