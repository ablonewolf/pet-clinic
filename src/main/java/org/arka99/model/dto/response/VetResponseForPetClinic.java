package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.List;

@Serdeable
@Builder
public record VetResponseForPetClinic(Long id,
                                      String firstName,
                                      String lastName,
                                      List<SpecialtyResponse> specialties) {
}
