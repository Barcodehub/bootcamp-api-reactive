package com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampEntityMapper {
    @Mapping(target = "capacityIds", ignore = true)
    Bootcamp toModel(BootcampEntity entity);

    @Mapping(target = "id", ignore = true)
    BootcampEntity toEntity(Bootcamp bootcamp);
}

