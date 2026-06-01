package org.arka99.model.dto.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record VetResponse(Long id, String firstName, String lastName) {
}
