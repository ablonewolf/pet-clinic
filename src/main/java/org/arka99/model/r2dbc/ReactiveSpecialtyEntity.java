package org.arka99.model.r2dbc;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("specialties")
public class ReactiveSpecialtyEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public ReactiveSpecialtyEntity() {
    }

    public ReactiveSpecialtyEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
