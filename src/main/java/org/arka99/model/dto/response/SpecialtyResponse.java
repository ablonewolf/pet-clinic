package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SpecialtyResponse(Long id, String name) {
}
