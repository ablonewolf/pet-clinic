package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Introspected
@Serdeable
public record VetUpdateRequest(Long id,
                               String firstName,
                               String lastName,
                               List<String> specialties) {
}
