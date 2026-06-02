package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Introspected
@Serdeable
public record VetUpdateRequest(
    @NotNull
    Long id,

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,
    List<String> specialties) {
}
