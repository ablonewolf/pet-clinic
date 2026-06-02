package org.arka99.integration;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.arka99.model.dto.request.CursorPageRequest;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.CursorPageResponse;
import org.arka99.model.dto.response.ErrorResponse;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.repository.SpecialtyRepository;
import org.arka99.repository.VetRepository;
import org.arka99.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(transactional = false)
class SpecialtyControllerIntegrationTest extends PostgresIntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    SpecialtyRepository specialtyRepository;

    @Inject
    VetRepository vetRepository;

    @BeforeEach
    void cleanDatabase() {
        vetRepository.deleteAll();
        specialtyRepository.deleteAll();
    }

    private static final Logger log = LoggerFactory.getLogger(SpecialtyControllerIntegrationTest.class);

    @Test
    void createSpecialtyPersistsItInPostgres() {
        SpecialtyResponse response = client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/create", new SpecialtyCreateRequest("radiology")),
            SpecialtyResponse.class
        );

        assertNotNull(response.id());
        assertEquals("radiology", response.name());
        assertTrue(specialtyRepository.existsByName("radiology"));
    }

    @Test
    void listSpecialtiesReturnsPagedResponse() {
        createSpecialty("radiology");
        createSpecialty("surgery");

        PageResponse<SpecialtyResponse> response = client.toBlocking().retrieve(
            HttpRequest.POST("/specialties", new PageRequest(1, 1)),
            Argument.of(PageResponse.class, SpecialtyResponse.class)
        );

        assertEquals(1, response.currentPage());
        assertEquals(2, response.totalPages());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.contents().size());
    }

    @Test
    void listSpecialtiesByCursorReturnsNextPage() {
        createSpecialty("radiology");
        createSpecialty("surgery");
        createSpecialty("dentistry");

        CursorPageResponse<SpecialtyResponse> firstPage = client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/cursor", new CursorPageRequest(2, null, null)),
            Argument.of(CursorPageResponse.class, SpecialtyResponse.class)
        );

        assertEquals(2, firstPage.contents().size());
        assertTrue(firstPage.hasNext());
        assertNotNull(firstPage.nextCursor());

        CursorPageResponse<SpecialtyResponse> secondPage = client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/cursor", new CursorPageRequest(2, firstPage.nextCursor(), null)),
            Argument.of(CursorPageResponse.class, SpecialtyResponse.class)
        );

        assertEquals(1, secondPage.contents().size());
        assertFalse(secondPage.hasNext());
        assertNotNull(secondPage.previousCursor());
    }

    @Test
    void createSpecialtyRejectsBlankName() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking()
                .retrieve(HttpRequest.POST("/specialties/create", new SpecialtyCreateRequest(" "))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(400, error.code());
        assertTrue(error.message().contains("must not be blank"));
    }

    @Test
    void listSpecialtiesRejectsInvalidPageRequest() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/specialties", new PageRequest(0, 10))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        log.info(error.message());
        assertTrue(error.message().contains("must be greater than or equal to 1"));
    }

    @Test
    void updateSpecialtyRejectsMissingSpecialty() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking()
                .retrieve(HttpRequest.POST("/specialties/update", new SpecialtyUpdateRequest(999L, "surgery"))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialty not found with id: 999.", error.message());
    }

    @Test
    void updateSpecialtyChangesStoredSpecialty() {
        SpecialtyResponse specialty = createSpecialty("radiology");

        SpecialtyResponse response = client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/update", new SpecialtyUpdateRequest(specialty.id(), "surgery")),
            SpecialtyResponse.class
        );

        assertEquals(specialty.id(), response.id());
        assertEquals("surgery", response.name());
        assertTrue(specialtyRepository.existsByName("surgery"));
    }

    @Test
    void updateSpecialtyRejectsSameName() {
        SpecialtyResponse specialty = createSpecialty("radiology");

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/specialties/update",
                new SpecialtyUpdateRequest(specialty.id(), "radiology"))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Specialty name cannot be the same as the existing name.", error.message());
    }

    @Test
    void updateSpecialtyRejectsDuplicateName() {
        SpecialtyResponse radiology = createSpecialty("radiology");
        createSpecialty("surgery");

        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.POST("/specialties/update",
                new SpecialtyUpdateRequest(radiology.id(), "surgery"))));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Specialty with name surgery already exists.", error.message());
    }

    @Test
    void deleteSpecialtyRejectsMissingName() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(HttpRequest.DELETE("/specialties?name=missing")));

        ErrorResponse error = exception.getResponse().getBody(ErrorResponse.class).orElseThrow();
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialty not found with name: missing.", error.message());
    }

    @Test
    void deleteSpecialtyRemovesExistingSpecialty() {
        createSpecialty("radiology");

        String response = client.toBlocking().retrieve(HttpRequest.DELETE("/specialties?name=radiology"));

        assertEquals("Specialty deleted successfully", response);
        assertFalse(specialtyRepository.existsByName("radiology"));
    }

    private SpecialtyResponse createSpecialty(String name) {
        return client.toBlocking().retrieve(
            HttpRequest.POST("/specialties/create", new SpecialtyCreateRequest(name)),
            SpecialtyResponse.class
        );
    }
}
