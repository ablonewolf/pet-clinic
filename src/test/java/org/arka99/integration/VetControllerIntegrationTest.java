package org.arka99.integration;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.ErrorResponse;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.repository.SpecialtyRepository;
import org.arka99.repository.VetRepository;
import org.arka99.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
class VetControllerIntegrationTest extends PostgresIntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    VetRepository vetRepository;

    private static final Logger log = LoggerFactory.getLogger(VetControllerIntegrationTest.class);

    @BeforeEach
    void cleanDatabase() {
        vetRepository.deleteAll();
        specialtyRepository.deleteAll();
    }

    @Test
    void createVetPersistsItInPostgres() {
        createSpecialty("radiology");

        VetResponse response = createVet("James", "Carter", List.of("radiology"));

        assertNotNull(response.id());
        assertEquals("James", response.firstName());
        assertEquals("Carter", response.lastName());
        assertTrue(vetRepository.existsByFirstNameAndLastName("James", "Carter"));
    }

    @Test
    void listVetsReturnsPagedResponse() {
        createSpecialty("radiology");
        createVet("James", "Carter", List.of("radiology"));
        createVet("Helen", "Leary", List.of("radiology"));

        PageResponse<VetResponse> response = client.toBlocking().retrieve(
            HttpRequest.POST("/vets", new PageRequest(1, 1)),
            Argument.of(PageResponse.class, VetResponse.class)
        );

        assertEquals(1, response.currentPage());
        assertEquals(2, response.totalPages());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.contents().size());
    }

    @Test
    void createVetRejectsMissingSpecialty() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/vets/create",
                new VetCreateRequest("James", "Carter", List.of("missing")))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialty not found with name: missing.", error.message());
    }

    @Test
    void createVetRejectsInvalidRequest() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/vets/create",
                new VetCreateRequest("", "Carter", List.of()))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(error.message().contains("must not be blank"));
        assertTrue(error.message().contains("must not be empty"));
    }

    @Test
    void updateVetChangesStoredVet() {
        createSpecialty("radiology");
        createSpecialty("surgery");
        VetResponse vet = createVet("James", "Carter", List.of("radiology"));

        VetResponse response = client.toBlocking().retrieve(
            HttpRequest.POST("/vets/update", new VetUpdateRequest(vet.id(), "Jim", "Carter", List.of("surgery"))),
            VetResponse.class
        );

        assertEquals(vet.id(), response.id());
        assertEquals("Jim", response.firstName());
        assertTrue(vetRepository.existsByFirstNameAndLastName("Jim", "Carter"));
    }

    @Test
    void updateVetRejectsMissingVet() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/vets/update",
                new VetUpdateRequest(999L, "Jim", "Corbet", null))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        log.info(error.message());
        assertEquals("Vet not found with id: 999.", error.message());
    }

    @Test
    void deleteVetRejectsMissingVet() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.DELETE("/vets?firstName=Missing&lastName=Vet")));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Vet not found with first name: Missing and last name: Vet.", error.message());
    }

    @Test
    void deleteVetRemovesExistingVet() {
        createSpecialty("radiology");
        createVet("James", "Carter", List.of("radiology"));

        String response = client.toBlocking().retrieve(HttpRequest.DELETE("/vets?firstName=James&lastName=Carter"));

        assertEquals("Vet deleted successfully", response);
        assertFalse(vetRepository.existsByFirstNameAndLastName("James", "Carter"));
    }

    private SpecialtyResponse createSpecialty(String name) {
        return client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/create", new SpecialtyCreateRequest(name)),
            SpecialtyResponse.class
        );
    }

    private VetResponse createVet(String firstName, String lastName, List<String> specialties) {
        return client.toBlocking().retrieve(
            HttpRequest.POST("/vets/create", new VetCreateRequest(firstName, lastName, specialties)),
            VetResponse.class
        );
    }
}
