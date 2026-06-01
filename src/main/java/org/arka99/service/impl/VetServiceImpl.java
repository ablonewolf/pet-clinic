package org.arka99.service.impl;

import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.entity.Specialty;
import org.arka99.model.entity.Vet;
import org.arka99.repository.VetRepository;
import org.arka99.service.SpecialtyService;
import org.arka99.service.VetService;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;
    private final SpecialtyService specialtyService;

    @Override
    @Transactional(readOnly = true)
    public List<VetResponse> findAllVets() {
        return this.vetRepository.findAllVets();
    }

    @Override
    @Transactional
    public VetResponse createNewVet(VetCreateRequest vetCreateRequest) {
        Set<Specialty> specialties = new HashSet<>();

        for (String specialtyName : vetCreateRequest.specialties()) {
            Specialty specialty = specialtyService.findSpecialtyByName(specialtyName);
            specialties.add(specialty);
        }

        Vet vet = Vet.builder()
            .firstName(vetCreateRequest.firstName())
            .lastName(vetCreateRequest.lastName())
            .specialties(specialties)
            .build();

        vet = this.vetRepository.save(vet);
        return new VetResponse(vet.getId(), vet.getFirstName(), vet.getLastName());
    }

    @Override
    @Transactional
    public void deleteByName(String firstName, String lastName) {
        if (!this.vetRepository.existsByFirstNameAndLastName(firstName, lastName)) {
            throw new NoSuchElementException("Vet not found with first name: " + firstName +
                " and last name: " + lastName + ".");
        }
        this.vetRepository.deleteByFirstNameAndLastName(firstName, lastName);
    }

    @Override
    @Transactional
    public VetResponse updateVet(VetUpdateRequest vetUpdateRequest) {
        Vet vet = this.vetRepository.findById(vetUpdateRequest.id())
            .orElseThrow(() -> new NoSuchElementException("Vet not found with id: " + vetUpdateRequest.id() + "."));

        if (vetUpdateRequest.firstName() != null) {
            vet.setFirstName(vetUpdateRequest.firstName());
        }

        if (vetUpdateRequest.lastName() != null) {
            vet.setLastName(vetUpdateRequest.lastName());
        }

        if (vetUpdateRequest.specialties() != null) {
            List<String> specialtyNames = vetUpdateRequest.specialties();
            Set<Specialty> specialties = specialtyNames.stream()
                .map(specialtyService::findSpecialtyByName)
                .collect(Collectors.toSet());
            vet.setSpecialties(specialties);
        }

        vet = this.vetRepository.update(vet);
        return new VetResponse(vet.getId(), vet.getFirstName(), vet.getLastName());
    }
}
