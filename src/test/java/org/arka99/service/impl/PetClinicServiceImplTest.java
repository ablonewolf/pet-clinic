package org.arka99.service.impl;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.model.dto.response.VetResponseForPetClinic;
import org.arka99.repository.VetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetClinicServiceImplTest {

    @Mock
    VetRepository vetRepository;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    PetClinicServiceImpl petClinicService;

    @Test
    void getPetClinicDetailsDeserializesRepositoryJson() throws IOException {
        String json = "[]";
        List<VetResponseForPetClinic> vets = List.of(new VetResponseForPetClinic(1L, "James", "Carter", List.of()));
        when(vetRepository.findAllVetsWithSpecialties()).thenReturn(json);
        when(objectMapper.readValue(eq(json), any(Argument.class))).thenReturn(vets);

        PetClinicDetails response = petClinicService.getPetClinicDetails();

        assertEquals(1, response.vetResponses().size());
        assertEquals("James", response.vetResponses().getFirst().firstName());
    }

    @Test
    void getPetClinicDetailsWrapsDeserializationFailure() throws IOException {
        String json = "not-json";
        when(vetRepository.findAllVetsWithSpecialties()).thenReturn(json);
        when(objectMapper.readValue(eq(json), any(Argument.class))).thenThrow(new IOException("bad json"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            petClinicService.getPetClinicDetails());

        assertEquals("Unable to deserialize pet clinic details.", exception.getMessage());
    }
}
