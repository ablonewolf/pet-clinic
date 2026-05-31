package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Introspected
@Serdeable
public record SpecialtyUpdateRequest(
    @NotNull
    Long id,

    @NotBlank
    String name) {
}
