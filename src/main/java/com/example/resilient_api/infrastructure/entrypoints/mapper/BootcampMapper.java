package com.example.resilient_api.infrastructure.entrypoints.mapper;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BootcampMapper {
    Bootcamp bootcampDTOToBootcamp(BootcampDTO bootcampDTO);
    BootcampDTO bootcampToBootcampDTO(Bootcamp bootcamp);
}

