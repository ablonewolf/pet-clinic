package org.arka99.service.impl;

import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.response.PetClinicDetails;
import org.arka99.model.dto.response.VetResponseForPetClinic;
import org.arka99.repository.VetRepository;
import org.arka99.service.PetClinicService;

import java.io.IOException;
import java.util.List;

@Singleton
@RequiredArgsConstructor
public class PetClinicServiceImpl implements PetClinicService {

    private final VetRepository vetRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PetClinicDetails getPetClinicDetails() {
        String vetsWithSpecialties = this.vetRepository.findAllVetsWithSpecialties();

        try {
            List<VetResponseForPetClinic> vetResponses = this.objectMapper.readValue(vetsWithSpecialties,
                Argument.listOf(VetResponseForPetClinic.class));
            return new PetClinicDetails(vetResponses);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize pet clinic details.", e);
        }
    }
}
