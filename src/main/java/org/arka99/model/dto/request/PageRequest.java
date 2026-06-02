package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Introspected
@Serdeable
public record PageRequest(
    @NotNull
    @Min(1)
    Integer page,

    @NotNull
    @Min(1)
    Integer size) {
}
