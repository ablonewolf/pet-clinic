package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable
public record SpecialtyCreateRequest(
    @NotBlank
    String name) {
}
