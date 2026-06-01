package org.arka99.model.dto.response;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ReactiveVetSpecialtyRow(Long vetId,
                                      String firstName,
                                      String lastName,
                                      Long specialtyId,
                                      String specialtyName) {
}
