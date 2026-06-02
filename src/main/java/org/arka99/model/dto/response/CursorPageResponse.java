package org.arka99.model.dto.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

import java.util.Collection;

@Serdeable
@Builder
public record CursorPageResponse<T>(Collection<T> contents,
                                    boolean hasNext,
                                    boolean hasPrevious,
                                    Long nextCursor,
                                    Long previousCursor) {
}
