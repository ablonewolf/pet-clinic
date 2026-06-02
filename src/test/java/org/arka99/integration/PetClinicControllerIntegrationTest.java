package org.arka99.integration;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.repository.SpecialtyRepository;
import org.arka99.repository.VetRepository;
import org.arka99.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactional = false)
class PetClinicControllerIntegrationTest extends PostgresIntegrationTest {

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

    @Test
    void petClinicDetailsReturnsVetsSavedInTemporaryPostgres() {
        client.toBlocking().retrieve(HttpRequest.POST("/specialties/create", new SpecialtyCreateRequest("radiology")));
        client.toBlocking().retrieve(HttpRequest.POST("/vets/create",
            new VetCreateRequest("James", "Carter", List.of("radiology"))));

        PetClinicDetails response = client.toBlocking().retrieve(
            HttpRequest.GET("/pet-clinic/details"),
            PetClinicDetails.class
        );

        assertEquals(1, response.vetResponses().size());
        assertEquals("James", response.vetResponses().getFirst().firstName());
        assertEquals("Carter", response.vetResponses().getFirst().lastName());
        assertEquals(1, response.vetResponses().getFirst().specialties().size());
        assertEquals("radiology", response.vetResponses().getFirst().specialties().getFirst().name());
    }

    @Test
    void petClinicDetailsReturnsEmptyListWhenNoVetsExist() {
        PetClinicDetails response = client.toBlocking().retrieve(
            HttpRequest.GET("/pet-clinic/details"),
            PetClinicDetails.class
        );

        assertEquals(0, response.vetResponses().size());
    }
}
