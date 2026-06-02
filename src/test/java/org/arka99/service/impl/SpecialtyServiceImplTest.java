package org.arka99.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;
import org.arka99.repository.SpecialtyRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceImplTest {

    @Mock
    SpecialtyRepository specialtyRepository;

    @InjectMocks
    SpecialtyServiceImpl specialtyService;

    @Test
    void findAllSpecialtiesMapsRepositoryPageToResponse() {
        Pageable pageable = Pageable.from(0, 1);
        Page<SpecialtyResponse> page = Page.of(List.of(new SpecialtyResponse(1L, "radiology")), pageable, 2L);
        when(specialtyRepository.findAllSpecialties(pageable)).thenReturn(page);

        PageResponse<SpecialtyResponse> response = specialtyService.findAllSpecialties(new PageRequest(1, 1));

        assertEquals(1, response.currentPage());
        assertEquals(2, response.totalElements());
        assertEquals(2, response.totalPages());
        assertEquals(1, response.contents().size());
    }

    @Test
    void createNewSpecialtySavesAndMapsEntity() {
        Specialty savedSpecialty = specialty(1L, "radiology");
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(savedSpecialty);

        SpecialtyResponse response = specialtyService.createNewSpecialty(new SpecialtyCreateRequest("radiology"));

        assertEquals(1L, response.id());
        assertEquals("radiology", response.name());
    }

    @Test
    void updateSpecialtyRejectsMissingSpecialty() {
        when(specialtyRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
            specialtyService.updateSpecialty(new SpecialtyUpdateRequest(99L, "surgery")));

        assertEquals("Specialty not found with id: 99.", exception.getMessage());
    }

    @Test
    void updateSpecialtyRejectsSameName() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty(1L, "radiology")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            specialtyService.updateSpecialty(new SpecialtyUpdateRequest(1L, "radiology")));

        assertEquals("Specialty name cannot be the same as the existing name.", exception.getMessage());
    }

    @Test
    void updateSpecialtyRejectsDuplicateName() {
        when(specialtyRepository.findById(1L)).thenReturn(Optional.of(specialty(1L, "radiology")));
        when(specialtyRepository.existsByNameAndIdNot("surgery", 1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            specialtyService.updateSpecialty(new SpecialtyUpdateRequest(1L, "surgery")));

        assertEquals("Specialty with name surgery already exists.", exception.getMessage());
    }

    @Test
    void deleteByNameRejectsMissingSpecialty() {
        when(specialtyRepository.existsByName("missing")).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
            specialtyService.deleteByName("missing"));

        assertEquals("Specialty not found with name: missing.", exception.getMessage());
    }

    @Test
    void deleteByNameDeletesExistingSpecialty() {
        when(specialtyRepository.existsByName("radiology")).thenReturn(true);

        specialtyService.deleteByName("radiology");

        verify(specialtyRepository).deleteByName("radiology");
    }

    @Test
    void findSpecialtyByNameRejectsMissingSpecialty() {
        when(specialtyRepository.findByName("missing")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
            specialtyService.findSpecialtyByName("missing"));

        assertEquals("Specialty not found with name: missing.", exception.getMessage());
    }

    private Specialty specialty(Long id, String name) {
        Specialty specialty = new Specialty();
        specialty.setId(id);
        specialty.setName(name);
        return specialty;
    }
}
