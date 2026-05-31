package org.arka99.model.dto.request;

import jakarta.annotation.Nonnull;

public record SpecialtyUpdateRequest(
    @Nonnull
    Long id,

    @Nonnull
    String name) {
}
