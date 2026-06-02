package org.arka99.service;

import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.CursorPageRequest;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.CursorPageResponse;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;

public interface SpecialtyService {

    PageResponse<SpecialtyResponse> findAllSpecialties(PageRequest pageRequest);

    CursorPageResponse<SpecialtyResponse> findAllSpecialtiesByCursor(CursorPageRequest cursorPageRequest);

    SpecialtyResponse createNewSpecialty(SpecialtyCreateRequest createRequest);

    SpecialtyResponse updateSpecialty(SpecialtyUpdateRequest updateRequest);

    void deleteByName(String name);

    Specialty findSpecialtyByName(String name);
}
