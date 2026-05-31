package org.arka99.model.dto.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Introspected
@Serdeable
public record VetCreateRequest(
    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotEmpty
    List<String> specialties) {
}
