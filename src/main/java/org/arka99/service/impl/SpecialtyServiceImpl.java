package org.arka99.service.impl;

import io.micronaut.data.model.CursoredPageable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Pageable.Cursor;
import io.micronaut.data.model.Sort;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.arka99.model.dto.request.CursorPageRequest;
import org.arka99.config.TraceThread;
import org.arka99.model.dto.request.PageRequest;
import org.arka99.model.dto.response.CursorPageResponse;
import org.arka99.model.dto.request.SpecialtyCreateRequest;
import org.arka99.model.dto.request.SpecialtyUpdateRequest;
import org.arka99.model.dto.response.PageResponse;
import org.arka99.model.dto.response.SpecialtyResponse;
import org.arka99.model.entity.Specialty;
import org.arka99.repository.SpecialtyRepository;
import org.arka99.service.SpecialtyService;

import java.util.NoSuchElementException;

@Singleton
@TraceThread("service")
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public PageResponse<SpecialtyResponse> findAllSpecialties(PageRequest pageRequest) {
        Pageable pageable = Pageable.from(pageRequest.page() - 1, pageRequest.size());
        Page<SpecialtyResponse> specialtyResponsePage = this.specialtyRepository.findAllSpecialties(pageable);
        return PageResponse.<SpecialtyResponse>builder()
            .contents(specialtyResponsePage.getContent())
            .totalPages(specialtyResponsePage.getTotalPages())
            .currentPage(specialtyResponsePage.getPageNumber() + 1)
            .totalElements(specialtyResponsePage.getTotalSize())
            .build();
    }

    @Override
    public CursorPageResponse<SpecialtyResponse> findAllSpecialtiesByCursor(CursorPageRequest cursorPageRequest) {
        CursoredPageable cursoredPageable = createCursorPageable(cursorPageRequest);
        Page<Specialty> specialtyPage = this.specialtyRepository.findAllSpecialtiesByCursor(cursoredPageable);

        Specialty first = specialtyPage.getContent().isEmpty() ? null : specialtyPage.getContent().getFirst();
        Specialty last = specialtyPage.getContent().isEmpty() ? null : specialtyPage.getContent().getLast();

        return CursorPageResponse.<SpecialtyResponse>builder()
            .contents(specialtyPage.getContent().stream()
                .map(specialty -> new SpecialtyResponse(specialty.getId(), specialty.getName()))
                .toList())
            .hasNext(specialtyPage.hasNext())
            .hasPrevious(specialtyPage.hasPrevious())
            .nextCursor(last == null ? null : last.getId())
            .previousCursor(first == null ? null : first.getId())
            .build();
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

    private CursoredPageable createCursorPageable(CursorPageRequest cursorPageRequest) {
        Sort sort = Sort.of(Sort.Order.desc("id"));
        if (cursorPageRequest.afterId() != null && cursorPageRequest.beforeId() != null) {
            throw new IllegalArgumentException("Only one of afterId or beforeId can be supplied.");
        }

        Cursor cursor = null;
        if (cursorPageRequest.afterId() == null && cursorPageRequest.beforeId() != null) {
            cursor = Cursor.of(cursorPageRequest.beforeId());
        }

        if (cursorPageRequest.afterId() != null) {
            cursor = Cursor.of(cursorPageRequest.afterId());
        }

        if (cursor == null) {
            return CursoredPageable.from(cursorPageRequest.size(), sort);
        }

        return CursoredPageable.from(
            0,
            cursor,
            Pageable.Mode.CURSOR_NEXT,
            cursorPageRequest.size(),
            sort,
            false
        );
    }
}
