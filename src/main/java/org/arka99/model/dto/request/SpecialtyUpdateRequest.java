package org.arka99.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SpecialtyUpdateRequest(
    @NotNull
    Long id,

    @NotBlank
    String name) {
}
