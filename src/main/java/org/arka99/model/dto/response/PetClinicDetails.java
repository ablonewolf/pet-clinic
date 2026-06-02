package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record PetClinicDetails(List<VetResponseForPetClinic> vetResponses) {

    public PetClinicDetails {
        vetResponses = vetResponses == null ? List.of() : vetResponses;
    }
}
