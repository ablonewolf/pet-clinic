package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;

import java.util.List;

@Introspected
@Serdeable
public record VetUpdateRequest(
    @Nonnull
    Long id,

    String firstName,
    String lastName,
    List<String> specialties) {
}
