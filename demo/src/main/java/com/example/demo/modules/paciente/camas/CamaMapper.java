package com.example.demo.modules.paciente.camas;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CamaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    CamaEntity toEntity(CamaDTOs.Request request);

    CamaDTOs.Response toDTO(CamaEntity entity);
}
