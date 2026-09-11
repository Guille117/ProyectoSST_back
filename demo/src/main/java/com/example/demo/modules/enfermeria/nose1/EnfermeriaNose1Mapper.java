package com.example.demo.modules.enfermeria.nose1;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnfermeriaNose1Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estado", expression = "java(request.estado() != null ? request.estado() : true)")
    EnfermeriaNose1Entity toEntity(EnfermeriaNose1DTOs.Request request);

    EnfermeriaNose1DTOs.Response toDTO(EnfermeriaNose1Entity entity);
}
