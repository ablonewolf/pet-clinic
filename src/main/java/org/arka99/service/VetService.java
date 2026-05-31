package org.arka99.service;

import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.dto.request.VetUpdateRequest;

import java.util.List;

public interface VetService {

    List<VetResponse> findAllVets();

    VetResponse createNewVet(VetCreateRequest vetCreateRequest);

    Long deleteByName(String firstName, String lastName);

    VetResponse updateVet(VetUpdateRequest vetUpdateRequest);
}
