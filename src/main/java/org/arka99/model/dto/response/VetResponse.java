package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record VetResponse(Long id, String firstName, String lastName) {
}
