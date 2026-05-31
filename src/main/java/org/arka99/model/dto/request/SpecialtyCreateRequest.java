package org.arka99.model.dto.request;

import jakarta.annotation.Nonnull;

public record SpecialtyCreateRequest(
    @Nonnull
    String name) {
}
