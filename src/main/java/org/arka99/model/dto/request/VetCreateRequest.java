package org.arka99.model.dto.request;

import java.util.List;

public record VetCreateRequest(String firstName, String lastName, List<String> specialties) {
}
