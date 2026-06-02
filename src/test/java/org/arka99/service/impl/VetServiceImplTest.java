package org.arka99.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.entity.Specialty;
import org.arka99.model.entity.Vet;
import org.arka99.repository.VetRepository;
import org.arka99.service.SpecialtyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VetServiceImplTest {

    @Mock
    VetRepository vetRepository;

    @Mock
    SpecialtyService specialtyService;

    @InjectMocks
    VetServiceImpl vetService;

    @Test
    void findAllVetsMapsRepositoryPageToResponse() {
        Pageable pageable = Pageable.from(0, 1);
        Page<VetResponse> page = Page.of(List.of(new VetResponse(1L, "James", "Carter")), pageable, 2L);
        when(vetRepository.findAllVets(pageable)).thenReturn(page);

        PageResponse<VetResponse> response = vetService.findAllVets(new PageRequest(1, 1));

        assertEquals(1, response.currentPage());
        assertEquals(2, response.totalElements());
        assertEquals(2, response.totalPages());
        assertEquals(1, response.contents().size());
    }

    @Test
    void createNewVetFindsSpecialtiesSavesVetAndMapsResponse() {
        when(specialtyService.findSpecialtyByName("radiology")).thenReturn(specialty(1L, "radiology"));
        when(vetRepository.save(any(Vet.class))).thenAnswer(invocation -> {
            Vet vet = invocation.getArgument(0);
            vet.setId(10L);
            return vet;
        });

        VetResponse response = vetService.createNewVet(new VetCreateRequest("James", "Carter", List.of("radiology")));

        assertEquals(10L, response.id());
        assertEquals("James", response.firstName());
        assertEquals("Carter", response.lastName());
    }

    @Test
    void deleteByNameRejectsMissingVet() {
        when(vetRepository.existsByFirstNameAndLastName("Missing", "Vet")).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
            vetService.deleteByName("Missing", "Vet"));

        assertEquals("Vet not found with first name: Missing and last name: Vet.", exception.getMessage());
    }

    @Test
    void deleteByNameDeletesExistingVet() {
        when(vetRepository.existsByFirstNameAndLastName("James", "Carter")).thenReturn(true);

        vetService.deleteByName("James", "Carter");

        verify(vetRepository).deleteByFirstNameAndLastName("James", "Carter");
    }

    @Test
    void updateVetRejectsMissingVet() {
        when(vetRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
            vetService.updateVet(new VetUpdateRequest(99L, "Jim", null, null)));

        assertEquals("Vet not found with id: 99.", exception.getMessage());
    }

    @Test
    void updateVetUpdatesOnlyProvidedFields() {
        Vet vet = Vet.builder()
            .id(1L)
            .firstName("James")
            .lastName("Carter")
            .build();
        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(vetRepository.update(vet)).thenReturn(vet);

        VetResponse response = vetService.updateVet(new VetUpdateRequest(1L, "Jim", null, null));

        assertEquals("Jim", response.firstName());
        assertEquals("Carter", response.lastName());
    }

    @Test
    void updateVetReplacesSpecialtiesWhenProvided() {
        Vet vet = Vet.builder()
            .id(1L)
            .firstName("James")
            .lastName("Carter")
            .build();
        when(vetRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(specialtyService.findSpecialtyByName("surgery")).thenReturn(specialty(2L, "surgery"));
        when(vetRepository.update(vet)).thenReturn(vet);

        vetService.updateVet(new VetUpdateRequest(1L, null, null, List.of("surgery")));

        assertEquals(1, vet.getSpecialties().size());
        assertTrue(vet.getSpecialties().stream().anyMatch(specialty -> "surgery".equals(specialty.getName())));
    }

    private Specialty specialty(Long id, String name) {
        Specialty specialty = new Specialty();
        specialty.setId(id);
        specialty.setName(name);
        return specialty;
    }
}
