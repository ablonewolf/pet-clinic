package org.arka99.service.reactive.impl;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.VetCreateRequest;
import org.arka99.model.dto.request.VetUpdateRequest;
import org.arka99.model.dto.response.VetResponse;
import org.arka99.model.r2dbc.ReactiveSpecialtyEntity;
import org.arka99.model.r2dbc.ReactiveVetEntity;
import org.arka99.repository.reactive.ReactiveVetRepository;
import org.arka99.service.reactive.ReactiveSpecialtyService;
import org.arka99.service.reactive.ReactiveVetService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Singleton
@RequiredArgsConstructor
public class ReactiveVetServiceImpl implements ReactiveVetService {

    private final ReactiveVetRepository vetRepository;
    private final ReactiveSpecialtyService specialtyService;

    @Override
    public Flux<VetResponse> findAllVets() {
        return this.vetRepository.findAllVetResponses();
    }

    @Override
    public Mono<VetResponse> createNewVet(VetCreateRequest vetCreateRequest) {
        return findSpecialties(vetCreateRequest.specialties())
            .flatMap(specialties -> {
                ReactiveVetEntity vet = new ReactiveVetEntity(null, vetCreateRequest.firstName(), vetCreateRequest.lastName());
                return this.vetRepository.save(vet)
                    .flatMap(savedVet -> addSpecialties(savedVet.getId(), specialties)
                        .thenReturn(new VetResponse(savedVet.getId(), savedVet.getFirstName(), savedVet.getLastName())));
            });
    }

    @Override
    public Mono<Void> deleteByName(String firstName, String lastName) {
        return this.vetRepository.findByFirstNameAndLastName(firstName, lastName)
            .switchIfEmpty(Mono.error(() -> new NoSuchElementException("Vet not found with first name: " + firstName +
                " and last name: " + lastName + ".")))
            .flatMap(vet -> this.vetRepository.deleteSpecialties(vet.getId())
                .then(this.vetRepository.deleteByFirstNameAndLastName(firstName, lastName))
                .then());
    }

    @Override
    public Mono<VetResponse> updateVet(VetUpdateRequest vetUpdateRequest) {
        return this.vetRepository.findById(vetUpdateRequest.id())
            .switchIfEmpty(Mono.error(() -> new NoSuchElementException(
                "Vet not found with id: " + vetUpdateRequest.id() + ".")))
            .flatMap(vet -> {
                if (vetUpdateRequest.firstName() != null) {
                    vet.setFirstName(vetUpdateRequest.firstName());
                }

                if (vetUpdateRequest.lastName() != null) {
                    vet.setLastName(vetUpdateRequest.lastName());
                }

                Mono<ReactiveVetEntity> updatedVet = this.vetRepository.update(vet);
                if (vetUpdateRequest.specialties() == null) {
                    return updatedVet.map(savedVet -> new VetResponse(
                        savedVet.getId(), savedVet.getFirstName(), savedVet.getLastName()));
                }

                return findSpecialties(vetUpdateRequest.specialties())
                    .flatMap(specialties -> updatedVet.flatMap(savedVet -> this.vetRepository.deleteSpecialties(savedVet.getId())
                        .then(addSpecialties(savedVet.getId(), specialties))
                        .thenReturn(new VetResponse(savedVet.getId(), savedVet.getFirstName(), savedVet.getLastName()))));
            });
    }

    private Mono<List<ReactiveSpecialtyEntity>> findSpecialties(List<String> specialtyNames) {
        return Flux.fromIterable(specialtyNames)
            .distinct()
            .flatMap(this.specialtyService::findSpecialtyByName)
            .collectList();
    }

    private Mono<Void> addSpecialties(Long vetId, List<ReactiveSpecialtyEntity> specialties) {
        return Flux.fromIterable(specialties)
            .flatMap(specialty -> this.vetRepository.addSpecialty(vetId, specialty.getId()))
            .then();
    }
}
