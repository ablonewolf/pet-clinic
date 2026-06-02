package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.Collection;

@Serdeable
@Builder
public record PageResponse<T>(Collection<T> contents,
                              int totalPages,
                              int currentPage,
                              long totalElements) {
}
