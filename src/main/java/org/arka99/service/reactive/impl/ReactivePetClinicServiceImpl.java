package org.arka99.service.reactive.impl;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.model.dto.response.ReactiveVetSpecialtyRow;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.dto.response.VetResponseForPetClinic;
import org.arka99.repository.reactive.ReactiveVetRepository;
import org.arka99.service.reactive.ReactivePetClinicService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor
public class ReactivePetClinicServiceImpl implements ReactivePetClinicService {

    private final ReactiveVetRepository vetRepository;

    @Override
    public Mono<PetClinicDetails> getPetClinicDetails() {
        return this.vetRepository.findAllVetSpecialtyRows()
            .collectList()
            .map(rows -> new PetClinicDetails(toVetResponses(rows)));
    }

    private List<VetResponseForPetClinic> toVetResponses(List<ReactiveVetSpecialtyRow> rows) {
        Map<Long, VetAccumulator> vetsById = new LinkedHashMap<>();

        for (ReactiveVetSpecialtyRow row : rows) {
            VetAccumulator vet = vetsById.computeIfAbsent(row.vetId(), ignored ->
                new VetAccumulator(row.vetId(), row.firstName(), row.lastName(), new ArrayList<>()));

            if (row.specialtyId() != null) {
                vet.specialties().add(new SpecialtyResponse(row.specialtyId(), row.specialtyName()));
            }
        }

        return vetsById.values().stream()
            .map(vet -> new VetResponseForPetClinic(vet.id(), vet.firstName(), vet.lastName(), vet.specialties()))
            .toList();
    }

    private record VetAccumulator(Long id,
                                  String firstName,
                                  String lastName,
                                  List<SpecialtyResponse> specialties) {
    }
}
