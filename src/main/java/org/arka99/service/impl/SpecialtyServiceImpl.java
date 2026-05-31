package org.arka99.service.impl;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;
import org.arka99.repository.SpecialtyRepository;
import org.arka99.service.SpecialtyService;

import java.util.List;
import java.util.NoSuchElementException;

@Singleton
@TraceThread("service")
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public List<SpecialtyResponse> findAllSpecialties() {
        return this.specialtyRepository.findAllSpecialties();
    }

    @Override
    public SpecialtyResponse createNewSpecialty(SpecialtyCreateRequest createRequest) {
        Specialty specialty = new Specialty();
        specialty.setName(createRequest.name());

        specialty = this.specialtyRepository.save(specialty);
        return new SpecialtyResponse(specialty.getId(), specialty.getName());
    }

    @Override
    public SpecialtyResponse updateSpecialty(SpecialtyUpdateRequest updateRequest) {
        Specialty existingSpecialty = this.specialtyRepository.findById(updateRequest.id())
            .orElseThrow(() -> new NoSuchElementException("Specialty not found with id: " + updateRequest.id() + "."));

        if (existingSpecialty.getName().equals(updateRequest.name())) {
            throw new IllegalArgumentException("Specialty name cannot be the same as the existing name.");
        } else if (this.specialtyRepository.existsByNameAndIdNot(updateRequest.name(), updateRequest.id())) {
            throw new IllegalArgumentException("Specialty with name " + updateRequest.name() + " already exists.");
        }
        existingSpecialty.setName(updateRequest.name());

        existingSpecialty = this.specialtyRepository.update(existingSpecialty);
        return new SpecialtyResponse(existingSpecialty.getId(), existingSpecialty.getName());
    }

    @Override
    public void deleteByName(String name) {
        if (!this.specialtyRepository.existsByName(name)) {
            throw new NoSuchElementException("Specialty not found with name: " + name + ".");
        }
        this.specialtyRepository.deleteByName(name);
    }

    @Override
    public Specialty findSpecialtyByName(String name) {
        return this.specialtyRepository.findByName(name)
            .orElseThrow(() -> new NoSuchElementException("Specialty not found with name: " + name + "."));
    }
}
