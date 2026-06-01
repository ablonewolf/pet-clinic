package org.arka99.service.reactive.impl;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.r2dbc.ReactiveSpecialtyEntity;
import org.arka99.repository.reactive.ReactiveSpecialtyRepository;
import org.arka99.service.reactive.ReactiveSpecialtyService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Singleton
@RequiredArgsConstructor
public class ReactiveSpecialtyServiceImpl implements ReactiveSpecialtyService {

    private final ReactiveSpecialtyRepository specialtyRepository;

    @Override
    public Flux<SpecialtyResponse> findAllSpecialties() {
        return this.specialtyRepository.findAllSpecialtyResponses();
    }

    @Override
    public Mono<SpecialtyResponse> createNewSpecialty(SpecialtyCreateRequest createRequest) {
        ReactiveSpecialtyEntity specialty = new ReactiveSpecialtyEntity(null, createRequest.name());
        return this.specialtyRepository.save(specialty)
            .map(savedSpecialty -> new SpecialtyResponse(savedSpecialty.getId(), savedSpecialty.getName()));
    }

    @Override
    public Mono<SpecialtyResponse> updateSpecialty(SpecialtyUpdateRequest updateRequest) {
        return this.specialtyRepository.findById(updateRequest.id())
            .switchIfEmpty(Mono.error(() -> new NoSuchElementException(
                "Specialty not found with id: " + updateRequest.id() + ".")))
            .flatMap(existingSpecialty -> {
                if (existingSpecialty.getName().equals(updateRequest.name())) {
                    return Mono.error(new IllegalArgumentException(
                        "Specialty name cannot be the same as the existing name."));
                }

                return this.specialtyRepository.existsByNameAndIdNot(updateRequest.name(), updateRequest.id())
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new IllegalArgumentException(
                                "Specialty with name " + updateRequest.name() + " already exists."));
                        }

                        existingSpecialty.setName(updateRequest.name());
                        return this.specialtyRepository.update(existingSpecialty)
                            .map(updatedSpecialty -> new SpecialtyResponse(
                                updatedSpecialty.getId(), updatedSpecialty.getName()));
                    });
            });
    }

    @Override
    public Mono<Void> deleteByName(String name) {
        return this.specialtyRepository.existsByName(name)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new NoSuchElementException("Specialty not found with name: " + name + "."));
                }
                return this.specialtyRepository.deleteByName(name).then();
            });
    }

    @Override
    public Mono<ReactiveSpecialtyEntity> findSpecialtyByName(String name) {
        return this.specialtyRepository.findByName(name)
            .switchIfEmpty(Mono.error(() -> new NoSuchElementException(
                "Specialty not found with name: " + name + ".")));
    }
}
