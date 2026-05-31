package org.arka99.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SpecialtyCreateRequest(
    @NotBlank
    String name) {
}
