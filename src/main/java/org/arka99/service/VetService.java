package org.arka99.service;

import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.VetResponse;

public interface VetService {

    PageResponse<VetResponse> findAllVets(PageRequest pageRequest);

    VetResponse createNewVet(VetCreateRequest vetCreateRequest);

    void deleteByName(String firstName, String lastName);

    VetResponse updateVet(VetUpdateRequest vetUpdateRequest);
}
